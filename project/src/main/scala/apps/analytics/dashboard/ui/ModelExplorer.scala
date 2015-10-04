package apps.analytics.dashboard.ui

import javafx.scene.control.{ListCell, ListView, Label}
import javafx.scene.layout.VBox

import scala.collection.mutable

/**
 * Created by mnural on 9/14/15.
 */
class ModelExplorer extends VBox{
  setId("ModelExplorer")
  val label = new Label("ModelExplorer")

  val runtimeModels = mutable.ArrayBuffer()

  val modelList = new ListView[RuntimeTab]()

  //TODO COMPLETE THE BODY
  modelList.setCellFactory((list: ListView[RuntimeTab]) => {
    new ListCell[RuntimeTab]
  })

  getChildren.addAll(label, modelList)
}
