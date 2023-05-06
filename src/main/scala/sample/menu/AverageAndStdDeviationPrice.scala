package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.LoadingDataFrame
import org.apache.spark.sql.functions.{avg, col, stddev}

import javax.swing.SwingWorker
import scala.swing.{BoxPanel, Dimension, Label, Orientation, Point, Swing, TextField}

class AverageAndStdDeviationPrice extends MenuOption{

  override def toString(): String = {
    "Average and Std Deviation of prices"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq
    val window = new LoadingDataFrame(AverageAndStdDeviationPrice.this.toString())
    window.start()

    val worker = new SwingWorker[Seq[Double], Int] {
      override def doInBackground(): Seq[Double] = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: Seq[Double] = get().toSeq
        window.contents = new BoxPanel(Orientation.Vertical) {
          border = Swing.EmptyBorder(10)
          contents += new Label(f"Average price of apps is: ${dataTable(0)}%1.2f €")
          contents += new Label(f"Standard deviation of apps is: ${dataTable(1)}%1.2f")
        }
        window.pack()

      }
    }
    worker.execute()
  }

  private def processDataFrame(): Seq[Double] = {

    val noGratis = Main.data
      .filter(col("Free") === "False")


    val average =  {
      noGratis.agg(Map("Price" -> "avg")).first().getDouble(0)
    }


    val desviacionMedia2 = noGratis.agg(stddev("Price")).first().getDouble(0)


    Seq(average,desviacionMedia2)


  }

}
