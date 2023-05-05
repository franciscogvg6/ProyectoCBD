package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.LoadingDataFrame
import org.apache.spark.sql.functions.{avg, col}

import javax.swing.SwingWorker
import scala.swing.{BoxPanel, Dimension, Label, Orientation, Point, Swing, TextField}

class AveragePrice extends MenuOption{

  override def toString(): String = {
    "Average of prices"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(AveragePrice.this.toString())
    window.start()

    val worker = new SwingWorker[Double, Unit] {
      override def doInBackground(): Double = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: Double = get()
        window.contents = new BoxPanel(Orientation.Vertical) {
          border = Swing.EmptyBorder(10)
          contents += new Label(f"La media de aplicaciones de pago es: $dataTable%1.2f €")
        }
        window.pack()

      }
    }
    worker.execute()
  }

  private def processDataFrame(): Double = {

    val noGratis = Main.data
      .filter(col("Free") === "False")


    val average =  {
      noGratis.agg(Map("Price" -> "avg")).first().getDouble(0)
    }

    average
  }

}
