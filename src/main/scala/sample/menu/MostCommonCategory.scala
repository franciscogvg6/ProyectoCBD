package main.scala.sample.menu


import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, FilterFrame, LoadingDataFrame}
import org.apache.spark.sql.functions.{avg, col, sum}
import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.io.Source

import java.awt.{Color, Toolkit}
import javax.swing.SwingWorker
import scala.swing.{Dimension, Frame, Point, Swing}

class MostCommonCategory extends MenuOption {

  override def toString(): String = {
    "Most Common Categories"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(MostCommonCategory.this.toString())
    window.start()

    val worker = new SwingWorker[DataFrame, Unit] {
      override def doInBackground(): DataFrame = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: DataFrame = get()
        val lista1 = new DataFrameTable(dataTable, dataTable.columns)
        window.preferredSize = new Dimension( 1080, 720)
        window.location = new Point(0, 0)
        window.contents = lista1
        window.pack()
      }
    }
    worker.execute()


  }


 private def processDataFrame(): DataFrame = {

    val data = Main.data
      .groupBy("Category").count().as("Count")

    val mostCommon = data.filter(col("Count")>1).sort(col("Count").desc)
    mostCommon
  }

}



