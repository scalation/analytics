package apps.analytics.dashboard.ui

import java.io.File
import java.net.{HttpURLConnection, URL}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.input.Clipboard
import javafx.scene.layout.{HBox, VBox}
import javafx.stage.FileChooser

/**
 * Controller class for leftPane (input related actions)
 * @author Mustafa Nural
 * Created by mnural on 8/11/15.
 */
//TODO Reset model if a new file is loaded.
class InputController(DEBUG : Boolean = false) extends VBox{

  var url : URL = null

  val label = new Label()

  val fileBrowser = new FileChooser
  fileBrowser.setTitle("Open Resource File")
  fileBrowser.setInitialDirectory(
    new File(System.getProperty("user.home"))
  )

  val urlLabel = new Label ("Load file from URL")
  val urlDescLabel = new Label (" Paste the file URL or type the URL and hit Enter")
  urlDescLabel.setStyle("-fx-font-size:12;-fx-font-style:italic")
  val urlField =  new TextField(){
    override def paste(): Unit ={
      val clipboard = Clipboard.getSystemClipboard
      if (clipboard.hasString) {
        replaceSelection(clipboard.getString)
        validateURL(null)
      }
    }
  }

  urlField.setOnAction(validateURL(_))
  val urlBox = new VBox(urlLabel,urlDescLabel,urlField)


  val fileButton = new Button("Click to Select a File")

  val loadButton = new Button ("Load File")

  val fileDetailsBox = new VBox()

  val delimLabel = new Label("Delimiter:")

  val delimComboBox = new ComboBox[String]()
  delimComboBox.setItems(FXCollections.observableArrayList("Tab", "Comma", "Space", "Custom"))
  delimComboBox.getSelectionModel.select("Comma")

  val delimFieldLabel= new Label ("Custom Delimiter: ")
  val delimField = new TextField(",")
  delimField.setPrefWidth(50)

  val customDelimBox = new HBox ()
  customDelimBox.getChildren.addAll(delimFieldLabel, delimField)

  val mergeDelimsLabel = new Label("Treat consecutive delimiters as one?")
  val mergeDelimsCheckBox = new CheckBox()
  mergeDelimsCheckBox.setSelected(false)

  val mergeDelimsBox = new HBox()
  mergeDelimsBox.getChildren.addAll(mergeDelimsLabel, mergeDelimsCheckBox)

  val delimBox = new HBox()
  delimBox.setId("delimiterBox")
  delimBox.getChildren.addAll(delimLabel, delimComboBox)

  val headersLabel = new Label ("Headers in First Row?")
  val headersCheckBox = new CheckBox()
  headersCheckBox.setSelected(true)

  val headersHBox = new HBox()
  headersHBox.getChildren.addAll(headersLabel, headersCheckBox)

  fileDetailsBox.setId("fileDetailsBox")
  fileDetailsBox.getChildren.addAll(delimBox, mergeDelimsBox, headersHBox, loadButton)

  delimComboBox.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[String] {
    override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      newValue match {
      case "Custom" =>
      {
        if (!fileDetailsBox.getChildren.contains(customDelimBox)) fileDetailsBox.getChildren.add(1, customDelimBox)
      }
      case _ => {}
        if (fileDetailsBox.getChildren.contains(customDelimBox)) fileDetailsBox.getChildren.remove(customDelimBox)
    }
  }
})

  fileButton.setOnAction(handleFileSelection(_))
  loadButton.setOnAction(handleLoadButton(_))

  getChildren.addAll(fileButton, urlBox, label)


  if (DEBUG){ // Add reload CSS and load example buttons if DEBUG mode
    val exampleButton = new Button("Load Example")
    exampleButton.setId("exampleButton")
    exampleButton.setOnAction(handleExampleButton(_))
    val updateCSSButton = new Button("Reload CSS")
    updateCSSButton.setOnAction((event: ActionEvent) => {
      getScene.getStylesheets.clear()
      getScene.getStylesheets.add("file:resources/main.css");
    })
    getChildren.add(1, exampleButton)
    getChildren.add(updateCSSButton)
  }

  def init() : Unit = {
    label.setWrapText(true)
  }

  def validateURL(event: ActionEvent) : Unit = {
    val prefixPatttern = "(?i)^(https?|ftp|)://.*"
    val url = if(urlField.getText matches prefixPatttern) new URL(urlField.getText) else new URL("http://" + urlField.getText)
    val urlConnection = url.openConnection().asInstanceOf[HttpURLConnection]
    if(urlConnection.getResponseCode == 200){
      this.url = url
      if(!getChildren.contains(fileDetailsBox)){
        getChildren.add(getChildren.indexOf(urlBox) + 1, fileDetailsBox)
      }
    }else{
      //TODO Display error
    }
  }


  /**
   * Event handler for load example button
   * @param event
   */
  def handleExampleButton(event: ActionEvent): Unit = {
//    file = new File("../examples/3d_road/3D_spatial_network.csv")
    val file = new File("../examples/auto_mpg.csv")
    this.url = file.getAbsoluteFile.toURI.toURL
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
    val file = fileBrowser.showOpenDialog(this.getScene.getWindow)
    if (file != null) {
      this.url = file.getAbsoluteFile.toURI.toURL
      label.setText(file.getName)
      fileButton.setText("Choose a Different File")
      if(!getChildren.contains(fileDetailsBox)){
        getChildren.add(getChildren.indexOf(urlBox) + 1, fileDetailsBox)
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
      case "Space" => " "
      case "Custom" => delimField.getText
      case _ => ","
    }

    val tabs = getScene.lookup("#tabs").asInstanceOf[TabPane]
    val datasetTab = tabs.getTabs.get(0).asInstanceOf[DatasetTab]
    datasetTab.init(url, delimiter, headersCheckBox.isSelected, mergeDelimsCheckBox.isSelected)
    tabs.getSelectionModel.select(datasetTab)
//    if (!getChildren.contains(getModelsButton)) {
//      getChildren.add(getModelsButton)
//    }
    loadButton.setText("Reload File")
  }

}
