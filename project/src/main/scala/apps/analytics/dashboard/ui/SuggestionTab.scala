package apps.analytics.dashboard.ui

import javafx.beans.binding.Bindings
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.layout.{HBox, StackPane, VBox}
import javafx.scene.text._

import apps.analytics.dashboard.model.ModelTypes.ModelType
import apps.analytics.dashboard.model.{Model, ModelTypes}

import scala.collection.JavaConverters._

/**
  * Created by mnural on 10/19/15.
  */
class SuggestionTab (title : String = "Model Selection") extends Tab {
  setClosable(false)
  setText(title)

  var tabs : TabPane = null
  var conceptualModel : Model = null

  val modelTabContent = new VBox()
  modelTabContent.setId("modelSelectionTabContent")

  val summaryLabel = new Label("Conceptual Model Summary:")
  summaryLabel.setId("modelSummaryLabel")

  val suggestedModelsLabel = new VBox(new Label("Suggested Models"))
  suggestedModelsLabel.setId("suggestedModelsLabel")
  suggestedModelsLabel.setAlignment(Pos.CENTER)

  var suggestionsListView : ListView[String] = null

  var justificationsPane: StackPane = null


  val summary = new TextFlow()
  summary.setBackground(modelTabContent.getBackground)
  summary.setId("modelSummary")
//  summary.setWrapText(true)
  summary.getStyleClass.add("text-label")


  def reset(): Unit = {
    setContent(null)
    tabs = getTabPane
    summary.getChildren.removeAll(summary.getChildren)

    suggestionsListView = new ListView[String]()
    suggestionsListView.setId("modelsListView")
    suggestionsListView.setFixedCellSize(30)


    justificationsPane = new StackPane()

    modelTabContent.getChildren.removeAll(modelTabContent.getChildren)

  }

  def init(conceptualModel : Model): Unit ={
    reset()
    this.conceptualModel = conceptualModel
    val suggestedModels = conceptualModel.getModelTypes.toList

    conceptualModel.variables
      .filter(_.isResponse)
      .foreach(v => {
        val label = new Text(v.label)
        label.getStyleClass.add("bold")
        val variableType = new Text("(" + v.variableType.toString.toLowerCase() + ")")
        variableType.getStyleClass.add("italic")
        val operator = new Text(" ~ ")
        operator.getStyleClass.add("bold")
        summary.getChildren.addAll(label, variableType, operator)
    })

    conceptualModel.variables
      .filterNot(v => v.ignore || v.isResponse)
      .foreach(v=> {
        val label = new Text(v.label)
        label.getStyleClass.add("bold")
        val variableType = new Text("(" + v.variableType.toString.toLowerCase() + ")")
        variableType.getStyleClass.add("italic")
        val operator = new Text(" + ")
        operator.getStyleClass.add("bold")
        summary.getChildren.addAll(label, variableType, operator)
      })
    summary.getChildren.remove(summary.getChildren.size() - 1) //remove operator after last item

    val hasRepeatedObservations = new Text ({if(conceptualModel.hasRepeatedObservations) "Yes" else "No"})
    hasRepeatedObservations.getStyleClass.add("bold")
    summary.getChildren.addAll(new Text("\n\nHas repeated observations? : "), hasRepeatedObservations)

    val suggestions : ObservableList[String] = FXCollections.observableArrayList (
      suggestedModels.map(suggestedModel => {
        ModelTypes.getById(suggestedModel.getIRI.getRemainder.get()).label
      }).asJavaCollection
    )

    for (suggestedModel <- suggestedModels){
      val explanations = conceptualModel.getExplanation(suggestedModel)
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
      listView.setItems(items)

      val justificationVBox = new VBox()
      justificationVBox.setId("justificationVBox")
      val justificationLabel = new Label("Justification For This Suggestion")

      val runButton = new Button("Use This Model For My Dataset")
      runButton.setLineSpacing(10)
      runButton.setOnAction(handleRunModel(_))

      justificationVBox.getChildren.addAll(runButton, justificationLabel, listView)
      justificationsPane.getChildren.add(justificationVBox)
    }

    suggestionsListView.getSelectionModel.selectedIndexProperty().addListener((observable, oldValue: Number, newValue: Number) => {
      justificationsPane.getChildren.asScala.foreach(pane => pane.setOpacity(0))
      justificationsPane.getChildren.get(newValue.intValue()).setOpacity(1)
    })

    suggestionsListView.setItems(suggestions)
    suggestionsListView.getSelectionModel.select(0)


    suggestionsListView.prefWidthProperty().bind(getTabPane.widthProperty().subtract(2).divide(3))
    justificationsPane.prefWidthProperty().bind(getTabPane.widthProperty().subtract(2).divide(3).multiply(2))

    val suggestionHBox = new HBox(suggestionsListView, justificationsPane)
    suggestionHBox.setId("suggestionPane")

    suggestionsListView.maxHeightProperty().bind(Bindings.size(suggestionsListView.getItems()).multiply(suggestionsListView.getFixedCellSize))

    modelTabContent.getChildren.addAll(summaryLabel, summary, suggestedModelsLabel, suggestionHBox)
    setContent(modelTabContent)
  }

  def handleRunModel(event: ActionEvent) = {
    val modelType : ModelType = ModelTypes.getByLabel(suggestionsListView.getSelectionModel.getSelectedItem)
    val runtimeTab = new RuntimeTab(modelType, conceptualModel)

    getTabPane.getScene.lookup("#modelExplorer").asInstanceOf[ModelExplorer].add(runtimeTab)

    getTabPane.getTabs.add(runtimeTab)
    getTabPane.getSelectionModel.select(runtimeTab)
    runtimeTab.getContent.requestFocus()

  }
}
