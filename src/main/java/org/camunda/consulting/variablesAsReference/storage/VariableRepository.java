package org.camunda.consulting.variablesAsReference.storage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.bson.types.ObjectId;

public interface VariableRepository extends MongoRepository<Variable, ObjectId> {
}

