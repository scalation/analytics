package apps.analytics.dashboard.ui

import java.io.File
import java.lang.Boolean
import javafx.application.Application
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.{ActionEvent, Event, EventHandler}
import javafx.scene.Scene
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.control._
import javafx.scene.control.cell.{CheckBoxTableCell, PropertyValueFactory, TextFieldTableCell}
import javafx.scene.layout._
import javafx.stage.{FileChooser, Stage}
import javafx.util.converter.DefaultStringConverter
import javafx.util.{Callback, StringConverter}

import apps.analytics.dashboard.model.VariableTypes.VariableType
import apps.analytics.dashboard.model.{Model, Variable, VariableTypes}

import scala.collection.JavaConverters._
import scala.io.Source

object Main {

    def main(args: Array[String]) {
        Application.launch(classOf[Main], args: _*)
    }
}

class Main extends Application {
    val DEBUG = true

    override def start(primaryStage: Stage) {
        //val root: Parent = FXMLLoader.load(getClass.getResource("/sample.fxml"))

        var file : File = null

        val gridPane = new GridPane()
        gridPane.setGridLinesVisible(true)
        val vbox = new VBox()

        var variables: ObservableList[Variable] = FXCollections.observableArrayList[Variable]

        val rowConstraint = new RowConstraints()
        rowConstraint.setPercentHeight(100)
        rowConstraint.setVgrow(Priority.ALWAYS)
        gridPane.getRowConstraints.add(rowConstraint)

        val firstColumnConstraint = new ColumnConstraints()
        firstColumnConstraint.setPercentWidth(29)

        val secondColumnConstraint = new ColumnConstraints()
        secondColumnConstraint.setPercentWidth(71)

        gridPane.getColumnConstraints.addAll(firstColumnConstraint, secondColumnConstraint)

        vbox.setId("leftPane")


        val label = new Label()
        label.setWrapText(true)

        gridPane.add(vbox,0,0)

        val tabbedPane = new TabPane();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE)

        val inputTab = new Tab();
        inputTab.setText("Dataset")

        val modelSelectionTab = new Tab();
        modelSelectionTab.setText("Model Selection")
        modelSelectionTab.setDisable(true)

        tabbedPane.getTabs.addAll(inputTab, modelSelectionTab)


        val table = new TableView[Variable]();
        inputTab.setContent(table)

        gridPane.add(tabbedPane,1,0)

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

        fileButton.setOnAction(
        new EventHandler[ActionEvent]() {
            override def handle(e :ActionEvent) {
                    file = fileBrowser.showOpenDialog(primaryStage)
                    if (file != null) {
                        label.setText(file.getName)
                        fileButton.setText("Choose a Different File")
                        vbox.getChildren.add(fileDetailsBox)
                    }
                }
        })

        loadButton.setOnAction(
        new EventHandler[ActionEvent] {
            override def handle(event: ActionEvent): Unit = {
                val stream = Source.fromFile(file)
                val headerLine = stream.getLines().next()
                val delimiter = delimComboBox.getSelectionModel.getSelectedItem() match {
                    case "Tab" => "\t"
                    case "Comma" => ","
                    case _ => ","
                }
                val headers = headerLine.split(delimiter)

                //val headers = headerLine.split(delimField.getText())
                variables = FXCollections.observableArrayList[Variable]()
                if (headersCheckBox.isSelected){
                    headers.map(label => variables.add(new Variable(label)))
                }else{
                    var counter = 1
                    for (header <- headers){
                        variables.add(new Variable("Variable" + counter))
                        counter +=1
                    }
                }
                table.setItems(variables)
                if (!vbox.getChildren.contains(getModelsButton)){
                    vbox.getChildren.add(getModelsButton)
                }
            }
        }
        )

        getModelsButton.setOnAction(new EventHandler[ActionEvent] {
            override def handle(event: ActionEvent): Unit = {
                println(variables)
                val model = new Model(false)
                variables.asScala.map(variable => model.variables += variable)
                val suggestedModels = model.getModelTypes
                val modelsAccordionPane = new Accordion()
                for (suggestedModel <- suggestedModels){
                    val explanations = model.getExplanation(suggestedModel)
                    val detailPane = new TextArea(explanations.toString())
                    detailPane.setWrapText(true)

                    val titledPane = new TitledPane(model.getLabel(suggestedModel), detailPane)
                    modelsAccordionPane.getPanes.add(titledPane)

                }
                println(suggestedModels)
                modelSelectionTab.setDisable(false)
                modelSelectionTab.setContent(modelsAccordionPane)
                tabbedPane.getSelectionModel.select(modelSelectionTab)

            }
        })

        vbox.getChildren.addAll(fileButton, label)

        if (DEBUG){
            val exampleButton = new Button("Load Example")
            exampleButton.setOnAction(new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    file = new File( "/home/mnural/research/data/auto_mpg/no_missing.csv")
                    label.setText(file.getName)
                    fileButton.setText("Choose a Different File")
                    loadButton.fire()
                    vbox.getChildren.remove(exampleButton)
                }
            })
            vbox.getChildren.add(1,exampleButton)

        }

        table.setEditable(true);

        val labelColumn = new TableColumn[Variable,String]("Variable");
        labelColumn.setCellValueFactory(new PropertyValueFactory[Variable, String]("fxLabel"))
        labelColumn.setCellFactory(new Callback[TableColumn[Variable,String],TableCell[Variable,String]]() {
            def call(p : TableColumn[Variable, String]) = {
                new TextFieldTableCell[Variable,String](new DefaultStringConverter())
            }
        })

        labelColumn.setOnEditCommit(new EventHandler[TableColumn.CellEditEvent[Variable,String]]{
            override def handle(event: CellEditEvent[Variable, String]): Unit = {
                event.getTableView.getItems.get(event.getTablePosition.getRow).setFxLabel(event.getNewValue)
            }
        })
        labelColumn.setPrefWidth(180);

        val isResponseColumn = new TableColumn[Variable,Boolean]("Response?");
        isResponseColumn.setCellValueFactory(new PropertyValueFactory[Variable, Boolean]("fxResponse"))
        isResponseColumn.setPrefWidth(130)
        isResponseColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isResponseColumn))

        val variableTypeColumn = new TableColumn[Variable,VariableType]("Type");
        variableTypeColumn.setCellValueFactory(new PropertyValueFactory[Variable,VariableType]("fxVariableType"))
        variableTypeColumn.setPrefWidth(240);
        variableTypeColumn.setOnEditCommit(new EventHandler[CellEditEvent[Variable, VariableType]] {
            override def handle(event: CellEditEvent[Variable, VariableType]): Unit = {
                event.getTableView.getItems.get(event.getTablePosition.getRow).setFxVariableType(event.getNewValue)
            }
        })
        variableTypeColumn.setCellFactory(new Callback[TableColumn[Variable, VariableType], TableCell[Variable, VariableType]](){
            def call(p : TableColumn[Variable, VariableType]) = {
                new ComboBoxCell()
            }
        })

        table.getColumns().addAll(labelColumn,isResponseColumn, variableTypeColumn)

        val scene = new Scene(gridPane)
        scene.getStylesheets.add("main.css")

        primaryStage.setTitle("ScalaTion Analytics")
        primaryStage.setScene(scene)
        primaryStage.setWidth(820)
        primaryStage.setHeight(480)
        primaryStage.show
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

