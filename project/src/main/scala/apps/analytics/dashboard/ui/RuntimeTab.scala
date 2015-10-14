package apps.analytics.dashboard.ui

import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.scene.control.{Button, ScrollPane, Tab}
import javafx.scene.layout.VBox

import apps.analytics.dashboard.model.ModelTypes.ModelType
import apps.analytics.dashboard.model.{Model, ModelRuntime}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by mnural on 9/13/15.
 */
class RuntimeTab(modelType: ModelType, conceptualModel : Model) extends Tab {
  //Set the title of the Tabbed Pane
  setId("runtimeTab")
  setClosable(true)
  setText(modelType.label + "_" + RuntimeTab.getCounter)
  val modelRuntime = new ModelRuntime(modelType, conceptualModel)
  val results : ArrayBuffer[ResultTab] = ArrayBuffer()

  val scrollPane = new ScrollPane()
  scrollPane.setId("runtimeTabContent")
  setContent(scrollPane)
  scrollPane.setFitToWidth(true)


  val contents = new VBox()
  scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)
  contents.getStyleClass.add("padded-vbox")

  val runButton = new Button("Run Model")
  runButton.setOnAction(handleRunButton(_))

  contents.getChildren.add(runButton)
  scrollPane.setContent(contents)

  def handleRunButton (event: ActionEvent) : Unit = {
    val resultTab = new ResultTab(modelRuntime, conceptualModel)
    resultTab.setText("Results: " + this.getText)
    getTabPane.getTabs.add(resultTab)
    resultTab.init
    resultTab.run()
  }
}

object RuntimeTab{
  private var counter = 0

  def getCounter: Int ={
    counter += 1
    counter
  }
}

private class MySwingNode (var width: Double = 640, var height: Double = 480) extends SwingNode {

  override def minWidth(width : Double) = {
    this.width
  }

  override def minHeight(height : Double) = {
    this.height
  }

}
