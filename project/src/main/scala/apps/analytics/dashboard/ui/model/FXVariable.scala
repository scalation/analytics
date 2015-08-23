package apps.analytics.dashboard.ui.model

import javafx.beans.property._

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Variable, VariableTypes}

/**
 * Created by mnural on 8/22/15.
 */
class FXVariable(label : String = "", isResponse : Boolean = false, variableType : VariableType = VariableTypes.Continuous, ignore : Boolean = false) {

  val fxLabel = new SimpleStringProperty(label)
  val fxResponse = new SimpleBooleanProperty(isResponse)
  val fxIgnore = new SimpleBooleanProperty(ignore)
  val fxVariableType = new SimpleObjectProperty[VariableType](variableType)



  def fxResponseProperty: BooleanProperty = {
    fxResponse
  }

  def fxIgnoreProperty: BooleanProperty = {
    fxIgnore
  }

  def fxVariableTypeProperty: ObjectProperty[VariableType] = {
    fxVariableType
  }

  def fxLabelProperty: StringProperty = {
    fxLabel
  }

  def toVariable : Variable = {
    new Variable(fxLabel.get,fxResponse.get, fxVariableType.get, fxIgnore.get)
  }
}
