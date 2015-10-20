package apps.analytics.dashboard.ui

import java.lang.Boolean
import java.net.URL
import javafx.beans.binding.Bindings
import javafx.beans.property.{ObjectProperty, SimpleBooleanProperty, SimpleObjectProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.concurrent.Task
import javafx.event.{ActionEvent, Event}
import javafx.geometry.{Pos, VPos}
import javafx.scene.control._
import javafx.scene.control.cell.{CheckBoxTableCell, PropertyValueFactory, TextFieldTableCell}
import javafx.scene.layout.{GridPane, VBox}
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Model, VariableTypes}
import apps.analytics.dashboard.ui.model.FXVariable

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scalation.math.near_eq
import scalation.util.getFromURL_File

/**
 * Controller class for dataset.
 * @author Mustafa Nural
 * Created by mnural on 8/16/15.
 */
class DatasetTab(title : String = "Dataset") extends Tab {
  var mergeDelims : Boolean = false

  setClosable(false)
  setText(title) // Title of the Tab

  //Model Parameters
  var variables = ArrayBuffer[FXVariable]() // reference to the list holding variables
  var url : URL = null
  var delimiter : String = null
  var hasRepeatedObservations = new SimpleBooleanProperty()
  var noInstances : Int = -1
  var hasMissingValues = new SimpleBooleanProperty()

  val errorBox = new VBox()
  errorBox.getStyleClass.addAll("padded-vbox","error-box")

  //UI Controls
  var table : TableView[FXVariable] = null // reference to the table
  var contents : VBox = null
  var properties : GridPane = null

  val getModelsButton = new Button("Suggest Models")
  getModelsButton.setOnAction(handleGetModels(_))

  val propertiesLabel = new Label("Properties")
  propertiesLabel.getStyleClass.add("title")
  val tableLabel = new Label("Variables")
  tableLabel.getStyleClass.add("title")

  /**
   * Initialize and Configure dataset table columns
   */

