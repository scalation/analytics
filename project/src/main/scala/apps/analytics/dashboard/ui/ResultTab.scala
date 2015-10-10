package apps.analytics.dashboard.ui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.layout.{GridPane, StackPane, VBox}
import javafx.scene.text.TextAlignment
import javax.swing.SwingUtilities

import apps.analytics.dashboard.model.{Model, ModelRuntime}

import scalation.plot.FramelessPlot
import scalation.random.Quantile
import scalation.stat.Q_Q_Plot

/**
 * Created by mnural on 10/9/15.
 */
class ResultTab (val modelRuntime: ModelRuntime, conceptualModel : Model) extends Tab{

  val stack = new StackPane()

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
    stack.setPrefWidth(getTabPane.getWidth)
    stack.setPrefHeight(getTabPane.getHeight)

    scrollPane.setContent(contents)

    val progress: ProgressIndicator = new ProgressIndicator(-1)

    progressBox.getChildren.addAll(progress, progressMessage, cancelButton)

    stack.getChildren().addAll(scrollPane, lightBox, progressBox)


    val resultsLabel = new Label("Results")
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

    task = new Task[Void]{
      override def call(): Void = {
        updateMessage("Preparing Dataset for Execution")
        val predictor = modelRuntime.predictor
        if (isCancelled) { return null }

        updateMessage("Executing Model")
        predictor.train()
        if (isCancelled) { return null }

        updateMessage("Finished Executing Model, Printing Results")
        val labeledParams = predictor.fitLabels.zip(predictor.fit.toList)
        labeledParams.indices.foreach(i => {
          fitGrid.add(new Label(labeledParams(i)._1), 0, i)
          fitGrid.add(new Label(labeledParams(i)._2.toString), 1, i)
        })

        Platform.runLater(() => { update(fitLabel, fitGrid)})

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

        Platform.runLater(() => { update(coefficientLabel, coefficientGrid) })

        updateMessage("Performing Post Execution Analysis")

        contents.setPrefWidth(getTabPane.getWidth - 25)
        //        contents.getChildren.remove(runButton)

        val plotWidth = contents.getPrefWidth - contents.getPadding.getLeft - contents.getPadding.getLeft
        val plotHeight = 480
        val plot : FramelessPlot = Q_Q_Plot.plot(predictor.residual, Quantile.normalInv, Array ())

        if (isCancelled) { return null }

        //    val plot = new FramelessPlot(predictor.residual, predictor.residual)

        plot.width = plotWidth.toInt
        plot.height = plotHeight
        val swingNode = new MySwingNode(plotWidth, plotHeight)

        swingNode.setId("swingNode")
        SwingUtilities.invokeLater(() => { swingNode.setContent(plot.canvas) } )
        val plotLabel = new Label("Q-Q Plot of Residuals")
        plotLabel.getStyleClass.add("title")
        Platform.runLater(() => { contents.getChildren.addAll(plotLabel, swingNode) } )
        updateMessage("Completed Post Execution Analysis. Displaying Results")
        null
      }
    }

    task.setOnScheduled(e => {
      getTabPane.getSelectionModel.select(this)
    })

    task.setOnSucceeded(e => {
      stack.getChildren.removeAll(lightBox, progressBox)
    })

    task.setOnCancelled(e => {
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
