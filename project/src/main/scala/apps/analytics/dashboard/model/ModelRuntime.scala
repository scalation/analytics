package apps.analytics.dashboard.model

import java.net.{URI, URL}

import apps.analytics.dashboard.model.ModelTypes.ModelType

import scalation.analytics.Predictor

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
}
