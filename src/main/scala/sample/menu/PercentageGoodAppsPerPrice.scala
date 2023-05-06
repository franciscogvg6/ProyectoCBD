package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, LoadingDataFrame}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col}
import org.apache.spark.sql.functions._

import javax.swing.SwingWorker
import scala.swing.{Dimension, Point, TextField}

class PercentageGoodAppsPerPrice extends MenuOption {

  override def toString(): String = {
    "Percentage of Good Apps Per Price"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(PercentageGoodAppsPerPrice.this.toString())
    window.start()

    val worker = new SwingWorker[DataFrame, Unit] {
      override def doInBackground(): DataFrame = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: DataFrame = get()
        val lista1 = new DataFrameTable(dataTable, dataTable.columns)
        window.preferredSize = new Dimension(1080, 720)
        window.location = new Point(0, 0)
        window.contents = lista1
        window.pack()

      }
    }
    worker.execute()
  }

  private def processDataFrame(): DataFrame = {

    val data = Main.data
      .select("Price", "Editors Choice", "Free")
      .filter((col("Free")==="False"))
      .filter((col("Price")<10))


    val rangos = data.withColumn("Range",
      when(floor(col("Price")) === ceil(col("Price")), null)
      .otherwise(concat((floor(col("Price"))).cast("int"), lit("-"), (ceil(col("Price"))).cast("int"))))

    val rangosFiltrados = rangos.filter(col("Range").isNotNull)


    val df_grouped = rangosFiltrados
      .groupBy("Rangos de precios")
      .agg(avg(when(col("Editors Choice") === "True", 1).otherwise(0)).alias("Percentage of good apps"))
      .sort(col("Range").asc)

      df_grouped

  }

}
