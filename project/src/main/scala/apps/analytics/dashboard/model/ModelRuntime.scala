package apps.analytics.dashboard.model

import java.net.{URI, URL}

import apps.analytics.dashboard.model.ModelTypes.ModelType

import scala.io.Source
import scalation.analytics.{Regression, Predictor}
import scalation.linalgebra.{VectorI, MatrixD}
import scalation.relalgebra.{Relation, RelationTest3}

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
  def get(modelType: ModelType, params : Map[String, Tuple2[Any, String]], dataset : Model) : Predictor = { null }

//  def get(modelType: ModelType, params : Map[String, Tuple2[Any, String]], dataset : Model) : Predictor = {
//    val testDataset = RelationTest3.productSales
//
//    val (predictors, response) = productSales.toMatriDD(0 to 10, 11)
//    modelType match {
//      case _ =>
//        new Regression(predictors.asInstanceOf[MatrixD], response)
//    }
//  }

}
