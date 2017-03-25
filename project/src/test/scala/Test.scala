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

import scala.io.Source
import scalation.linalgebra.VectorD
import scalation.random.Random
import scalation.relalgebra.Relation
import scalation.stat.Histogram

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


object Test2 extends App{
  def isInteger (str : String) : Boolean = {
    if (str == null) {
      return false
    }
    val length = str.length();
    if (length == 0) {
      return false
    }
    var i = 0
    if (str.charAt(0) == '-') {
      if (length == 1) {
        return false
      }
      i = 1
    }
    for (j <- i until length) {
      val c = str.charAt(i)
      if (c <= '/' || c >= ':') {
        return false
      }
    }
    true
  }

  val file = new File("../examples/3d_road/3D_spatial_network.csv")
  (0 until 10).foreach(_ =>{

    val lines = Source.fromFile(file).getLines()

    var flag = false
      var start : Long = System.currentTimeMillis()
      println("ParseInt" )
      lines.foreach(line => {
        val values =  line.split(",")
        values.foreach(value => {
          try{
            value.toInt
            flag = true
          }catch {
            case e: NumberFormatException => {
              flag = false
            }
          }
        })
      })
      println("ParseInt took " + (System.currentTimeMillis() - start)  )

    val lines2 = Source.fromFile(file).getLines()
    start = System.currentTimeMillis()
    println("Is Integer")
    lines2.foreach(line => {
      val values = line.split(",")
      values.foreach(value => {
        if (isInteger(value)) flag = true
        else flag = false
      })
    })
    println("IsInteger took " + (System.currentTimeMillis() - start))

    val lines3 = Source.fromFile(file).getLines()
      start= System.currentTimeMillis()
      println("Regex" )
      val intRegex = "[\\-\\+]?\\d+".r
//      val pattern = new Pattern(intPattern)
      lines3.foreach(line => {
        val values =  line.split(",")
        values.foreach(value => {
          if (intRegex.pattern.matcher(value).matches()) flag = true
          else flag = false
        })
      })
      println("Regex took " + (System.currentTimeMillis() - start)  )
  })
}

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
  val relation = Relation("/home/mnural/research/project_root/analytics/examples/air/1987.csv", "air", -1, null, ",")


}