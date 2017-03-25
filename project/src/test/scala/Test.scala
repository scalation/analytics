import java.io.File
import java.util.Scanner
import javafx.application.Application
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javax.swing.SwingUtilities

import play.api.libs.json._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.xml.XML
import scalation.analytics.Regression
import scalation.analytics.classifier.LogisticRegression
import scalation.linalgebra.{MatrixD, VectorD}
import scalation.random.CDF._
import scalation.random.{Normal, Random}
import scalation.relalgebra.{MakeSchema, Relation}
import scalation.stat.{GoodnessOfFit_KS, Histogram}

/**
 * Created by mnural on 10/5/15.
 */
class Test extends Application {

  def createAndSetSwingContent(swingNode: SwingNode) = {

  }

  override def start(primaryStage: Stage): Unit = {

    val pane: VBox = new VBox()

    primaryStage.setScene(new Scene(pane, 740, 580))
    primaryStage.show
    val button = new Button()
    button.setText("Create Plot")
    val swingNode: SwingNode = new MySwingNode
    button.setOnAction((event: ActionEvent) => {
      SwingUtilities.invokeLater(new Runnable() {
        def run {
//          val plot = Q_Q_Plot.plot(Quantile.normalInv, Array(10), Quantile.normalInv, new Array(10), 50)
//          swingNode.setContent(plot.canvas)
//          plot.canvas.repaint()
        }
      })
//      swingNode.resize(640, 480)
    })
    pane.getChildren.addAll(button, swingNode)


  }

  class MySwingNode extends SwingNode{
    override def minWidth(width :Double) = { 640}
    override def minHeight(height :Double) = { 480}
  }

}
object Test{
  def main(args: Array[String]) {
    val file = new File ("../examples/auto_mpg.csv")
    println(file.getAbsolutePath)
    println(file.exists())
//    Application.launch(classOf[Test], args: _*)
  }
}


//object Test2 extends App{
//  def isInteger (str : String) : Boolean = {
//    if (str == null) {
//      return false
//    }
//    val length = str.length();
//    if (length == 0) {
//      return false
//    }
//    var i = 0
//    if (str.charAt(0) == '-') {
//      if (length == 1) {
//        return false
//      }
//      i = 1
//    }
//    for (j <- i until length) {
//      val c = str.charAt(i)
//      if (c <= '/' || c >= ':') {
//        return false
//      }
//    }
//    true
//  }
//
//  val file = new File("../examples/3d_road/3D_spatial_network.csv")
//  (0 until 10).foreach(_ =>{
//
//    val lines = Source.fromFile(file).getLines()
//
//    var flag = false
//      var start : Long = System.currentTimeMillis()
//      println("ParseInt" )
//      lines.foreach(line => {
//        val values =  line.split(",")
//        values.foreach(value => {
//          try{
//            value.toInt
//            flag = true
//          }catch {
//            case e: NumberFormatException => {
//              flag = false
//            }
//          }
//        })
//      })
//      println("ParseInt took " + (System.currentTimeMillis() - start)  )
//
//    val lines2 = Source.fromFile(file).getLines()
//    start = System.currentTimeMillis()
//    println("Is Integer")
//    lines2.foreach(line => {
//      val values = line.split(",")
//      values.foreach(value => {
//        if (isInteger(value)) flag = true
//        else flag = false
//      })
//    })
//    println("IsInteger took " + (System.currentTimeMillis() - start))
//
//    val lines3 = Source.fromFile(file).getLines()
//      start= System.currentTimeMillis()
//      println("Regex" )
//      val intRegex = "[\\-\\+]?\\d+".r
////      val pattern = new Pattern(intPattern)
//      lines3.foreach(line => {
//        val values =  line.split(",")
//        values.foreach(value => {
//          if (intRegex.pattern.matcher(value).matches()) flag = true
//          else flag = false
//        })
//      })
//      println("Regex took " + (System.currentTimeMillis() - start)  )
//  })
//}

object Test3 extends App {
  val n = 434874
  val pv = new VectorD (n)
  for (i <- 1 until n) {
    val p  = i / n.toDouble
    pv(i-1) = p
//    if (DEBUG) println ("pv = " + pv + ", fv = " + fv(i-1) + ", gv = " + gv(i-1))
    if(i % 1000 == 0 ) {
      println(i)
    }
  } // for
}

object Test4 extends App{
  import scalation.random.Quantile.normalInv
  for (y <- 1 until 20 ){
    println(normalInv(y/20.0))
  }
}

object Test5 extends App{
  val rng = new Random()
  val hist = VectorD(
  for (i <- 0 until 10000) yield{
    var sum = 0.0
    for (k <- 0 until 10){
      sum += rng.gen
    }
    sum
  })
  new Histogram(hist, 40)
}

