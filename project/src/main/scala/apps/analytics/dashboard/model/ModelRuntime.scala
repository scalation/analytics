package apps.analytics.dashboard.model

import apps.analytics.dashboard.model.ModelTypes._
import apps.analytics.dashboard.model.VariableTypes._

import scalation.analytics._
import scalation.linalgebra.VectorD._
import scalation.linalgebra.{VectorD, VectorS, MatrixD}
import scalation.relalgebra.Relation

/**
 * Created by mnural on 8/30/15.
 */
class ModelRuntime(modelType: ModelType, dataset: Model) {
  /*Model Parameters*/
  val params = Map[String,Tuple2[Any, String]]()

  lazy val predictor: Predictor = ModelRuntime.get(modelType, params, dataset)

}

object ModelRuntime {
  def get(modelType: ModelType, params: Map[String, Tuple2[Any, String]], dataset: Model): Predictor = {
    //load the data into a Relation object, assuming all domains are "S", StrNum
    val dataTable = Relation(dataset.file.toString.substring(6), "Current Data", -1, null, dataset.delimiter)
    //var predictorsIndices = new ArrayBuffer[Int]()
    //var responseIndex = 0
    var predictors = new MatrixD(dataTable.rows, 0)
    var response: VectorD = null
    // TODO use params?
    for (i <- 0 until dataset.variables.length) {
      val variable = dataset.variables(i)
      if (!(variable ignore)) {
        val col_i = dataTable.col(i).asInstanceOf[VectorS]
        //FIX: how to treat ordinal?
        if (variable.variableType == Continuous || variable.variableType == Non_Negative_Continuous ||
          variable.variableType == Integer || variable.variableType == Non_Negative_Integer) {
          if (variable isResponse) response = col_i.toDouble
          else predictors = predictors :^+ col_i.toDouble
        }
        else {
          if (variable isResponse) response = col_i.mapToDouble
          else predictors = predictors :^+ col_i.mapToDouble
        } // if
      } // if
    } // for

    //val (predictors, response) = dataTable.toMatriDD(predictorsIndices, responseIndex)
    println("predictors = " + predictors)
    println("response = " + response)

    modelType match {
      //time data in the predictors matrix? Use params to determine?
      case ARMA =>
        return new ARMA(response, predictors(0))
      case ExponentialRegression =>
        return new ExpRegression(predictors, true, response)
      //use params to determine t and levels? add_one to design matrix?
      case ModelTypes.ANCOVA =>
        return new ANCOVA(one (predictors.dim1) +^: predictors.sliceCol(0, predictors.dim2 - 1), predictors(predictors.dim2 - 1).toInt, response, 4)
      case ModelTypes.ANOVA =>
        return new ANOVA(predictors(0).toInt, response, 4)
      case MultipleLinearRegression =>
        return new Regression(one (predictors.dim1) +^: predictors, response)
      case SimpleLinearRegression =>
        return new SimpleRegression(one (predictors.dim1) +^: predictors, response)
      //use params to determine t and order?
      case PolynomialRegression =>
        return new PolyRegression(predictors(0), response, 8)
      case ResponseSurfaceAnalysis =>
        return new ResponseSurface(predictors, response)
      //use params to determine transformation function?
      case TransformedMultipleLinearRegression =>
        return new TranRegression(predictors, response)
      //use params to determine kwt?
      case TrigonometricRegression =>
        return new TrigRegression(predictors(0), response, 8)
      case ModelTypes.PoissonRegression =>
        return new PoissonRegression(one (predictors.dim1) +^: predictors, response.toInt)
      case ModelTypes.Perceptron =>
        return new Perceptron(predictors, response)
      case _ =>
        new Regression(one (predictors.dim1) +^: predictors, response)
    }

  }
}
