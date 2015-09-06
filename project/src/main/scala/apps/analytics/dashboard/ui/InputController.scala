package apps.analytics.dashboard.ui

import java.io.File
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.text.Text
import javafx.stage.FileChooser

import scala.collection.JavaConverters._

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
          getChildren.add(fileDetailsBox)
        }
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
    datasetTab.init(file, delimiter)
    if (!getChildren.contains(getModelsButton)) {
      getChildren.add(getModelsButton)
    }
  }

  /**
   * Event handler for get models button.
   * A new tab will be created with the suggested models retrieved from the ontology.
   * @param event
   */
  def handleGetModels(event: ActionEvent) : Unit = {
    //println(model.variables)
    val tabs = getScene.lookup("#tabs").asInstanceOf[TabPane]
    val datasetTab = tabs.getTabs.get(0).asInstanceOf[DatasetTab]

    val model = datasetTab.getConceptualModel
    //variables.asScala.map(variable => model.variables += variable)

    val suggestionsVBox = new VBox()
    suggestionsVBox.setId("suggestionsVBox")

    val modelSummaryLabel = new Label("Suggestions for:")
    modelSummaryLabel.setId("modelSummaryLabel")

    val modelSummary = new TextArea()
    modelSummary.setBackground(suggestionsVBox.getBackground)
    modelSummary.setId("modelSummary")
    modelSummary.setText(model.toString)
    modelSummary.setEditable(false)
    modelSummary.setWrapText(true)

    val suggestedModels = model.getModelTypes
    val modelsAccordionPane = new Accordion()
    for (suggestedModel <- suggestedModels){
      val explanations = model.getExplanation(suggestedModel)
      val listView = new ListView[String]()
      listView.setCellFactory((list: ListView[String]) => {
        new ListCell[String]() {
          val text = new Text()
          text.wrappingWidthProperty().bind(list.widthProperty().subtract(15))
          text.textProperty().bind(itemProperty())

          setPrefWidth(0)
          setGraphic(text)
        }
      })

      val items : ObservableList[String] = FXCollections.observableArrayList (explanations.asJavaCollection)
      listView.setItems(items);

      val justificationVBox = new VBox()
      justificationVBox.setId("justificationVBox")
      val justificationLabel = new Label("Justification For This Suggestion")


      val runButton = new Button("Run This Model For My Dataset")

      justificationVBox.getChildren.addAll(justificationLabel, listView, runButton)
      val titledPane = new TitledPane(model.getLabel(suggestedModel), justificationVBox)
      modelsAccordionPane.getPanes.add(titledPane)

    }

    suggestionsVBox.getChildren.addAll(modelSummaryLabel, modelSummary, modelsAccordionPane)

    println(suggestedModels)
    val modelSelectionTab = tabs.getTabs.get(1)
    modelSelectionTab.setDisable(false)
    modelSelectionTab.setContent(suggestionsVBox)
    modelSelectionTab.getTabPane.getSelectionModel.select(modelSelectionTab)
  }
}
