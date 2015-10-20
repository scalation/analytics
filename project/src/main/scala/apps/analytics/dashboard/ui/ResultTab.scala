package apps.analytics.dashboard.ui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.layout.{GridPane, HBox, StackPane, VBox}
import javafx.scene.text.TextAlignment
import javax.swing.SwingUtilities

import apps.analytics.dashboard.model.{Model, ModelRuntime}
import play.api.libs.json.{JsDefined, Json, JsValue}

import scala.math._
import scalation.plot.FramelessPlot
import scalation.random.{Normal, Quantile}
import scalation.stat.{FramelessHistogram, GoodnessOfFit, Q_Q_Plot}

/**
 * Created by mnural on 10/9/15.
 */
class ResultTab (val modelRuntime: ModelRuntime, conceptualModel : Model) extends Tab{
  setId("resultTab")
  val stack = new StackPane()
  stack.setId("resultTabContent")

  val scrollPane = new ScrollPane()
  scrollPane.setFitToWidth(true)
  scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)

  val contents = new VBox()
  contents.getStyleClass.add("padded-vbox")

  val lightBox = new VBox()
  lightBox.getStyleClass.add("transparent-background")

  val progressBox = new VBox()
  progressBox.getStyleClass.add("white-background")
  progressBox.setAlignment(Pos.CENTER)
  progressBox.setMaxWidth(240)
  progressBox.setMaxHeight(160)

  val progressMessage = new Label()
  progressMessage.setWrapText(true)
  progressMessage.setTextAlignment(TextAlignment.CENTER)

  val cancelButton = new Button("Cancel")

  var task : Task[Void] = null

  setContent(stack)

  def init = {
    val tabPaneInsets = getTabPane.getInsets

    stack.setPrefWidth(getTabPane.getWidth - tabPaneInsets.getLeft - tabPaneInsets.getRight)
    stack.setPrefHeight(getTabPane.getHeight - tabPaneInsets.getTop - tabPaneInsets.getBottom)

    scrollPane.setContent(contents)

    val progress: ProgressIndicator = new ProgressIndicator(-1)

    progressBox.getChildren.addAll(progress, progressMessage, cancelButton)

    stack.getChildren().addAll(scrollPane, lightBox, progressBox)


    val resultsLabel = new Label("Diagnostics")
    resultsLabel.getStyleClass.add("title")
    resultsLabel.setAlignment(Pos.CENTER)
    contents.getChildren.add(resultsLabel)
    contents.setPrefWidth(getTabPane.getWidth - 25)

    val fitGrid = new GridPane()
    val fitLabel = new Label("Fit")
    fitLabel.getStyleClass.add("title")

    val coefficientGrid = new GridPane()
    val coefficientLabel = new Label("Coefficients")
    coefficientLabel.getStyleClass.add("title")

    val gofLabel = new Label("Do Residuals Pass Chi-Squared Goodness of Fit Test?  ")
    val gofValue = new Label("")

    task = new Task[Void]{

      def createReportNode(jsonReport: JsValue) : VBox = {
        val container = new VBox()
        val fit = jsonReport.\("fit")
        fit.productIterator.foreach( p => {
          val jsValue = p.asInstanceOf[JsDefined].value

        })
        val coefficients = jsonReport.\("coefficients")
        val label = new Label(Json.prettyPrint(jsonReport))
        container.getChildren.add(label)
        container
      }

      override def call(): Void = {
        updateMessage("Preparing Dataset for Execution")
        val predictor = modelRuntime.predictor
        if (isCancelled) { return null }

        updateMessage("Executing Model")
        predictor.train()
        if (isCancelled) { return null }

        updateMessage("Finished Executing Model, Printing Results")

        val report = createReportNode(predictor.jsonReport)

        val labeledParams = predictor.fitLabels.zip(predictor.fit.toList)
        labeledParams.indices.foreach(i => {
          fitGrid.add(new Label(labeledParams(i)._1), 0, i)
          fitGrid.add(new Label(labeledParams(i)._2.toString), 1, i)
        })

//        Platform.runLater(() => { update(fitLabel, fitGrid)})

        val labeledCoefficients = conceptualModel.variables
          .filterNot(v => {
            v.isResponse || v.ignore
          })
          .map(_.label)
          .zip(predictor.coefficient.toList)

        labeledCoefficients.indices.foreach(i => {
          coefficientGrid.add(new Label(labeledCoefficients(i)._1), 0, i)
          coefficientGrid.add(new Label(labeledCoefficients(i)._2.toString), 1, i)
        })

//        Platform.runLater(() => { update(coefficientLabel, coefficientGrid) })

        fitLabel.setText(predictor.reportToString)
        fitLabel.setStyle("-fx-font-family:monospace")
        fitLabel.setWrapText(true)

        Platform.runLater(()=> update(fitLabel))

        updateMessage("Performing Post Execution Analysis\nCreating QQ-Plot")

        val residuals = predictor.residual
        val dmin  = residuals.min ()         // the minimum
        val dmax  = residuals.max ()         // the minimum
        val dmu   = residuals.mean           // the mean
        val dsig2 = residuals.variance       // the variance
        val dsig  = sqrt (dsig2)

//        val interval = sqrt(residuals.dim).toInt
        val interval = sqrt(residuals.dim).toInt
        val gof = new GoodnessOfFit(residuals, dmin , dmax, interval)
        val fit = gof.fit(new Normal(dmu, dsig2))
        gofValue.setText(if (fit) "Yes" else "No")
        gofValue.getStyleClass.add("bold")
        val hbox = new HBox()
        hbox.getChildren.addAll(gofLabel, gofValue)
        Platform.runLater(() => { contents.getChildren.add(hbox) })

        contents.setPrefWidth(getTabPane.getWidth - 25)
        //        contents.getChildren.remove(runButton)

        Q_Q_Plot.frameless = true
        val plotWidth : Int = (contents.getPrefWidth - contents.getPadding.getLeft - contents.getPadding.getLeft).toInt
        val plotHeight = 480
        val plot : FramelessPlot = Q_Q_Plot.plot(predictor.residual, Quantile.normalInv, Array ())
        plot.width = plotWidth.toInt
        plot.height = plotHeight

        if (isCancelled) { return null }

        //    val plot = new FramelessPlot(predictor.residual, predictor.residual)

        val qqNode = new MySwingNode(plotWidth, plotHeight)
        qqNode.setId("swingNode")
        SwingUtilities.invokeLater(() => { qqNode.setContent(plot.canvas) } )

        if (isCancelled) { return null }

        val plotLabel = new Label("Q-Q Plot of Residuals")
        plotLabel.getStyleClass.add("title")
//        Platform.runLater(() => { contents.getChildren.addAll(plotLabel, qqNode) } )

        if (isCancelled) { return null }

        updateMessage("Performing Post Execution Analysis\nCreating Histogram")

        val hist = new FramelessHistogram(plotWidth, plotHeight, predictor.residual, interval)

        if (isCancelled) { return null }

        val histNode = new MySwingNode(plotWidth, plotHeight)
        histNode.setId("swingNode")

        SwingUtilities.invokeLater(() => { histNode.setContent(hist.canvas) } )

        val histLabel = new Label("Histogram of Residuals")
        histLabel.getStyleClass.add("title")

        if (isCancelled) { return null }

        Platform.runLater(() => {contents.getChildren.addAll(histLabel, histNode)})

        updateMessage("Completed Post Execution Analysis. Displaying Results")
        null
      }
    }

    task.setOnScheduled(e => {
      getTabPane.getSelectionModel.select(this)
      getContent.requestFocus()
    })

    task.setOnSucceeded(e => {
      stack.getChildren.removeAll(lightBox, progressBox)
    })

    task.setOnCancelled(e => {
      stack.getChildren.removeAll(lightBox, progressBox)
    })

    task.setOnFailed(e => {
      task.getException.printStackTrace()
      stack.getChildren.removeAll(lightBox, progressBox)
    })
    progressMessage.textProperty.bind(task.messageProperty)
    cancelButton.setOnAction(event => {
      task.cancel()
    })


  }

  def run() = {
    val thread: Thread = new Thread(task)
    thread.setDaemon(true)
    thread.start()
  }

  def update(elements : Node* ) = {
    contents.getChildren.addAll(elements:_*)
  }

}
