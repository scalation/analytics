package apps.analytics.dashboard.ui

import java.io.File
import java.lang.Boolean
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.Event
import javafx.scene.control._
import javafx.scene.control.cell.{CheckBoxTableCell, PropertyValueFactory, TextFieldTableCell}
import javafx.util.converter.DefaultStringConverter
import javafx.util.StringConverter

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Model, VariableTypes}
import apps.analytics.dashboard.ui.model.FXVariable

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import scalation.math.near_eq

/**
 * Controller class for dataset.
 * @author Mustafa Nural
 * Created by mnural on 8/16/15.
 */
class DatasetTab(title : String = "Dataset") extends Tab {

  setText(title) // Title of the Tab

  var table : TableView[FXVariable] = null // reference to the table

  var variables = ArrayBuffer[FXVariable]() // reference to the list holding variables

  /**
   * Initialize and Configure dataset table columns
   */

  val labelColumn = new TableColumn[FXVariable,String]("Variable");
  labelColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, String]("fxLabel"))
  labelColumn.setPrefWidth(180)
  labelColumn.setCellFactory( tableColumn => {new TextFieldTableCell[FXVariable, String](new DefaultStringConverter())})

  val isResponseColumn = new TableColumn[FXVariable, Boolean]("Response?");
  isResponseColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, Boolean]("fxResponse"))
  isResponseColumn.setPrefWidth(130)
  isResponseColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isResponseColumn))

  val ignoreColumn = new TableColumn[FXVariable, Boolean]("Ignore?")
  ignoreColumn.setCellValueFactory(new PropertyValueFactory[FXVariable, Boolean]("fxIgnore"))
  ignoreColumn.setPrefWidth(130)
  ignoreColumn.setCellFactory(CheckBoxTableCell.forTableColumn(ignoreColumn))

  val variableTypeColumn = new TableColumn[FXVariable,VariableType]("Type");
  variableTypeColumn.setCellValueFactory(new PropertyValueFactory[FXVariable,VariableType]("fxVariableType"))
  variableTypeColumn.setPrefWidth(240);
  variableTypeColumn.setCellFactory( tableColumn  => { new ComboBoxCell() })


  /**
   * When called, reads dataset from the specified file and populates the dataset tab
   *
   * @param file File object pointing to the dataset file on the machine
   * @param delimiter The delimiter used to separate variables in the files
   * @param hasHeaders true if first line contains headers, false otherwise.
   */
  def init(file: File, delimiter: String, hasHeaders : Boolean = true) : Unit = {
    val stream = Source.fromURI(file.toURI)
    val headers= stream.getLines().next().split(delimiter)

    if (hasHeaders) {
      headers.foreach(label => variables += new FXVariable(label))
      val valueList = new Array[Set[String]](headers.length).map(m => mutable.SortedSet[String]())

      stream.getLines().foreach(
        line => {
          val values = line.split(delimiter)
          values.indices.foreach(i => valueList(i) += values(i))
        }
      )

      valueList.indices.foreach(i => variables(i).fxVariableType.set(inferVariableType(valueList(i))))

    } else { //TODO HANDLE CASE WHEN HEADERS ARE NOT PRESENT
      var counter = 1
      for (header <- headers) {
        variables += new FXVariable("Variable" + counter)
        counter += 1
      }
    }
    stream.close()

    table = new TableView[FXVariable]()
    table.getColumns().addAll(labelColumn, isResponseColumn, ignoreColumn, variableTypeColumn)
    table.setEditable(true)
    table.setItems(FXCollections.observableArrayList[FXVariable](variables.asJava))
    setContent(table)

    variables.foreach(f => f.fxResponse.addListener((observable, oldValue, newValue :java.lang.Boolean) => {
      if (newValue.equals(true)) {
        toggleResponse(f)
      }
    }))

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
        return VariableTypes.Discrete
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

          if ((sortedDouble.toSet).size == 1){
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
            //TODO HOW TO HANDLE THIS? Create an ID type? Or pass null and mark the corresponding variable as ignore?
            //return null
            //VariableTypes.Continuous
            return VariableTypes.Categorical
          }
        } // try
        // The most general Variable Type? Able to take on values that are both numeric and categorical
        return VariableTypes.Discrete //Default variable type
      }
    }
  }


  /**
   * This method provides a newly created Model object representing the current
   * state of the user interface.
   * @return runtime model to be used for obtaining model type suggestions
   */
  def getRuntimeModel : Model = {
    val model = new Model()
    variables.foreach( fxVariable => model.variables += fxVariable.toVariable)
    model
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

  comboBox.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[VariableType] {
    override def changed(observable: ObservableValue[_ <: VariableType], oldValue: VariableType, newValue: VariableType): Unit = {
      val editEvent = new TableColumn.CellEditEvent(
        getTableView(),
        new TablePosition(getTableView(), getIndex(), getTableColumn()),
        TableColumn.editCommitEvent(),
        newValue
      )
      Event.fireEvent(getTableColumn(), editEvent)
    }
  })


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