object Test6 extends App{
//  builder ++= "Residual stdErr: %.3f on %d degrees of freedom".format (sqrt (sse/(m-k-1.0)), k.toInt) + "\n"
//  builder ++= "Multiple rSquared:  %.4f, Adjusted rSquared:  %.4f".format (rSquared, rBarSq) + "\n"
//  builder ++= "F-statistic: %.3f on %d and %d DF".format (fStat, k.toInt, (m-k-1).toInt) + "\n"
//  builder ++= "Akiake Information Criterion (AIC): %.4f".format (aic) + "\n"
//  builder ++= "Schwarz Criterion (SBIC): %.4f".format (sbic) + "\n"
//  builder ++= "-" * 80 + "\n"
//  builder ++= "Coefficients:" + "\n"
//  builder ++= "        | Estimate   |   StdErr   |  t value | Pr(>|t|)" + "\n"
//  for (j <- 0 until b.dim) {
//    builder ++= "%7s | %10.6f | %10.6f | %8.4f | %9.5f".format ("x" + sub(j), b(j), stdErr(j), t(j), p(j)) + "\n"
//  } // for
  val json: JsValue = JsObject(Seq(
    "fit" -> JsObject(Seq(
      "Degrees of Freedom" -> JsString("df"),
      "Residual Standard Error" -> JsString("number value"),
      "Multiple rSquared" -> JsString("number"),
      "Adjusted rSquared" -> JsString("number"),
      "F-Statistic" -> JsString("number"),
      "Akiake Information Criterion(AIC)"-> JsString("number"),
      "Schwarz Information Criterion(SBIC)" -> JsString("number")
    )),
    "coefficients" -> JsObject(Seq(
      "values" -> JsArray(Seq(
        JsObject(Seq(
          "Variable" -> JsString("mpg"),
          "Estimate" -> JsString("val1"),
          "StdErr" -> JsString("val2"),
          "t value" -> JsString("val3"),
          "Pr(>|t|)" -> JsString("val4")
        )),
        JsObject(Seq(
          "Variable" -> JsString("mpg"),
          "Estimate" -> JsString("val1"),
          "StdErr" -> JsString("val2"),
          "t value" -> JsString("val3"),
          "Pr(>|t|)" -> JsString("val4")
        )),
        JsObject(Seq(
          "Variable" -> JsString("mpg"),
          "Estimate" -> JsString("val1"),
          "StdErr" -> JsString("val2"),
          "t value" -> JsString("val3"),
          "Pr(>|t|)" -> JsString("val4")
        ))
      ))
    ))
  ))

  println(json)
}

object Test7 extends App{
  val scanner = new Scanner (System.in)
  println("Enter first number:")
  val firstNumber = scanner.nextDouble()
  println("Enter second number:")
  val secondNumber = scanner.nextDouble()

  println("firstNumber + secondNumber= " + (firstNumber + secondNumber))

}

object Test8 extends App{
  val relation_ = Relation("/home/mnural/research/project_root/analytics/examples/air/1987_clean.csv", "air", -1, null, ",")
  val relation = MakeSchema(relation_)
  var rel2 = relation
//    .σ("ArrDelay", (el : StrNum) => el!="NA")
//    .σ("DepTime", (el : StrNum) => el!="NA")
    .π("Year","Month","DayofMonth","DayOfWeek","DepTime","CRSDepTime","CRSArrTime","UniqueCarrier","ActualElapsedTime","CRSElapsedTime","ArrDelay","DepDelay","Origin","Dest","Distance")

  import scalation.linalgebra._
  import scalation.stat._

//  val mean = rel2.toVectorD(10).mean
//  val stddev = rel2.toVectorD(10).stddev

//  rel2 = rel2.sigmaI("ArrDelay", (el : Int) => (el - mean).abs < stddev * 3)

//  rel2.writeCSV("/home/mnural/1987_clean_outlier.csv")

  var response : VectorD = rel2.toVectorD(10)
//  response -= response.min()
//  response = response.map((x : Double) => math.log(x + 1.0))
//  val predictors = rel2.toMatriD(rel2.colName.indices diff Seq(7, 10,12,13))
  val predictors = rel2.toMatriD(Seq(0,1,2,3,4,5,6,11,14))

//  val response2 = rel3.toVectorD(10)
//  val predictors2 = rel3.toMatriD(rel2.colName.indices diff Seq(7, 10,12,13))

  new Histogram(response, 100)
//  new Histogram(rel3.toVectorD(10), 100)

//  val mlr =   new Regression(one (predictors.dim1) +^: predictors.asInstanceOf[MatrixD], response)
  val mlr = new Regression(predictors.asInstanceOf[MatrixD], response)
  mlr.train()
//  mlr.report
  println(mlr.fit)

//  val residuals = mlr.residual
//  val dmin  = residuals.min ()         // the minimum
//  val dmax  = residuals.max ()
//
//  val mean = residuals.mean
//  val stddev = residuals.stddev
//
//  val residuals_filtered = residuals.filter((el : Double) => (el - mean ).abs < stddev * 3)
//
//  new Histogram(residuals, 100)
//  new Histogram(residuals_filtered, 100)
//
//  val gof2 = new GoodnessOfFit_CS2(residuals_filtered, residuals_filtered.min ,residuals_filtered.max, Quantile.normalInv)
//  val gof2_fit = gof2.fit()
//  println ("CS2 fit:" + gof2_fit)
//
//  val gof = new GoodnessOfFit_CS(residuals_filtered, residuals_filtered.min, residuals_filtered.max, 100)
//  println ("CS fit: " + gof.fit(new Normal(residuals_filtered.mean, residuals_filtered.stddev)))
//
//  import scalation.random.CDF._
//
//  val ks = new GoodnessOfFit_KS(residuals_filtered)
//  println ("KS Fit: " + ks.fit(normalCDF))

//
//
//  val mlr2 = new Regression(predictors2.asInstanceOf[MatrixD], response2)
//
//  mlr2.train()
//  mlr2.report

//  val gof3 = new GoodnessOfFit__CS2(mlr2.residual, mlr2.residual.min,mlr2.residual.max, Quantile.normalInv)
//  val gof3_fit = gof2.fit()


}

