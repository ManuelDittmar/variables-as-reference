package org.camunda.consulting.variablesAsReference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class VariablesAsReferenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VariablesAsReferenceApplication.class, args);
	}

}
