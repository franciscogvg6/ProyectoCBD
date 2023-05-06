package main.scala.sample.menu
import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, FilterFrame, LoadingDataFrame}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.DataFrame

import java.awt.{Color, Toolkit}
import javax.swing.SwingWorker
import scala.swing.{Dimension, Frame, Point, Swing}

class ListRatingCount extends MenuOption {

  override def toString() : String = {
    "List Best Apps By Ratings"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq

    val filterFrame = new FilterFrame(columnNames, loadTable)
    filterFrame.start()
  }

  private def loadTable(selectedCols: Seq[String]): Unit = {
    val window = new LoadingDataFrame(ListRatingCount.this.toString(),"Loading data...")
    window.start()

    val worker = new SwingWorker[DataFrame, Unit] {
      override def doInBackground(): DataFrame = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: DataFrame = get()
        val lista1 = new DataFrameTable(dataTable, selectedCols)
        lista1.border = Swing.CompoundBorder(
          Swing.EmptyBorder(10),
          Swing.LineBorder(Color.BLACK))
        val screenSize = Toolkit.getDefaultToolkit.getScreenSize
        window.preferredSize = new Dimension(screenSize.width, 720)
        window.location = new Point(0, 0)
        window.contents = lista1
        window.pack()
      }
    }
    worker.execute()
  }

  private def processDataFrame(): DataFrame = {
    val filterEditors = Main.data
      .filter(col("Editors Choice") === "True")
    val sortedRating = filterEditors
      .sort(col("Rating").cast("double").desc, col("Rating Count").cast("long").desc)
    sortedRating
  }
}

/*
val filterEditors = Main.data
  .select("App Name", "App Id", "Category", "Rating", "Rating Count", "Developer Id")
  .filter(col("Editors Choice") === "True")
val sortedRating = filterEditors
  .sort(col("Rating").cast("double").desc, col("Rating Count").cast("long").desc)

println("List of Best Apps by Rating Proportion (Ratings/Rating Count)")
sortedRating.show()

val developersByAvgRating = Main.data
  .select("App Name", "App Id", "Category", "Rating", "Rating Count", "Developer Id")
  .groupBy(col("Developer Id"))
  .agg(avg(col("Rating")).as("AvgRating"),sum(col("Rating Count")).as("RatingCont"))
  .sort(col("AvgRating").desc,col("RatingCont").desc)

println("List of Best Developers by Average Ratings")
developersByAvgRating.show()
 */