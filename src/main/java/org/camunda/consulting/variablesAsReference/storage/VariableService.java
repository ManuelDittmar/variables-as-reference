package org.camunda.consulting.variablesAsReference.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import java.util.List;

@Service
public class VariableService {

  @Autowired
  private VariableRepository variableRepository;

  public List<Variable> getAllVariables() {
    return variableRepository.findAll();
  }

  public Variable getVariableById(ObjectId id) {
    return variableRepository.findById(id).orElse(null);
  }

  public Variable createVariable(String name, Object value) {
    Variable variable = new Variable(name, value);
    return variableRepository.save(variable);
  }

  public Variable updateVariable(ObjectId id, String name, Object value) {
    Variable variable = variableRepository.findById(id).orElse(null);
    if (variable != null) {
      variable.setName(name);
      variable.setValue(value);
      return variableRepository.save(variable);
    }
    return null;
  }

  public void deleteVariable(ObjectId id) {
    variableRepository.deleteById(id);
  }
}

