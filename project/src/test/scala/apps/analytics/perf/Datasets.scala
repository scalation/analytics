package apps.analytics.perf

import java.io.File

import apps.analytics.model.VariableTypes

import scala.collection.immutable.HashSet
import scala.collection.mutable
import scalation.analytics.{ExpRegression, Regression}
import scalation.linalgebra._
import scalation.math.StrO.StrNum
import scalation.plot.Plot
import scalation.relalgebra.{MakeSchema, Relation}
import scalation.stat.{vectorD2StatVector, vectorI2StatVector}

/**
  * Created by mnural on 11/26/16.
  */
object Datasets {
  val missingValueString = "?"
  val data_dir = "/home/mnural/research/scalation/analytics/examples"
  val datasets : Map[String, Dataset] = Map(
    "air" ->  new Dataset("air", data_dir + File.separator + "air/1987_clean.csv", "Airline 1987 Cleaned", ",", 12, Seq(1,2,3,4,5,7,13,16)),
    "auto-mpg" -> new Dataset("auto-mpg", data_dir + File.separator + "auto_mpg.csv", "Auto-MPG Dataset from UCI", ",", 0, Seq(1,2,3,4,5,6,7)),
    "airfoil" -> new Dataset("airfoil", data_dir + File.separator + "airfoil/airfoil_self_noise.csv", "Airfoil Self Noise Dataset from UCI(NASA)", ",", 5, Seq(0,1,2,3)),
    "concrete_compressive" -> new Dataset("concrete_compressive", data_dir + File.separator + "concrete_compressive/Concrete_Data.csv", "Concrete Compressive Strength Dataset from UCI", ",", 8, Seq(0,1,2,3,4,5,6,7)),
    "ccpp" -> new Dataset("ccpp", data_dir + File.separator + "ccpp/Folds5x2_pp.csv", "Combined Cycle Power Plant Dataset from UCI", ",", 4, Seq(0,1,2,3)),
    "concrete_slump_1" -> new Dataset("concrete_slump_1", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: SLUMP)", ",", 8, Seq(1,2,3,4,5,6,7)),
    "concrete_slump_2" -> new Dataset("concrete_slump_2", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: FLOW)", ",", 9, Seq(1,2,3,4,5,6,7)),
    "concrete_slump_3" -> new Dataset("concrete_slump_3", data_dir + File.separator + "concrete_slump/slump_test.csv", "Concrete Slump Dataset from UCI (OUTPUT: Compressive Strength)", ",", 10, Seq(1,2,3,4,5,6,7)),
    "nist_gauss_1" -> new Dataset("nist_gauss_1", data_dir + File.separator + "nist_gauss_1.csv", "", ",", 0, Seq(1))
//    "air_quality" -> new Dataset("air_quality", data_dir + File.separator + "air_quality/AirQualityUCI.csv", "", ";", 0,)
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

  def apply(name : String): Dataset = {
    datasets(name)
  }
}


class Dataset (name_ : String, path_ : String, desc_ : String, eSep: String, responseCol : Int, _predictorCols : Seq[Int]){
  def path = path_
  def desc = desc_
  def name = name_
  def predictorCols = _predictorCols


  lazy val metadata : Map[String, String] = collectMetadata

  lazy  val rel = MakeSchema(Relation(path, name, -1, null, eSep))
  def response : VectorD = {
    rel.toVectorD(responseCol)
  }

  def predictors : MatriD = {
    rel.toMatriD(predictorCols)
  }

