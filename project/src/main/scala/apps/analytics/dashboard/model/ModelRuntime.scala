package apps.analytics.dashboard.model

import java.net.{URI, URL}

import apps.analytics.dashboard.model.ModelTypes.ModelType

import scala.io.Source
import scalation.analytics.{Regression, Predictor}
import scalation.linalgebra.{VectorI, MatrixD}
import scalation.relalgebra.{Relation}

/**
 * Created by mnural on 8/30/15.
 */
class ModelRuntime(modelType: ModelType, dataset : Model) {
  /*Model Parameters*/
  val params = Map[String,Tuple2[Any, String]]()

  lazy val predictor : Predictor = ModelRuntime.get(modelType, params, dataset)

}

object ModelRuntime{
  // TODO IMPLEMENT THIS
  def get(modelType: ModelType, params : Map[String, Tuple2[Any, String]], dataset : Model) : Predictor = {
//    val testDataset = RelationTest3.productSales
    val dataset = Relation("https://raw.githubusercontent.com/scalation/analytics/zhaochongliu/examples/3D%20Road/3D_spatial_network.csv", "3D",0,null,",")

    val (predictors, response) = dataset.toMatriDD(1 to 2, 3)
    modelType match {
      case _ =>
        new Regression(predictors.asInstanceOf[MatrixD], response)
    }
  }
}
