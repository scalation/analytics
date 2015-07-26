package apps.analytics.dashboard.model

import javafx.beans.property._

import apps.analytics.dashboard.model.VariableTypes.VariableType

/**
 * Created by mnural on 3/29/15.
 */

class Variable(var label : String = "", var isResponse : Boolean = false, var variableType : VariableType = VariableTypes.Continuous) {

    val fxLabel = new SimpleStringProperty(label)
    var fxResponse = new SimpleBooleanProperty(isResponse)
    val fxVariableType = new SimpleObjectProperty[VariableType](variableType)

    //Use a counter to create a unique ID for ontology
    val id = "Variable" + Variable.counter ; Variable.counter += 1

    def getFxLabel() = { fxLabel.get()}
    def setFxLabel(label_ : String) = {
        this.fxLabel.set(label_)
        this.label = label_
    }

    def getFxResponse() = { fxResponse.get()}
    def setFxResponse(isResponse : Boolean) = {
        this.fxResponse.set(isResponse)
        this.isResponse = isResponse
    }

    def getFxVariableType() = { fxVariableType.get()}
    def setFxVariableType(variableType : VariableType) = {
        this.fxVariableType.set(variableType)
        this.variableType = variableType
    }
    //val columnIndex
    
    def fxResponseProperty : BooleanProperty =
    {
        return fxResponse
    }

    def fxVariableTypeProperty : ObjectProperty[VariableType] =
    {
        return fxVariableType
    }

    override def toString() = {
       fxLabel.get() + ":" + fxResponse.get() + ":" + fxVariableType.get()
    }
}


object Variable{

    // The counter to create unique ontology ID's for variables.
    var counter = 0
}