  def collectMetadata : Map[String, String] = {
    val metadata = mutable.Map[String, String]()

    val attrs : mutable.Map[Int, String] = mutable.Map()
    val df = (rel.rows - predictorCols.size - 1)

    predictorCols.foreach( idx => {
      val domain = rel.domain(idx)
      val vec =
        domain match {
          case 'I' => rel.toVectorI(idx)
          case 'D' => rel.toVectorD(idx)
          case _ => rel.toVectorS(idx)
        }
      val variableType = VariableTypes.inferVariableType(vec, domain)
      if (VariableTypes.numeric.contains(variableType)){
        attrs += ((idx, "numeric"))
      }else if (variableType == VariableTypes.Binary){
        attrs += ((idx, "binary"))
      }else if (VariableTypes.nominal.contains(variableType)){
        attrs += ((idx, "nominal"))
      }
    })

    val percNumAttr  = attrs.filter(_._2.equals("numeric") ).size.toDouble / predictorCols.size
    val percNomAttr = attrs.filter(_._2.equals("nominal") ).size.toDouble / predictorCols.size
    val percBinAttr = attrs.filter(_._2.equals("binary") ).size.toDouble / predictorCols.size

    val numericMeans : VectorD = {
      if (percNumAttr == 0){
        VectorD(0,0,0,0)
      }else {
        attrs
          .filter(_._2.equals("numeric"))
          .keys
          .map(idx => {
            if (rel.domain(idx) == 'D') {
              VectorD(
                rel.col(idx).asInstanceOf[VectorD].mean,
                rel.col(idx).asInstanceOf[VectorD].stddev,
                rel.col(idx).asInstanceOf[VectorD].kurtosis(),
                rel.col(idx).asInstanceOf[VectorD].skew()
              )
            }
            else {
              VectorD(
                rel.col(idx).asInstanceOf[VectorI].mean,
                rel.col(idx).asInstanceOf[VectorI].stddev,
                rel.col(idx).asInstanceOf[VectorI].kurtosis(),
                rel.col(idx).asInstanceOf[VectorI].skew()
              )
            }
          })
          .reduce(_ + _) // sum metrics from all attrs
          ./(percNumAttr * predictorCols.size) // calculate mean of each metric
      }
    }

    val nominalDistinctVals = {
      if(percNomAttr == 0){
        VectorI(0)
      }else{
        VectorI(
          attrs.keys.toList
          .filter(attrs(_).equals("nominal"))
          .map(idx => {
            val size : Int = rel.domain(idx) match{
              case 'I' => HashSet(rel.col(idx).asInstanceOf[VectorI].toSeq:_*).size
              case 'S' => HashSet(rel.col(idx).asInstanceOf[VectorS].toSeq:_*).size
              case 'D' => HashSet(rel.col(idx).asInstanceOf[VectorD].toSeq:_*).size
            }
            size
          }).toList
        )
      }
    }

    metadata += (
      ("#features", "" + predictorCols.size),       // # of predictor attributes in the dataset
      ("#instances", "" + rel.rows),                // # of instances in the dataset
      ("meanMeans", "" + numericMeans(0)),                 // mean means of all numeric attributes
      ("meanStddev", "" + numericMeans(1)),                // mean stddev of all numeric attributes
      ("meanKurtosis", "" + numericMeans(2)),              // mean kurtosis of all numeric attributes
      ("meanSkew", "" + numericMeans(3)),                  // mean skewness of all numeric attributes
      ("percentNumericAttr", "" + percNumAttr),     // percentage of numeric attributes
      ("percentNominalAttr", "" + percNomAttr),     // percentage of nominal attributes
      ("percentBinaryAttr", "" + percBinAttr),     // percentage of binary attributes
      ("maxNominalDistinctVals", "" + nominalDistinctVals.toList.max),     // Max. Nominal Att. Distinct Values
      ("minNominalDistinctVals", "" + nominalDistinctVals.toList.min),     // Min. Nominal Att. Distinct Values
      ("meanNominalDistinctVals", "" + nominalDistinctVals.mean),     // Mean Nominal Att. Distinct Values
      ("stddevNominalDistinctVals", "" + nominalDistinctVals.stddev),     // Stddev Nominal Att. Distinct Values
      ("df", "" + df),                              // degrees of freedom
      ("rDf", "" + (rel.rows - 1) * 1.0 / df),             // ratio of degrees of freedom
      ("stddevMeanRatioResponse", "" + response.stddev * 1.0 / response.mean)             // Ratio of the standard deviation to the mean of the target attribute
    )

    metadata ++=  extractMissingValueFeatures

    metadata.toMap
  }

