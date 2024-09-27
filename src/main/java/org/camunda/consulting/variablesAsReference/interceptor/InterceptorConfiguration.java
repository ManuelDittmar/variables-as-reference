package org.camunda.consulting.variablesAsReference.interceptor;

import io.grpc.ClientInterceptor;
import java.util.List;
import org.camunda.consulting.variablesAsReference.storage.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "variable.interceptor.enabled", havingValue = "true")
public class InterceptorConfiguration {

  @Autowired
  private VariableService variableService;

  @Value("${variable.interceptor.prefix:ref_}")
  private String prefix;

  @Bean
  public List<ClientInterceptor> clientInterceptors() {
    return List.of(new VariableInterceptor(prefix,variableService));
  }

}