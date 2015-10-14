package apps.analytics.dashboard.ui

import java.io.File
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.stage.FileChooser

/**
 * Controller class for leftPane (input related actions)
 * @author Mustafa Nural
 * Created by mnural on 8/11/15.
 */
//TODO Reset model if a new file is loaded.
class InputController(DEBUG : Boolean = false) extends VBox{

  var file : File = null

  val label = new Label()

  val fileBrowser = new FileChooser
  fileBrowser.setTitle("Open Resource File");
  fileBrowser.setInitialDirectory(
    new File(System.getProperty("user.home"))
  )

  val urlLabel = new Label ("Load file from URL")
  val urlField =  new TextField()
  urlField

  val fileButton = new Button("Click to Select a File")

  val loadButton = new Button ("Load File")

  val delimLabel = new Label("Delimiter:")

  val delimComboBox = new ComboBox[String]()
  delimComboBox.setItems(FXCollections.observableArrayList("Tab", "Comma"))
  delimComboBox.getSelectionModel.select("Comma")

  val delimField = new TextField(",")
  delimField.setPrefWidth(50)

  val delimBox = new HBox()
  delimBox.setId("delimiterBox")
  delimBox.getChildren.addAll(delimLabel, delimComboBox)

  val headersLabel = new Label ("Headers in First Row?")
  val headersCheckBox = new CheckBox()
  headersCheckBox.setSelected(true)

  val headersHBox = new HBox()
  headersHBox.getChildren.addAll(headersLabel, headersCheckBox)

  val fileDetailsBox = new VBox()
  fileDetailsBox.setId("fileDetailsBox")
  fileDetailsBox.getChildren.addAll(delimBox, headersHBox, loadButton)


  fileButton.setOnAction(handleFileSelection(_))
  loadButton.setOnAction(handleLoadButton(_))

  getChildren.addAll(fileButton, urlLabel, urlField, label)


  if (DEBUG){ // Add reload CSS and load example buttons if DEBUG mode
    val exampleButton = new Button("Load Example")
    exampleButton.setId("exampleButton")
    exampleButton.setOnAction(handleExampleButton(_))
    val updateCSSButton = new Button("Reload CSS")
    updateCSSButton.setOnAction((event: ActionEvent) => {
      getScene.getStylesheets.clear();
      getScene.getStylesheets.add("file:resources/main.css");
    })
    getChildren.add(1, exampleButton)
    getChildren.add(updateCSSButton)
  }

  def init() : Unit = {
    label.setWrapText(true)
  }

  /**
   * Event handler for load example button
   * @param event
   */
  def handleExampleButton(event: ActionEvent): Unit = {
//    file = new File("../examples/3d_road/3D_spatial_network.csv")
    file = new File("../examples/auto_mpg.csv")
    label.setText(file.getName)
    fileButton.setText("Choose a Different File")
    loadButton.fire()
    getChildren.remove(this.getScene.lookup("#exampleButton"))
  }

  /**
   * Event handler for file selection
   * @param actionEvent
   */
  def handleFileSelection(actionEvent: ActionEvent): Unit ={
    file = fileBrowser.showOpenDialog(this.getScene.getWindow)
    if (file != null) {
      label.setText(file.getName)
      fileButton.setText("Choose a Different File")
      if(!getChildren.contains(fileDetailsBox)){
        getChildren.add(2, fileDetailsBox)
      }
    }
    loadButton.setText("Load File")
  }

  /**
   * Event handler for loading selected file
   * @param event
   */
  def handleLoadButton(event: ActionEvent): Unit = {
    val delimiter = delimComboBox.getSelectionModel.getSelectedItem() match {
      case "Tab" => "\t"
      case "Comma" => ","
      case _ => ","
    }

    val tabs = getScene().lookup("#tabs").asInstanceOf[TabPane]
    val datasetTab = tabs.getTabs.get(0).asInstanceOf[DatasetTab]
    datasetTab.init(file, delimiter, headersCheckBox.isSelected)
    tabs.getSelectionModel.select(datasetTab)
//    if (!getChildren.contains(getModelsButton)) {
//      getChildren.add(getModelsButton)
//    }
    loadButton.setText("Reload File")
  }

}
