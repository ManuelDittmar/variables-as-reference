package org.camunda.consulting.variablesAsReference.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ActivateJobsResponse;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ActivatedJob;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CompleteJobRequest;
import io.grpc.*;
import org.bson.types.ObjectId;
import org.camunda.consulting.variablesAsReference.storage.Variable;
import org.camunda.consulting.variablesAsReference.storage.VariableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableInterceptor implements ClientInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(VariableInterceptor.class);
  private final String prefix;
  private final VariableService variableService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public VariableInterceptor(String prefix, VariableService variableService) {
    this.prefix = prefix;
    this.variableService = variableService;
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> methodDescriptor,
      CallOptions callOptions,
      Channel channel) {

    ClientCall<ReqT, RespT> delegateCall = channel.newCall(methodDescriptor, callOptions);

    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegateCall) {

      @Override
      public void sendMessage(ReqT message) {
        if (message.toString().startsWith("jobKey")) {
          CompleteJobRequest request = (CompleteJobRequest) message;
          message = (ReqT) objectToReference(request);
        }
        super.sendMessage(message);
      }

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        Listener<RespT> listener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
            responseListener) {
          @Override
          public void onMessage(RespT message) {
            if (message.toString().startsWith("jobs")) {
              ActivateJobsResponse response = (ActivateJobsResponse) message;
              message = (RespT) referenceToObject(response);
            }
            super.onMessage(message);
          }
        };
        super.start(listener, headers);
      }
    };
  }

  public ActivateJobsResponse referenceToObject(ActivateJobsResponse response) {
    List<ActivatedJob> modifiedJobs = response.getJobsList().stream().map(job -> {
      try {
        Map<String, Object> variablesAsMap = objectMapper.readValue(job.getVariables(),
            new TypeReference<Map<String, Object>>() {
            });

        variablesAsMap.replaceAll((key, value) -> {
          if (key.startsWith(prefix)) {
            try {
              logger.info("Retrieving variable {} with reference {}", key, value);
              ObjectId objectId = new ObjectId((String) value);
              Variable variable = variableService.getVariableById(objectId);
              logger.info("Value of variable {} from reference storage is {}", key, variable.getValue());
              return variable != null ? variable.getValue() : value;
            } catch (IllegalArgumentException e) {
              logger.error("Invalid ObjectId: {}", value);
              return value;
            }
          }
          return value;
        });

        return job.toBuilder().setVariables(objectMapper.writeValueAsString(variablesAsMap))
            .build();
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to process job variables", e);
      }
    }).collect(Collectors.toList());

    return response.toBuilder().clearJobs().addAllJobs(modifiedJobs).build();
  }

  public CompleteJobRequest objectToReference(CompleteJobRequest request) {
    try {
      Map<String, Object> variablesAsMap = objectMapper.readValue(request.getVariables(),
          new TypeReference<Map<String, Object>>() {
          });

      variablesAsMap.replaceAll((key, value) -> {
        if (key.startsWith(prefix)) {
          logger.info("Retrieving variable {} with value {} as reference", key, value);
          Variable variable = variableService.createVariable(key, value);
          logger.info("Reference of variable {} in reference storage is {}", key, variable.getId());
          return variable.getId().toString();
        }
        return value;
      });

      return request.toBuilder().setVariables(objectMapper.writeValueAsString(variablesAsMap)).build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to process job variables", e);
    }
  }
}