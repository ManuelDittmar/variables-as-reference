package org.camunda.consulting.variablesAsReference.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobWorkerConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(JobWorkerConfiguration.class);

  @JobWorker(type = "dummy")
  public Map<String, Object> handleDummyJobs(final ActivatedJob job) {
    LOG.info("Handling Job with following payload: {}", job.getVariablesAsMap());
    Map<String, Object> variables = new HashMap<>();
    variables.put("result", "dummy");
    variables.put("ref_1234", "thisIsOneString");
    variables.put("ref_anotherOne", 1234);
    return variables;
  }

  @JobWorker(type = "dummyTwo")
  public Map<String, Object> handleDummyJobTwo(final ActivatedJob job) {
    LOG.info("Handling Job with following payload: {}", job.getVariablesAsMap());
    Map<String, Object> variables = new HashMap<>();
    variables.put("result", "dummy");
    variables.put("ref_1234", "thisIsTwoString");
    variables.put("ref_anotherOne", 5678);
    return variables;
  }



}
