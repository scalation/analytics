package apps.analytics.dashboard.ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.layout._
import javafx.stage.Stage

//import FXEvent2HandlerImplicits._

//object FXEvent2HandlerImplicits{
//  implicit def mouseEvent2EventHandler(event:(ActionEvent)=>Unit) = new EventHandler[ActionEvent]{
//    override def handle(dEvent:ActionEvent):Unit = event(dEvent)
//  }
//}

object Main {

  def main(args: Array[String]) {
    Application.launch(classOf[Main], args: _*)
  }
}

class Main extends Application {

  def createLeftPane: GridPane = {
    val leftPane = new GridPane
    leftPane.setId("leftPane")
    leftPane.setGridLinesVisible(true)

    val rowConstraint = new RowConstraints()
    rowConstraint.setPercentHeight(50)
    rowConstraint.setVgrow(Priority.ALWAYS)
    leftPane.getRowConstraints.addAll(rowConstraint, rowConstraint)

    val firstColumnConstraint = new ColumnConstraints()
    firstColumnConstraint.setPercentWidth(100)

    leftPane.getColumnConstraints.addAll(firstColumnConstraint)

    leftPane
  }

  override def start(primaryStage: Stage) {
    val DEBUG = getParameters match {
      case null => false
      case x => x.getUnnamed.size() match{
        case 0 => false
        case y => x.getUnnamed.get(0).toBoolean
      }
    }

    val mainGrid = new GridPane()
    mainGrid.setGridLinesVisible(true)

    val inputController = new InputController(DEBUG)

    //var variables: ObservableList[Variable] = FXCollections.observableArrayList[Variable]

    val rowConstraint = new RowConstraints()
    rowConstraint.setPercentHeight(100)
    rowConstraint.setVgrow(Priority.ALWAYS)
    mainGrid.getRowConstraints.add(rowConstraint)

    val firstColumnConstraint = new ColumnConstraints()
    firstColumnConstraint.setPercentWidth(40)

    val secondColumnConstraint = new ColumnConstraints()
    secondColumnConstraint.setPercentWidth(60)

    mainGrid.getColumnConstraints.addAll(firstColumnConstraint, secondColumnConstraint)
    inputController.setId("inputController")

    val leftPane = createLeftPane
    mainGrid.add(leftPane, 0, 0)

    leftPane.add(inputController, 0, 0)
    leftPane.add(new VBox(), 0, 1)

    val tabbedPane = new TabPane();
    tabbedPane.setId("tabs")
    tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB)

    val inputTab = new DatasetTab()
    inputTab.setId("dataTab")

    val modelSelectionTab = new Tab("Model Selection");
    modelSelectionTab.setId("modelSelectionTab")
    modelSelectionTab.setDisable(true)

    tabbedPane.getTabs.addAll(inputTab, modelSelectionTab)

    mainGrid.add(tabbedPane, 1, 0)


    val scene = new Scene(mainGrid)
    scene.getStylesheets.add("file:resources/main.css")
    primaryStage.setTitle("ScalaTion Analytics")
    primaryStage.setScene(scene)
    primaryStage.setWidth(1280)
    primaryStage.setHeight(800)
    primaryStage.show
  }

}