  def extractMissingValueFeatures : Map[String, String] = {
    val schemalessRel = Relation(path, name, -1, null, eSep)
    val missingCounts = schemalessRel.colName.map(colName =>{
      schemalessRel.selectS(colName, (x: StrNum) => x == Datasets.missingValueString).size
    })

    val numAttrsWithMissingVals = missingCounts.filter(_ > 0).size
    val numAttrsWithMoreThanHalfMissingVals  = missingCounts.count(_ > schemalessRel.rows * 1.0 / 2)
    Map(
      ("#instancesWithMissingVals", "" + missingCounts.max),
      ("#attrsWithMissingVals", "" + missingCounts.filter(_ > 0).size),
      ("%attrsWithMissingVals", "" + numAttrsWithMissingVals * 1.0 / missingCounts.size ),
      ("#attrsWithMoreThanHalfMissingVals", "" + numAttrsWithMoreThanHalfMissingVals),
      ("%AttrsWithMoreThanHalfMissingVals", "" + numAttrsWithMoreThanHalfMissingVals * 1.0 / missingCounts.size)
    )
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

object MissingValTest extends App{
  val airQuality = Relation ("/home/mnural/research/project_root/analytics/examples" + File.separator + "air_quality" + File.separator + "AirQualityUCI.csv", "air_quality", -1, null, ";")
  val missingCounts = airQuality.colName.map(colName =>{
    airQuality.selectS(colName, (x: StrNum) => x != Datasets.missingValueString).size
  })
  val numInstancesWithMissingVals = missingCounts.max
  val numAttrsWithMissingVals = missingCounts.filter(_ == 0).size
  val percAttrsWithMissingVals = numAttrsWithMissingVals / missingCounts.size

  val numAttrsWithMoreThanHalfMissingVals  = missingCounts.count(_ > airQuality.rows / 2)
  val percAttrsWithMoreThanHalfMissingVals = numAttrsWithMoreThanHalfMissingVals / missingCounts.size

  val airQuality2 = MakeSchema(airQuality)

  println("End")
}

object VariableTypesTest extends App{
  val mpg = Datasets("auto-mpg")
  val attrs : mutable.Map[Int, String] = mutable.Map()

  mpg.predictorCols.foreach( idx => {
    val domain = mpg.rel.domain(idx)
    val vec =
    domain match {
      case 'I' => mpg.rel.toVectorI(idx)
      case 'D' => mpg.rel.toVectorD(idx)
      case _ => mpg.rel.toVectorS(idx)
    }
    val variableType = VariableTypes.inferVariableType(vec, domain)
    if (VariableTypes.numeric.contains(variableType)){
      attrs += ((idx, "numeric"))
    }else if (variableType == VariableTypes.Binary){
      attrs += ((idx, "binary"))
    }else if (VariableTypes.nominal.contains(variableType)){
      attrs += ((idx, "nominal"))
    }
  })
  println(attrs)
}

object MetadataTest extends App{
  Datasets.datasets.foreach((tuple: (String, Dataset)) => {
    val dataset = tuple._2
    val predictors = VectorD.one(dataset.predictors.dim1) +^: dataset.predictors.asInstanceOf[MatrixD]
    val reg = new Regression(predictors, dataset.response)
    reg.train()
    println(tuple._1 + ":" + reg.fit)
    println(dataset.metadata)
  })


//  val mpg = Datasets("auto-mpg")
//  println(mpg.metadata)
//  println("\n\n")
//  mpg.metadata.foreach( item => println(item._1 + ":" + item._2))
}
