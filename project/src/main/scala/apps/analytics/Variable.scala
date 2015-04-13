package apps.analytics

/**
 * Created by mnural on 3/29/15.
 */

import apps.analytics.VariableTypes._

class Variable(var label : String = "", var isResponse : Boolean = false, var variableType : VariableType = null) {

    //Use a counter to create a unique ID for ontology
    val id = "Variable" + Variable.counter ; Variable.counter += 1

    //val columnIndex
}

object Variable{

    // The counter to create unique ontology ID's.
    var counter = 0
}
