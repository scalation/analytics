package apps.analytics.dashboard.ui

import javafx.event.ActionEvent
import javafx.scene.control.{Label, Button, Tab}
import javafx.scene.layout.VBox

import apps.analytics.dashboard.model.ModelTypes.ModelType
import apps.analytics.dashboard.model.{Model, ModelRuntime}

/**
 * Created by mnural on 9/13/15.
 */
class RuntimeTab(modelType: ModelType, conceptualModel : Model) extends Tab {
  //Set the title of the Tabbed Pane
  setClosable(true)
  setText(modelType.label + "_" + RuntimeTab.getCounter)
  val modelRuntime = new ModelRuntime(modelType, conceptualModel)

  val contents = new VBox()
  contents.getStyleClass.add("padded-vbox")

  val runButton = new Button("Run Model")
  runButton.setOnAction(handleRunButton(_))

  contents.getChildren.add(runButton)
  setContent(contents)

  def handleRunButton(event: ActionEvent) = {
    val predictor = modelRuntime.predictor
    predictor.train()
    val fit = new Label(predictor.fit.toString())
    val coefficients = new Label(predictor.coefficient.toString())
    contents.getChildren.addAll(fit, coefficients)

  }
}

object RuntimeTab{
  private var counter = 0

  def getCounter: Int ={
    counter += 1
    counter
  }
}