  val labelColumn = new TableColumn[FXVariable,String]("Variable")
  labelColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, String]("fxLabel"))
  labelColumn.setPrefWidth(180)
  labelColumn.setCellFactory( tableColumn => {new TextFieldTableCell[FXVariable, String](new DefaultStringConverter())})

  val isResponseColumn = new TableColumn[FXVariable, Boolean]("Response?")
  isResponseColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, Boolean]("fxResponse"))
  isResponseColumn.setPrefWidth(130)
  isResponseColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isResponseColumn))

  val ignoreColumn = new TableColumn[FXVariable, Boolean]("Ignore?")
  ignoreColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, Boolean]("fxIgnore"))
  ignoreColumn.setPrefWidth(130)
  ignoreColumn.setCellFactory(CheckBoxTableCell.forTableColumn(ignoreColumn))

  val variableTypeColumn = new TableColumn[FXVariable,VariableType]("Type")
  variableTypeColumn.setCellValueFactory(new PropertyValueFactory[FXVariable,VariableType]("fxVariableType"))
  variableTypeColumn.setPrefWidth(240)
  variableTypeColumn.setCellFactory( tableColumn  => { new ComboBoxCell() })


  def reset() = {
    this.variables = ArrayBuffer()
    this.noInstances = 0
    this.hasRepeatedObservations = null
    this.hasMissingValues = null

    contents = new VBox()
    contents.getStyleClass.add("padded-vbox")
    contents.setId("dataTabContent")

    properties = new GridPane()
    properties.setId("propertiesPane")
  }

  /**
   * When called, reads dataset from the specified file and populates the dataset tab
   *
   * @param url url pointing to a local or remote dataset
   * @param delimiter The delimiter used to separate variables in the files
   * @param hasHeaders true if first line contains headers, false otherwise.
   */
  def init(url: URL, delimiter: String, hasHeaders : Boolean = true, mergeDelims : Boolean = false) : Unit = {
    reset()
    this.mergeDelims = mergeDelims
    this.hasRepeatedObservations = new SimpleBooleanProperty()
    this.hasMissingValues = new SimpleBooleanProperty()
//    this.file = file.toURI
    this.url = url
    this.delimiter = delimiter
    val stream = getFromURL_File(url.toString) //Source.fromURI(file.toURI)

    val task: Task[Void] = new Task[Void]() {
      @throws(classOf[InterruptedException])
      def call : Void = {
        updateMessage("Loading Dataset")
        val splitPattern = if (mergeDelims) delimiter + "+" else delimiter //treat consecutive delimiters as one
        val firstLine = stream.next().split(splitPattern)
        val valueList = new Array[Set[String]](firstLine.length).map(m => mutable.SortedSet[String]())

        if(hasHeaders) {
          firstLine.foreach(label => variables += new FXVariable(label))
        } else {
          firstLine.indices.foreach(i => {
            variables += new FXVariable("Variable" + i)
            valueList(i) += firstLine(i)
            noInstances += 1
          })
        }

        stream.foreach(
          line => {
            val values = line.split(splitPattern)
            values.indices.foreach(i => valueList(i) += values(i))
            noInstances += 1
          }
        )
        updateMessage("Trying to Infer Variable Types")
        valueList
          .indices
          .foreach(
            i => variables(i).fxVariableType.set(inferVariableType(valueList(i)))
          )

        variables
          .filterNot(variable => variable.fxVariableType.get().isNumeric)
          .foreach( variable => variable.fxIgnore.set(true))

        updateMessage("Dataset is loaded successfully")
        null
      }
    }

    val progressBox = new VBox()
//    progressBox.setId("dataTabContent")
    progressBox.setAlignment(Pos.TOP_CENTER)
    val progressMessage = new Label()
    val progress: ProgressIndicator = new ProgressIndicator(-1)
    progressMessage.textProperty.bind(task.messageProperty)
    progressBox.getChildren.addAll(progress, progressMessage)

    task.setOnFailed(e => {
      setContent(new Label("An error has occurred, please make sure the parameters are correct and try loading the file again."))
      task.getException.printStackTrace()
    })

    task.setOnScheduled(e => {
      setContent(progressBox)
    })

    task.setOnSucceeded(e => {
//      stream.close()

      table = new TableView[FXVariable]()
//
      table.getColumns.addAll(labelColumn, isResponseColumn, ignoreColumn, variableTypeColumn)
      table.setEditable(true)
      table.setItems(FXCollections.observableArrayList[FXVariable](variables.asJava))
      table.setFixedCellSize(35)
      table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize).add(30))

      variables.foreach(f => f.fxResponse.addListener((observable, oldValue, newValue :java.lang.Boolean) => {
        if (newValue.equals(true)) {
          toggleResponse(f)
        }
      }))

      val repeatedObservationsLabel = new Label("hasRepeatedObservations?")
      repeatedObservationsLabel.getStyleClass.addAll("bold", "right-padded")

      val repeatedObservationsCheckBox = new CheckBox()
      repeatedObservationsCheckBox.setAllowIndeterminate(false)
      repeatedObservationsCheckBox.selectedProperty().bindBidirectional(hasRepeatedObservations)

      val noInstancesLabel = new Label("Number of Instances")
      noInstancesLabel.getStyleClass.addAll("bold", "right-padded")

      val noInstancesValue = new Label(noInstances.toString)

      val missingValuesLabel = new Label("Missing Values?")
      missingValuesLabel.getStyleClass.addAll("bold", "right-padded")

      val missingValuesCheckBox = new CheckBox()
      missingValuesCheckBox.selectedProperty().bindBidirectional(hasMissingValues)
      missingValuesCheckBox.setAllowIndeterminate(false)

      GridPane.setValignment(missingValuesCheckBox, VPos.TOP)
      GridPane.setValignment(repeatedObservationsCheckBox, VPos.TOP)
      GridPane.setValignment(noInstancesValue, VPos.TOP)

      //val tooltip = new Tooltip("Repeated Observations is a good measure of kindness. This is true in any situtation")
      //repeatedObservationsLabel.setTooltip(tooltip)

      properties.addRow(0, noInstancesLabel, noInstancesValue)
      properties.addRow(1, repeatedObservationsLabel, repeatedObservationsCheckBox)
      properties.addRow(2, missingValuesLabel, missingValuesCheckBox)

      contents.getChildren.addAll(getModelsButton, propertiesLabel, properties, tableLabel, table)
      setContent(contents)

    })

    task.setOnCancelled(e =>{
      setContent(null)
    })

    val thread: Thread = new Thread(task)
    thread.setDaemon(true)
    thread.start
  }

  /**
   * Uncheck response column for all variables except the variable passed as a parameter
   * @param variable The variable that is checked as response
   */
  def toggleResponse(variable: FXVariable) = {
    variables.filterNot( _ equals variable ).foreach(_.fxResponse.set(false))
  }

  /**
   * Given a value set, this method tries to infer the domain.
   * @param valueSet
   * @return Inferred VariableType for the provided value set.
   */
  def inferVariableType(valueSet : mutable.Set[String]): VariableType = {
    valueSet.size match {
      case 1 =>
        //TODO HANDLE THIS CASE
        //Create "Constant" Variable Type?
        println("CONSTANT")
        return VariableTypes.Constant
      case 2 =>
        return VariableTypes.Binary
      case it if 3 until 7 contains it =>
        return VariableTypes.Categorical
      case _ => {
        try{
          val doubleList = valueSet.map(_.toDouble)
          // check for ordinal
          val sortedDouble = (collection.immutable.SortedSet[Double]() ++ doubleList).toBuffer
          if (sortedDouble.size >= 2){
            for (i <- 1 until sortedDouble.size){
              sortedDouble(i-1) = sortedDouble(i) - sortedDouble(i-1)
            }
          } // if
          sortedDouble.remove(sortedDouble.size-1)

          if (sortedDouble.toSet.size == 1){
            return VariableTypes.Ordinal
          } else {
            //how to use =~ instead of near_eq?
            if (doubleList.count(d => near_eq(d.toInt, d)) == doubleList.size) {
              if (doubleList.count(_ >= 0) == doubleList.size) return VariableTypes.Non_Negative_Integer
              else return VariableTypes.Integer
            } else {
              if (doubleList.count(_ >= 0) == doubleList.size) return VariableTypes.Non_Negative_Continuous
              //added "Continuous" by following the pattern above
              else return VariableTypes.Continuous
            } // if
          } // if
        } catch  { // The variable can't be cast to number.
          case e : NumberFormatException => { //Can not be cast to a number.
            return VariableTypes.String
          }
        } // try
        // The most general Variable Type? Able to take on values that are both numeric and categorical
        return VariableTypes.String //Default variable type
      }
    }
  }

  /**
   * This method provides a newly created Model object representing the current
   * state of the user interface.
   * @return runtime model to be used for obtaining model type suggestions
   */
  def getConceptualModel : Model = {
    val model = new Model(url, delimiter, hasRepeatedObservations.get(), mergeDelims)
    variables.foreach( fxVariable => model.variables += fxVariable.toVariable)
    model
  }

  /**
   * This method would set user interface to the given conceptual model.
   * This would especially be handy to revert back to a previous setting during analysis
   * @param conceptualModel
   */
  def update (conceptualModel : Model) = {
    //TODO IMPLEMENT
  }


  def clearErrors() = {
    if(!errorBox.getChildren.isEmpty){
      errorBox.getChildren.removeAll(errorBox.getChildren)
    }
    if(contents.getChildren.contains(errorBox)) {
      contents.getChildren.remove(errorBox)
    }
  }

  /**
   * Event handler for get models button.
   * A new tab will be created with the suggested models retrieved from the ontology.
   * @param event
   */
  def handleGetModels(event: ActionEvent) : Unit = {
    if (!variables.exists(variable => variable.fxResponse.get())){
      val errorMessage = new Label("No response variable is selected! Please mark one of the variables as Response. ")
      errorMessage.getStyleClass.add("text-label")
      errorBox.getChildren.add(errorMessage)
      contents.getChildren.add(0, errorBox)
    }else{
      clearErrors()
      val tabs = getTabPane
      val modelSelectionTab = tabs.getTabs.get(1).asInstanceOf[SuggestionTab]
      modelSelectionTab.setDisable(false)
      modelSelectionTab.init(getConceptualModel)
      tabs.getSelectionModel.select(modelSelectionTab)
      modelSelectionTab.getContent.requestFocus()
    }
  }

}


