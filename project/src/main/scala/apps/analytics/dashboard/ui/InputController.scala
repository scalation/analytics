package apps.analytics.dashboard.ui

import java.io.File
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.Text
import javafx.stage.FileChooser

import apps.analytics.dashboard.model.Model

import scala.collection.JavaConverters._

/**
 * Created by mnural on 8/11/15.
 */
//TODO Reset model if a new file is loaded.

class InputController(model: Model, DEBUG : Boolean = false) extends VBox{

  var file : File = null

  val label = new Label()

  val fileBrowser = new FileChooser
  fileBrowser.setTitle("Open Resource File");
  fileBrowser.setInitialDirectory(
    new File(System.getProperty("user.home"))
  )

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

  val getModelsButton = new Button("Get Models")

  getModelsButton.setOnAction(handleGetModels(_))
  fileButton.setOnAction(handleFileSelection(_))
  loadButton.setOnAction(handleLoadButton(_))

  getChildren.addAll(fileButton, label)


  if (DEBUG){
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

  def handleExampleButton(event: ActionEvent): Unit = {
    file = new File("/home/mnural/research/data/auto_mpg/no_missing.csv")
    label.setText(file.getName)
    fileButton.setText("Choose a Different File")
    loadButton.fire()
    getChildren.remove(this.getScene.lookup("#exampleButton"))
  }

  def handleFileSelection(actionEvent: ActionEvent): Unit ={
        file = fileBrowser.showOpenDialog(this.getScene.getWindow)
        if (file != null) {
          label.setText(file.getName)
          fileButton.setText("Choose a Different File")
          getChildren.add(fileDetailsBox)
        }
  }

  def handleLoadButton(event: ActionEvent): Unit = {
    val delimiter = delimComboBox.getSelectionModel.getSelectedItem() match {
      case "Tab" => "\t"
      case "Comma" => ","
      case _ => ","
    }

    val tabs = getScene().lookup("#tabs").asInstanceOf[TabPane]
    val dataTab = tabs.getTabs.get(0).asInstanceOf[DataTab]
    dataTab.init(file, delimiter, model)
//    inputTab.table.setItems(FXCollections.observableArrayList[Variable](model.variables.asJava))
    if (!getChildren.contains(getModelsButton)) {
      getChildren.add(getModelsButton)
    }
  }

  def handleGetModels(event: ActionEvent) : Unit = {
    println(model.variables)
    //val model = new Model(false)
    //variables.asScala.map(variable => model.variables += variable)
    val suggestedModels = model.getModelTypes
    val modelsAccordionPane = new Accordion()
    for (suggestedModel <- suggestedModels){
      val explanations = model.getExplanation(suggestedModel)
      val listView = new ListView[String]();
      listView.setCellFactory((list: ListView[String]) => {
        return new ListCell[String]() {
          {
            val text = new Text();
            text.wrappingWidthProperty().bind(list.widthProperty().subtract(15));
            text.textProperty().bind(itemProperty());

            setPrefWidth(0);
            setGraphic(text);
          }
        };
      });

      val items : ObservableList[String] = FXCollections.observableArrayList (explanations.asJavaCollection)
      listView.setItems(items);

      val titledPane = new TitledPane(model.getLabel(suggestedModel), listView)
      modelsAccordionPane.getPanes.add(titledPane)

    }
    println(suggestedModels)
//    val modelSelectionTab = getScene.lookup("#modelSelectionTab").asInstanceOf[Tab]
    val tabs = getScene().lookup("#tabs").asInstanceOf[TabPane]
    val modelSelectionTab = tabs.getTabs.get(1)
    modelSelectionTab.setDisable(false)
    modelSelectionTab.setContent(modelsAccordionPane)
    modelSelectionTab.getTabPane.getSelectionModel.select(modelSelectionTab)
  }
}
