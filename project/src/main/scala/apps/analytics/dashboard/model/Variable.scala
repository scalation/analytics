package apps.analytics.dashboard.model

import apps.analytics.dashboard.model.VariableTypes.VariableType

/**
 * Variable provides a conceptual representation of a single variable in the dataset.
 *
 * @author Mustafa Nural
 * Created by mnural on 3/29/15.
 */
class Variable(var label : String = "", var isResponse : Boolean = false, var variableType : VariableType = VariableTypes.Continuous, var ignore : Boolean = false) {
  //Use a counter to create a unique ID for ontology
  val id = "Variable" + Variable.counter ; Variable.counter += 1


  override def toString() = {
    label + ": " + {if(isResponse) "response" else "predictor"} + ", " + variableType
  }
}

/**
 * Companion object for variable to have a singleton counter for creating unique ontology ID's.
 */
object Variable{

  /**
   * The counter is used for creating a unique ID for variable objects in the constructor.
   */
  var counter = 0
}