/**
 * Alternative ComboBoxTableCell implementation which allows "live" editing of the ComboBox.
 */
class ComboBoxCell extends TableCell[FXVariable, VariableType]{

  val items : ObservableList[VariableType] = FXCollections.observableArrayList(
    VariableTypes.values.asJavaCollection
  )

  val comboBox : ComboBox[VariableType] = new ComboBox[VariableType](items)
  val converter : ObjectProperty[StringConverter[VariableType]] =
    new SimpleObjectProperty[StringConverter[VariableType]](this, "converter")

  setText(null)
  setGraphic(comboBox)
  comboBox.setMaxWidth(Double.MaxValue)

  comboBox.getSelectionModel.selectedItemProperty.addListener(
    (observable , oldValue: VariableType, newValue: VariableType) => {
      val editEvent = new TableColumn.CellEditEvent(
        getTableView,
        new TablePosition(getTableView, getIndex, getTableColumn),
        TableColumn.editCommitEvent(),
        newValue
      )
      Event.fireEvent(getTableColumn(), editEvent)
    }
  )
//  comboBox.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[VariableType] {
//    override def changed(observable: ObservableValue[_ <: VariableType], oldValue: VariableType, newValue: VariableType): Unit = {
//      val editEvent = new TableColumn.CellEditEvent(
//        getTableView(),
//        new TablePosition(getTableView(), getIndex(), getTableColumn()),
//        TableColumn.editCommitEvent(),
//        newValue
//      )
//      Event.fireEvent(getTableColumn(), editEvent)
//    }
//  })


  override def updateItem(item: VariableType, empty: scala.Boolean) {
    super.updateItem(item, empty)
    if (this.isEmpty()) {
      this.setText(null)
      this.setGraphic(null)
    } else {
        this.setText(null)
        comboBox.getSelectionModel().select(this.getItem())
        this.setGraphic(comboBox)
    }
  }

  override def startEdit : Unit = {
    if (!isEditable || !getTableView.isEditable || !getTableColumn.isEditable) {
      return
    }
    super.startEdit
  }

  override def cancelEdit : Unit = {
    if (!isEditable || !getTableView.isEditable || !getTableColumn.isEditable) {
      return
    }
    super.cancelEdit()
  }

}