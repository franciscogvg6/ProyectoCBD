package sample.menu

import main.scala.sample.Main
import main.scala.sample.components.LoadingDataFrame
import main.scala.sample.menu.MenuOption
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.{MulticlassClassificationEvaluator, RegressionEvaluator}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.functions.{col, count}

import java.awt.Color
import java.util
import javax.swing.{ImageIcon, SwingWorker}
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, CheckBox, ComboBox, Dimension, FlowPanel, Frame, GridPanel, Label, Orientation, ProgressBar, Swing, TextField}

class PredictRatingsAndInstalls extends MenuOption {

  override def toString: String = {
    "Predict Ratings, Downloads and Editor's Choice"
  }
  override def start(): Unit = {

    // WHAT ARE YOU
    val myappWindow = new Frame {
      title = "What's your app like?"
      //COMPONENTS
      val price = new TextField(16)
      price.minimumSize = new Dimension(240,20)
      price.maximumSize = new Dimension(360,30)
      val contentRatingOptions = Seq("Everyone", "Everyone 10+","Mature 17+","Teen")
      val contentRating = new ComboBox[String](contentRatingOptions)
      contentRating.minimumSize = new Dimension(240, 20)
      contentRating.maximumSize = new Dimension(360, 30)
      val categoryOptions = Main.data.select(col("Category"))
        .groupBy(col("Category"))
        .agg(count(col("Category")).as("CountCat"))
        .filter(col("CountCat") > 1)
        .map(cat => cat.getString(0))(Encoders.STRING)
        .collect()
        .toSeq
      val category = new ComboBox[String](categoryOptions)
      category.minimumSize = new Dimension(240, 20)
      category.maximumSize = new Dimension(360, 30)
      val addSupported = new CheckBox("Are ads supported?")
      val applyButton = new Button("Predict") {
        reactions += {
          case ButtonClicked(_) => {
            val priceValid = checkPrice(price.text)
            if(priceValid == true) {
              close()
              predictRatingsAndInstalls(price.text.toDouble,
                category.selection.item,
                contentRating.selection.item,
                addSupported.selected)
            }else{
              val errorFrame = new ErrorFrame("price")
              errorFrame.pack()
              errorFrame.centerOnScreen()
              errorFrame.visible = true
              errorFrame.open()
            }
          }
        }
      }

      // DISPLAY
      contents = new BoxPanel(Orientation.Vertical) {
        contents += new FlowPanel() {
          contents += new Label("What's your app like?")
        }
        contents += Swing.VStrut(10)

        contents += new FlowPanel() {
          contents += new Label("Price ($): ")
          contents += Swing.HStrut(5)
          contents += price
        }
        contents += Swing.VStrut(10)

        contents += new FlowPanel() {
          contents += new Label("Category: ")
          contents += Swing.HStrut(5)
          contents += category
        }
        contents += Swing.VStrut(10)

        contents += new FlowPanel() {
          contents += new Label("Content Rating: ")
          contents += Swing.HStrut(5)
          contents += contentRating
        }
        contents += Swing.VStrut(10)

        contents += new FlowPanel() {
          contents += addSupported
        }
        contents += Swing.VStrut(10)

        contents += new FlowPanel() {
          contents += applyButton
        }

      }
    }
    myappWindow.visible = true
    myappWindow.centerOnScreen()
    myappWindow.pack()
    myappWindow.open()

  }

