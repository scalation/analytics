package apps.analytics.dashboard.ui

import javafx.scene.control.{Label, ListCell, ListView}
import javafx.scene.layout.VBox

import scala.collection.mutable

/**
 * Created by mnural on 9/14/15.
 */
class ModelExplorer extends VBox{
  setId("modelExplorer")
  getStyleClass.add("padded-vbox")

  val label = new Label("ModelExplorer")

  val runtimeModels = mutable.ArrayBuffer()

  val modelList = new ListView[RuntimeTab]()

  //TODO PAIR WITH RESULT TAB, GO TO TAB ON CLICK
  modelList.setCellFactory((list: ListView[RuntimeTab]) => {
    new ListCell[RuntimeTab](){
      override def updateItem(item : RuntimeTab, empty : Boolean) {
        super.updateItem(item, empty)
        if(!empty){
          setText(item.getText)
        }
      }
    }
  })



  getChildren.addAll(label, modelList)

  def add(runtimeTab : RuntimeTab): Unit ={
    modelList.getItems.add(runtimeTab)
  }
}
