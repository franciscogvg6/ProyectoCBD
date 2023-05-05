package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, FilterFrame, LoadingDataFrame}
import main.scala.sample.menu.MenuOption
import org.apache.spark.sql.functions.{avg, col, sum}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.io.Source
import java.awt.{Color, Toolkit}
import javax.swing.SwingWorker
import scala.swing.{Dimension, Frame, Label, Point, Swing, TextField}

class PercentageFreeApps extends MenuOption{

  override def toString(): String = {
    "Percentage of Free Apps"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(PercentageFreeApps.this.toString())
    window.start()

    val worker = new SwingWorker[Double, Unit] {
      override def doInBackground(): Double = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: Double = get()

        window.preferredSize = new Dimension(1080, 720)
        window.location = new Point(0, 0)
        val dataTableString: String = dataTable.toString()
        window.contents = new TextField(dataTableString)
        window.pack()

      }
    }
    worker.execute()
  }

  private def processDataFrame(): Double = {

    val data = Main.data
      .filter(col("Free") === "True").count()


    val total = Main.data.count()

    val percentage = data.toDouble / total * 100

    println(s"El porcentaje de aplicaciones gratuitas es: $percentage%")
    percentage
  }
}