object Test9 extends App{
  import scalation.relalgebra._
  var rel = Relation("/home/mnural/research/project_root/analytics/examples/air/allyears2k.csv", "air", -1, null, ",")
  rel = MakeSchema(rel)

  val response = rel.toVectorD(14)
  var predictors = rel.toMatriD(Seq(0,1,2,3,4,5,7,11)).asInstanceOf[MatrixD]
  predictors = VectorD.one(predictors.dim1) +^: predictors
  val logit = new LogisticRegression(predictors,response.toInt, Array.ofDim(15))
//  logit.train_null()
  logit.train()
  println("ScalaTion ll: " + logit.ll(VectorD(-39039.8,	-7.80091e+07,	-44421.8,	-596084,	-149342,	-5.05944e+07,	-4.99154e+07,	-5.65605e+07,	-2.80258e+07)))
  println("ScalaTion ll: " + logit.ll(VectorD(0.000145389,	0.289378,	0.000632369,	0.00114374,	0.000552108,	0.273234,	0.245230,	0.273672,	0.131442)))
  println("R ll: " + logit.ll(VectorD(5.664e+01, -2.861e-02, 1.600e-01,-1.997e-02, -6.396e-03, 1.212e-03,-1.025e-03, 2.612e-04, 1.587e-04)))
//  println(logit.auc)

  println(logit.test(predictors, response.toInt))
//  println(logit.crossValidate(4))
  println(logit.fit)
}

object Test10 extends App {
  val rand = new Normal()
  val d : VectorD =  VectorD ((0 until 1000).map( _ => rand.gen)  )

  val gof2 = new GoodnessOfFit_KS (d)
  println ("fit = " + gof2.fit (normalCDF))
}

//object AutoMPGTest extends App{
//  import scalation.relalgebra._
//  var rel = Relation("/home/mnural/research/project_root/analytics/examples/auto_mpg.csv", "auto_mpg", -1, null, ",")
//  rel = MakeSchema(rel)
//
//  val response = rel.toVectorD(0)
//  var predictors = rel.toMatriD(Seq(1,2,3,4,5,6)).asInstanceOf[MatrixD]
//  predictors = VectorD.one(predictors.dim1) +^: predictors
//
//  var mlr = new Regression (predictors, response)
//
//  var index : Int = 0
//  var fitNew : VectorD = null
//  var rSquared : Double = 0.0
//  do {
//    mlr.train
//    rSquared = mlr.fit(0)
//    mlr.report
//    val tuple = mlr.backElim()
//    index = tuple._1
//    fitNew = tuple._3
//    println("Next Index: " + index)
//
//    val x = mlr.predictors
//
//    mlr = new Regression (x.selectCols((0 until index) ++: ((index +1) until x.dim2).toArray), response)
//  }while( (rSquared - fitNew(0)) / rSquared < .05 )
//
//
// //mlr.report
//
//  println("Confirmation of Final Model")
//
//  val mlr2 = new Regression (VectorD.one(predictors.dim1) +^: rel.toMatriD(Seq(4,6)).asInstanceOf[MatrixD], response)
//  mlr2.train()
//  mlr2.report
////
////  println(mlr2.backElim())
//}

object SpellingProcessor extends App{
  val doc = XML.loadFile("/home/mnural/inspections/SpellCheckingInspection.xml")

  val rows = doc \\ "problems" \\ "problem"

  val wordMap = new mutable.HashMap[String, ArrayBuffer[String]]()

  rows.foreach( node => {
    val key = (node \\ "description").text.substring(14).replace("'", "").toLowerCase
    val file = (node \\ "file").text.replace("file://$PROJECT_DIR$/scalation/","")
    val line = (node \\ "line").text
    if (!wordMap.contains(key)){
      val arr = new ArrayBuffer[String]()
      wordMap += key -> arr
    }
    wordMap(key) += "Line " + line + " at " + file
  })

  val sortedSet= wordMap.toSeq.sortWith(_._2.length > _._2.length)

  sortedSet.foreach(tuple => {
    println (tuple._1 + " " + tuple._2.length + " times")
//    tuple._2.foreach(file => println("\t" + file))
  })

//  sortedSet.foreach(tuple => {
//    println (tuple._1)
//    tuple._2.foreach(file => println("\t" + file))
//  })
//  val countMap = new mutable.TreeMap[String, Int]()
//
//  wordMap.keys.foreach(key => {
//    countMap += key -> wordMap(key).length
//  })


}