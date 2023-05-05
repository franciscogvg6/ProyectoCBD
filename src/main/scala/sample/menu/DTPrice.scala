
package main.scala.sample.menu

import breeze.linalg.InjectNumericOps
import breeze.linalg.Matrix.castOps
import breeze.linalg.Vector.castFunc
import breeze.numerics.abs
import breeze.stats.mean
import main.scala.sample.Main
import main.scala.sample.components.LoadingDataFrame
import org.apache.spark.sql.functions.{avg, col}
import org.apache.spark.sql.functions._

import javax.swing.SwingWorker
import scala.swing.MenuBar.NoMenuBar.{border, contents}
import scala.swing.{BoxPanel, Dimension, Label, Orientation, Point, Swing, TextField}

class DTPrice extends MenuOption{

  override def toString(): String = {
    "Typical Deviation of prices"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(DTPrice.this.toString())
    window.start()

    val worker = new SwingWorker[Double, Unit] {
      override def doInBackground(): Double = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: Double = get()
        window.contents = new BoxPanel(Orientation.Vertical) {
          border = Swing.EmptyBorder(10)
          contents += new Label(f"La desviación típica de aplicaciones de pago es de: $dataTable%1.2f")
        }
        window.pack()

      }
    }
    worker.execute()
  }

  private def processDataFrame(): Double = {

    val data = Main.data
      .filter(col("Free") === "False")

    //val desviacionMedia = data.agg(Map("Price" -> "stddev")).first().getDouble(0)

    val desviacionMedia2 = data.agg(stddev("Price")).first().getDouble(0)


    desviacionMedia2
  }

}

