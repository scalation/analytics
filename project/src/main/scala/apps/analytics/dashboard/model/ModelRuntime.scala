package apps.analytics.dashboard.model

import apps.analytics.dashboard.model.ModelTypes._

import scala.collection.mutable.ArrayBuffer
import scalation.analytics._
import scalation.linalgebra.MatrixD
import scalation.linalgebra.VectorD._
import scalation.relalgebra.{MakeSchema, Relation}

/**
 * Created by mnural on 8/30/15.
 *
 */
class ModelRuntime(modelType: ModelType, dataset: Model) {
  /*Model Parameters*/
  val params = Map[String, Tuple2[Any, String]]()

  lazy val predictor: Predictor = ModelRuntime.get(modelType, params, dataset)

}


object ModelRuntime {
  /**
   * Retrieves a predictors based on the given model type, dataset, and model-specific parameters
   * @param modelType the type of the model, e.g. Regression, ExpRegression, and PolyRegression.
   * @param params model-specific parameters.
   * @param dataset the meta-data
   */
  def get(modelType: ModelType, params: Map[String, Tuple2[Any, String]], dataset: Model): Predictor = {
    //load the data into a Relation object, assuming all domains are "S", StrNum
    val delim = if (dataset.mergeDelims) dataset.delimiter + "+" else dataset.delimiter
    val dataTable = Relation(dataset.file.getPath, "Current Data", -1, null, delim)
    val dataTable_s = MakeSchema(dataTable)
    println("dataTable_s domain = " + dataTable_s.domain)
    var predictorsIndices = new ArrayBuffer[Int]()
    var responseIndex = 0
    //var predictors = new MatrixD(dataTable.rows, 0)
    //var response: VectorD = null
    // TODO use params?
    for (i <- dataset.variables.indices) {
      val variable = dataset.variables(i)
      if (!(variable ignore)) {
        if (variable isResponse) responseIndex = i
        //by default, ignore all columns that are VectorS
        else if (dataTable_s.domain(i) != 'S') predictorsIndices += i
      } // if
    } // for

    val (predictors, response) = dataTable.toMatriDD(predictorsIndices, responseIndex)
    //println("predictors = " + predictors)
    //println("response = " + response)

    modelType match {
      //time data in the predictors matrix? Use params to determine?
      case ARMA =>
        new ARMA(response, predictors(0))
      case ExponentialRegression =>
        new ExpRegression(predictors.asInstanceOf[MatrixD], true, response)
      //use params to determine t and levels?
      case ModelTypes.ANCOVA =>
        new ANCOVA(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], predictors(predictors.dim2 - 1).toInt, response, 4)
      case ModelTypes.ANOVA =>
        new ANOVA(predictors(0).toInt, response, 4)
      case MultipleLinearRegression =>
        new Regression(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], response)
      case SimpleLinearRegression =>
        new SimpleRegression(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], response)
      //use params to determine t and order?
      case PolynomialRegression =>
        new PolyRegression(predictors(0), response, 8)
      case ResponseSurfaceAnalysis =>
        new ResponseSurface(predictors.asInstanceOf[MatrixD], response)
      //use params to determine transformation function?
      case TransformedMultipleLinearRegression =>
        new TranRegression(predictors.asInstanceOf[MatrixD], response)
      //use params to determine kwt?
      case TrigonometricRegression =>
        new TrigRegression(predictors(0), response, 8)
      case ModelTypes.PoissonRegression =>
        new PoissonRegression(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], response.toInt)
      case ModelTypes.Perceptron =>
        new Perceptron(predictors.asInstanceOf[MatrixD], response)
      case _ =>
        new Regression(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], response)
    } // match

  } // get
} // object ModelRuntime