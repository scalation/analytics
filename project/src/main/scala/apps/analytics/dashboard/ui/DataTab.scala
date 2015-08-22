package apps.analytics.dashboard.ui

import java.io.File
import java.lang.Boolean
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.Event
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.control._
import javafx.scene.control.cell.{CheckBoxTableCell, PropertyValueFactory, TextFieldTableCell}
import javafx.util.converter.DefaultStringConverter
import javafx.util.{Callback, StringConverter}

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Model, Variable, VariableTypes}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source

/**
 * Created by mnural on 8/16/15.
 */
class DataTab(title : String = "Dataset") extends Tab {

  setText(title)

  var table : TableView[Variable] = null //new TableView[Variable]();

  //setContent(table)

  val labelColumn = new TableColumn[Variable,String]("Variable");
  labelColumn.setCellValueFactory(new PropertyValueFactory[Variable, String]("fxLabel"))
  labelColumn.setCellFactory(new Callback[TableColumn[Variable,String],TableCell[Variable,String]]() {
    def call(p : TableColumn[Variable, String]) = {
      new TextFieldTableCell[Variable,String](new DefaultStringConverter())
    }
  })

  labelColumn.setOnEditCommit((event: CellEditEvent[Variable, String]) => {
    event.getTableView.getItems.get(event.getTablePosition.getRow).setFxLabel(event.getNewValue)
  })
  labelColumn.setPrefWidth(180);

  val isResponseColumn = new TableColumn[Variable, Boolean]("Response?");
  isResponseColumn.setCellValueFactory(new PropertyValueFactory[Variable, Boolean]("fxResponse"))
  isResponseColumn.setPrefWidth(130)
  isResponseColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isResponseColumn))

  val variableTypeColumn = new TableColumn[Variable,VariableType]("Type");
  variableTypeColumn.setCellValueFactory(new PropertyValueFactory[Variable,VariableType]("fxVariableType"))
  variableTypeColumn.setPrefWidth(240);
  variableTypeColumn.setOnEditCommit((event: CellEditEvent[Variable, VariableType]) => {
    event.getTableView.getItems.get(event.getTablePosition.getRow).setFxVariableType(event.getNewValue)
  })
  variableTypeColumn.setCellFactory(new Callback[TableColumn[Variable, VariableType], TableCell[Variable, VariableType]](){
    def call(p : TableColumn[Variable, VariableType]) = {
      new ComboBoxCell()
    }
  })

  def init(file: File, delimiter: String, model : Model, hasHeaders : Boolean = true) : Unit = {
    val stream = Source.fromURI(file.toURI)
    val headers= stream.getLines().next().split(delimiter)
    //
    ////    val headers = headerLine.split(delimField.getText())
        //variables = FXCollections.observableArrayList[Variable]()
    if (hasHeaders) {
      headers.foreach(label => model.variables += new Variable(label))
      val valueList = new Array[Set[String]](headers.length).map(m => mutable.SortedSet[String]())

      stream.getLines().foreach(
        line => {
          val values = line.split(delimiter)
          values.indices.foreach(i => valueList(i) += values(i))
        }
      )

      valueList.indices.foreach(i => model.variables(i).setFxVariableType(inferVariableType(valueList(i))))

    } else {
      var counter = 1
      for (header <- headers) {
        model.variables += new Variable("Variable" + counter)
        counter += 1
      }
    }
    stream.close()


    table = new TableView[Variable]()
    table.getColumns().addAll(labelColumn, isResponseColumn, variableTypeColumn)
    table.setEditable(true)
    table.setItems(FXCollections.observableArrayList[Variable](model.variables.asJava))
    setContent(table)

  }

  def inferVariableType(valueList : mutable.Set[String]): VariableType = {
    valueList.size match {
      case 1 =>
        //TODO HANDLE THIS CASE
        println("CONSTANT")
        return VariableTypes.Discrete
      case 2 =>
        return VariableTypes.Binary
      case it if 3 until 10 contains it =>
        return VariableTypes.Categorical
      case _ => {
        try{
          val doubleList = valueList.map(_.toDouble)
          if(doubleList.count(d => d.toInt == d) == doubleList.size){
            if(doubleList.count(_ >= 0) == doubleList.size){
              return VariableTypes.Non_Negative_Integer
            }else{
              return VariableTypes.Integer
            }
          } else {
            if(doubleList.count(_ >= 0) == doubleList.size){
              return VariableTypes.Non_Negative_Continuous
            }
          }
        } catch  {
          case e : NumberFormatException => { //Can not be cast to a number.
            //HOW TO HANDLE THIS?
            //return null
            return VariableTypes.Continuous
          }
        }

        return VariableTypes.Continuous //Default variable type
      }
    }
  }

}



class ComboBoxCell extends TableCell[Variable, VariableType]{

  val items : ObservableList[VariableType] = FXCollections.observableArrayList(
    VariableTypes.values.asJavaCollection
  )

  val comboBox: ComboBox[VariableType] = new ComboBox[VariableType](items)
  val converter : ObjectProperty[StringConverter[VariableType]] =
    new SimpleObjectProperty[StringConverter[VariableType]](this,"converter")

  setText(null)
  setGraphic(comboBox)
  comboBox.setMaxWidth(Double.MaxValue)
  //comboBox.editableProperty.bind(comboBoxEditableProperty)
  comboBox.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[VariableType] {
    override def changed(observable: ObservableValue[_ <: VariableType], oldValue: VariableType, newValue: VariableType): Unit = {
      val editEvent = new TableColumn.CellEditEvent(
        getTableView(),
        new TablePosition(getTableView(), getIndex(), getTableColumn()),
        TableColumn.editCommitEvent(),
        newValue
      );
      Event.fireEvent(getTableColumn(), editEvent);
    }
  })

  comboBox.getSelectionModel.select(getItem)

  override def updateItem(item: VariableType, empty: scala.Boolean) {
    super.updateItem(item, empty);
    if (this.isEmpty()) {
      this.setText(null)
      this.setGraphic(null)
    } else {
      if (this.isEditing()) {
        if (comboBox != null) {
          comboBox.getSelectionModel().select(this.getItem())
        }
        this.setText(null)
        this.setGraphic(comboBox)

      } else {
        this.setText(null)
        comboBox.getSelectionModel().select(this.getItem())
        this.setGraphic(comboBox)
      }
    }
  }

  override def startEdit : Unit = {
    if (!isEditable || !getTableView.isEditable || !getTableColumn.isEditable) {
      return
    }
    comboBox.getSelectionModel.select(getItem)
    super.startEdit
    //        setText(null)
    //        setGraphic(comboBox)
  }

  override def cancelEdit : Unit = {
    if (!isEditable || !getTableView.isEditable || !getTableColumn.isEditable) {
      return
    }
    comboBox.getSelectionModel.select(getItem)
    super.cancelEdit()
    //        setText(null)
    //        setGraphic(comboBox)
  }

}