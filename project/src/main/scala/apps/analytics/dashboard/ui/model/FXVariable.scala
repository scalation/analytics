package apps.analytics.dashboard.ui.model

import javafx.beans.property._

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Variable, VariableTypes}

/**
 * @author Mustafa Nural
 * Created by mnural on 8/22/15.
 *
 * JavaFX Wrapper class for Variable.
 *
 */
class FXVariable(label : String = "", isResponse : Boolean = false, variableType : VariableType = VariableTypes.Continuous, ignore : Boolean = false) {

  val fxLabel = new SimpleStringProperty(label)
  val fxResponse = new SimpleBooleanProperty(isResponse)
  val fxIgnore = new SimpleBooleanProperty(ignore)
  val fxVariableType = new SimpleObjectProperty[VariableType](variableType)
  val fxMean = new SimpleDoubleProperty(Double.NaN)
  val fxStdDev = new SimpleDoubleProperty(Double.NaN)
  val fxOverDispersed = new SimpleBooleanProperty(false)


  def fxStdDevProperty : DoubleProperty = { fxStdDev }

  def fxOverDispersedProperty : BooleanProperty = { fxOverDispersed }

  /**
   * @return The boolean property that holds isResponse variable.
   */
  def fxResponseProperty: BooleanProperty = { fxResponse }

  /**
   * @return The boolean property that holds ignore variable.
   */
  def fxIgnoreProperty: BooleanProperty = { fxIgnore }

  /**
   * @return The object property that holds VariableType.
   */
  def fxVariableTypeProperty: ObjectProperty[VariableType] = { fxVariableType }

  /**
   * @return The string property that holds label
   */
  def fxLabelProperty: StringProperty = { fxLabel }

  /**
    * @return The double property that holds mean
    */
  def fxMeanProperty: DoubleProperty = { fxMean }

  /**
   * Creates a runtime variable object from this JavaFXVariable.
   * @return an immutable runtime Variable object
   */
  def toVariable : Variable = {
    new Variable(fxLabel.get,fxResponse.get, fxVariableType.get, fxIgnore.get, fxOverDispersed.get)
  }
}
