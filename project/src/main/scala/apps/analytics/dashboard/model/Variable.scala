package apps.analytics.dashboard.model

import apps.analytics.dashboard.model.VariableTypes.VariableType

/**
 * Created by mnural on 3/29/15.
 */

class Variable(var label : String = "", var isResponse : Boolean = false, var variableType : VariableType = VariableTypes.Continuous, var ignore : Boolean = false) {

  //Use a counter to create a unique ID for ontology
  val id = "Variable" + Variable.counter ; Variable.counter += 1

}

object Variable{

  // The counter to create unique ontology ID's for variables.
  var counter = 0
}