  private def predictRatingsAndInstalls(price:Double,category:String,contentRating:String,addsSupported:Boolean): Unit = {
    val resultWindow = new LoadingDataFrame(PredictRatingsAndInstalls.this.toString(),"Processing data...",false)
    resultWindow.start()

    val progressBar = new ProgressBar() {
      min = 0
      max = 100
    }
    resultWindow.contents.head.asInstanceOf[BoxPanel].contents += progressBar

    val worker = new SwingWorker[Seq[Double],Int] {
      override def doInBackground(): Seq[Double] = {
        // LOAD DATA
        val data = Main.data
          .select(col("Price").cast("double"),
            col("Category"),
            col("Content Rating"),
            col("Ad Supported"),
            col("Editors Choice"),
            col("Rating").cast("double"),
            col("Minimum Installs").cast("double"))
          .filter(col("Price").isNotNull)
          .filter(col("Category").isNotNull)
          .filter(col("Content Rating").isNotNull)
          .filter(col("Ad Supported").isNotNull && (col("Ad Supported") === "True" || col("Ad Supported") === "False"))
          .filter(col("Editors Choice").isNotNull && (col("Editors Choice") === "True" || col("Editors Choice") === "False"))
          .filter(col("Rating").isNotNull)
          .filter(col("Minimum Installs").isNotNull)

        publish(12)

        // TRANSFORM STRING INTO DOUBLE FOR THE DECISION TREE
        val dataIndexer = new StringIndexer()
          .setInputCols(Array("Category", "Content Rating", "Ad Supported", "Editors Choice"))
          .setOutputCols(Array("indexedCategory", "indexedContent", "indexedAds", "indexedEditors"))
          .fit(data)

        publish(25)

        // TRANSFORM COLUMNS INTO VECTORS FOR THE DT
        val featureAssembler = new VectorAssembler()
          .setInputCols(Array("Price", "indexedCategory", "indexedContent", "indexedAds"))
          .setOutputCol("assembledFeatures")

        publish(38)

        val indexedData = dataIndexer.transform(data)
        val assembledData = featureAssembler.transform(indexedData)
        // SPLIT THE DATA INTO TRAINING AND TEST
        val Array(trainingData, testData) = assembledData.randomSplit(Array(0.7, 0.3))

        val catCount = assembledData.select(col("indexedCategory")).distinct().count()
        val contCount = assembledData.select(col("indexedContent")).distinct().count()
        val maxCatCont = catCount.max(contCount)

        publish(50)

        val dtEditors = new DecisionTreeClassifier()
          .setLabelCol("indexedEditors")
          .setFeaturesCol("assembledFeatures")
          .setMaxBins(maxCatCont.toInt)
        val modelEditors = dtEditors.fit(trainingData)

        val evaluator = new MulticlassClassificationEvaluator()
          .setLabelCol("indexedEditors")
          .setPredictionCol("prediction")
          .setMetricName("f1")
        val f1_score = evaluator.evaluate(modelEditors.transform(testData))

        publish(63)

        val dtRating = new DecisionTreeRegressor()
          .setLabelCol("Rating")
          .setFeaturesCol("assembledFeatures")
          .setMaxBins(maxCatCont.toInt)
        val modelRating = dtRating.fit(trainingData)

        val evaluatorRating = new RegressionEvaluator()
          .setLabelCol("Rating")
          .setPredictionCol("prediction")
          .setMetricName("rmse")
        val rmse_rating = evaluatorRating.evaluate(modelRating.transform(testData))

        publish(75)

        val dtInstalls = new DecisionTreeRegressor()
          .setLabelCol("Minimum Installs")
          .setFeaturesCol("assembledFeatures")
          .setMaxBins(maxCatCont.toInt)
        val modelInstalls = dtInstalls.fit(trainingData)

        val evaluatorInstalls = new RegressionEvaluator()
          .setLabelCol("Minimum Installs")
          .setPredictionCol("prediction")
          .setMetricName("rmse")
        val rmse_installs = evaluatorInstalls.evaluate(modelInstalls.transform(testData))

        publish(88)

        val sc = Main.spark
        import sc.implicits._

        val predictDF = Seq((price, category.trim, contentRating.trim, addsSupported.toString.capitalize.trim, "True", 0.0, 0.0))
          .toDF("Price", "Category", "Content Rating", "Ad Supported", "Editors Choice", "Rating", "Minimum Installs")

        val indexedPrediction = dataIndexer.transform(predictDF).head()
        val vectorPrediction = Vectors.dense(price, indexedPrediction.getDouble(7), indexedPrediction.getDouble(8), indexedPrediction.getDouble(9))

        val predictionEditors = modelEditors.predict(vectorPrediction)

        val predictionRating = modelRating.predict(vectorPrediction)

        val predictionInstalls = modelInstalls.predict(vectorPrediction)

        publish(99)
        Seq(f1_score,rmse_rating,rmse_installs,predictionEditors,predictionRating,predictionInstalls)
      }

      override def process(chunks: util.List[Int]): Unit = {
        val progress = chunks.get(chunks.size() - 1)
        progressBar.value = progress
      }

      override def done(): Unit = {
        val results = get().toSeq

        //COMPONENTS
        var isEditor = "MAY"
        if(results(3) > 0) {
          isEditor = "MAY NOT"
        }

        val textEditors = new BoxPanel(Orientation.Vertical) {
          val editors = new Label(f"Your application $isEditor appear on Editors Choice.")
          editors.icon = new ImageIcon("src/icons/estrella.png")
          contents += editors
          contents += new Label(f"The F1 score was: ${results(0)}%.4f")
        }

        val textRating = new BoxPanel(Orientation.Vertical) {
          val ratings = new Label(f"Your application is estimated to have a rating of ${results(4)}%.4f.")
          ratings.icon = new ImageIcon("src/icons/estrella.png")
          contents += ratings
          contents += new Label(f"With a RMSE (Std. Dev.) of ${results(1)}%.4f")
        }

        val textInstalls = new BoxPanel(Orientation.Vertical) {
          val installs = new Label(f"Your application is estimated to have ${results(5).toInt} downloads.")
          installs.icon = new ImageIcon("src/icons/estrella.png")
          contents += installs
          contents += new Label(f"With a RMSE (Std. Dev.) of ${results(2)}%.4f")
        }

        val displayTextPanel = new GridPanel(3,1) {
          contents += new BoxPanel(Orientation.Horizontal) {
            border = Swing.LineBorder(Color.BLACK)
            contents += textEditors
          }

          contents += new BoxPanel(Orientation.Horizontal) {
            border = Swing.LineBorder(Color.BLACK)
            contents += textRating
          }

          contents += new BoxPanel(Orientation.Horizontal) {
            border = Swing.LineBorder(Color.BLACK)
            contents += textInstalls
          }
        }

        val mainPanel = new BoxPanel(Orientation.Vertical) {
          border = Swing.EmptyBorder(10)
          contents += displayTextPanel
        }
        resultWindow.preferredSize = null
        resultWindow.contents = mainPanel
        resultWindow.centerOnScreen()
        resultWindow.pack()

      }
    }
    worker.execute()


  }

  private def checkPrice(price: String): Boolean = {
    var result = true

    try {
      val priceD : Double = price.toDouble
      if(priceD < 0){
        result = false
      }
      result
    } catch {
      case e : Throwable => {
        false
      }
    }

  }

}

class ErrorFrame(field:String) extends Frame {
  title = "Error on fields"

  contents = new GridPanel(2,1){
    contents += new Label(s"The field $field has errors.")
    val closeButton = new Button("Close") {
      reactions += {
        case ButtonClicked(_) => {
          close()
        }
      }
    }
    closeButton.icon = new ImageIcon("src/icons/error.png")
    contents += closeButton
  }
}
