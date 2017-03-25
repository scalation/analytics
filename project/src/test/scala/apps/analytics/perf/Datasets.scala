package apps.analytics.perf

import java.io.File

import scala.collection.mutable
import scalation.analytics.ExpRegression
import scalation.linalgebra.{MatriD, MatrixD, VectorD}
import scalation.plot.Plot
import scalation.relalgebra.{MakeSchema, Relation}
import scalation.stat.vectorD2StatVector

/**
  * Created by mnural on 11/26/16.
  */
object Datasets {
  val data_dir = "/home/mnural/research/project_root/analytics/examples"
  val datasets : Map[String, Dataset] = Map(
    "air" ->  new Dataset("air", data_dir + File.separator + "air/1987_clean.csv", "Airline 1987 Cleaned", ",", 12, Seq(0,1,2,3,4,5,7,13,16)),
    "auto-mpg" -> new Dataset("auto-mpg", data_dir + File.separator + "auto_mpg.csv", "Auto-MPG Dataset from UCI", ",", 0, Seq(1,2,3,4,5,6,7)),
    "airfoil" -> new Dataset("airfoil", data_dir + File.separator + "airfoil/airfoil_self_noise.csv", "Airfoil Self Noise Dataset from UCI(NASA)", ",", 5, Seq(0,1,2,3)),
    "concrete_compressive" -> new Dataset("concrete_compressive", data_dir + File.separator + "concrete_compressive/Concrete_Data.csv", "Concrete Compressive Strength Dataset from UCI", ",", 8, Seq(0,1,2,3,4,5,6,7)),
    "ccpp" -> new Dataset("ccpp", data_dir + File.separator + "ccpp/Folds5x2_pp.csv", "Combined Cycle Power Plant Dataset from UCI", ",", 4, Seq(0,1,2,3)),
    "concrete_slump_1" -> new Dataset("concrete_slump_1", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: SLUMP)", ",", 8, Seq(1,2,3,4,5,6,7)),
    "concrete_slump_2" -> new Dataset("concrete_slump_2", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: FLOW)", ",", 9, Seq(1,2,3,4,5,6,7)),
    "concrete_slump_3" -> new Dataset("concrete_slump_3", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: Compressive Strength)", ",", 10, Seq(1,2,3,4,5,6,7)),
    "nist_gauss_1" -> new Dataset("nist_gauss_1", data_dir + File.separator + "nist_gauss_1.csv", "", ",", 0, Seq(1))
  )

  def printDatasets = {
    println("NAME\t\tPATH\t\tDESCRIPTION")
    datasets
      .values
      .foreach(dataset => {
        println(dataset.name + "\t\t\t:" + dataset.path)
        println("\t\t" + dataset.desc)
      })
  }


}


class Dataset (name_ : String, path_ : String, desc_ : String, eSep: String, responseCol : Int, predictorCols : Seq[Int]){
  def path = path_
  def desc = desc_
  def name = name_

  val metadata : Map[String, String] = collectMetadata

  lazy  val rel = MakeSchema(Relation(path, name, -1, null, eSep))
  def response : VectorD = {
    rel.toVectorD(responseCol)
  }

  def predictors : MatriD = {
    rel.toMatriD(predictorCols)
  }

  def collectMetadata : Map[String, String] = {
    val metadata = mutable.Map[String, String]()
    val means : VectorD = predictorCols
      .filter(idx => rel.domain(idx) != 'S')
      .map(idx => {
        VectorD(
          rel.col(idx).asInstanceOf[VectorD].mean,
          rel.col(idx).asInstanceOf[VectorD].stddev,
          rel.col(idx).asInstanceOf[VectorD].kurtosis(),
          rel.col(idx).asInstanceOf[VectorD].skew()
        )
      })
      .reduce(_ + _) // sum metrics from all attrs
      ./(predictorCols.size)  // calculate mean of each metric

    val percNumAttr = predictorCols.filter(idx => rel.domain(idx) != 'S').size / predictorCols.size
    val df = (rel.rows - predictorCols.size - 1)

    metadata += (
      ("#features", "" + predictorCols.size),       // # of predictor attributes in the dataset
      ("#instances", "" + rel.rows),                // # of instances in the dataset
      ("meanMeans", "" + means(0)),                 // mean means of all numeric attributes
      ("meanStddev", "" + means(1)),                // mean stddev of all numeric attributes
      ("meanKurtosis", "" + means(2)),              // mean kurtosis of all numeric attributes
      ("meanSkew", "" + means(3)),                  // mean skewness of all numeric attributes
      ("percentNumericAttr", "" + percNumAttr),     // percentage of numeric attributes
      ("df", "" + df),                              // degrees of freedom
      ("rDf", "" + (rel.rows - 1) / df)             // ratio of degrees of freedom
    )

    metadata.toMap
  }


}

object DatasetsTest extends App{
//  Datasets.datasets.values.foreach(d => {
//    val  d = Datasets.datasets("auto-mpg")
//    println("Testing: " + d.name)
//    val mlr = new RidgeRegression(VectorD.one(d.predictors.dim1) +^: d.predictors.asInstanceOf[MatrixD], d.response)
//    println(d.predictors.asInstanceOf[MatrixD])
//    mlr.train()
//    println(mlr.fit)
//    println(mlr.coefficient)
////    println(mlr.rmse)
//    println("----")
//    mlr.report()
//  })
//  val x = new AnnotatedRelation(null, null, null)

  val gauss = Datasets.datasets("nist_gauss_1")
  val predictors = VectorD.one(gauss.predictors.dim1) +^: gauss.predictors.asInstanceOf[MatrixD]
//  val exp = new ExpRegression(predictors, true, gauss.response)
  val exp = new ExpRegression(predictors, true, gauss.response)
  exp.train()
  println(exp.fit)
  println(exp.coefficient)
//  exp.report
  val prediction : Seq[Double]=
    for (i <- predictors.range1) yield {
      exp.predict(predictors(i))
    }

//  println(predictors.asInstanceOf[MatrixD])
  println(predictors)
  println(prediction)
  new Plot(predictors.asInstanceOf[MatrixD].col(1), gauss.response, VectorD(prediction), null)
}

//class AnnotatedRelation (name: String, colName: Seq [String], col: Vector [Vec])
//  extends Relation(name: String, colName: Seq[String], col : Vector[Vec]){
//
//  private val facets = ArrayBuffer()
//
//  def getFacets() = {}
//
//  def getModel() = {}
//
//}