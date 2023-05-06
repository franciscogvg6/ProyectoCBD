package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, FilterFrame, LoadingDataFrame}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.DataFrame

import java.awt.{Color, Toolkit}
import javax.swing.SwingWorker
import scala.swing._

class ListMinInstalledCount extends MenuOption {

  override def toString(): String = {
    "List Best Apps By Minimum Install Count"
  }

  override def start(): Unit = {
    val columnNames = Main.data.columns.toSeq

    val filterFrame = new FilterFrame(columnNames,loadTable)
    filterFrame.start()

  }

  private def loadTable(selectedCols: Seq[String]) : Unit = {
    val window = new LoadingDataFrame(ListMinInstalledCount.this.toString(),"Loading Data...")
    window.start()

    val worker = new SwingWorker[DataFrame, Unit] {
      override def doInBackground(): DataFrame = {
        processDataFrame()
      }

      override def done(): Unit = {
        val dataTable: DataFrame = get()
        val lista1 = new DataFrameTable(dataTable,selectedCols)
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
    val sortedInstalls = filterEditors
      .sort(col("Maximum Installs").cast("long").desc,
        col("Minimum Installs").cast("long").desc)
    sortedInstalls
  }
}

  /*override def start(spark: SparkSession, data:DataFrame): Unit = {
    val filterEditors = data.filter(col("Editors Choice") === "True")
    val sortedInstalls = filterEditors
      .sort(col("Maximum Installs").cast("long").desc,col("Minimum Installs").cast("long").desc)

    println("List of Best Apps by Install Count")
    sortedInstalls.show()

    val groupedByDeveloper = data.groupBy(col("Developer Id"))
      .agg(sum(col("Minimum Installs").cast("long")).as("Suma_Minimum_Installs"),
        sum(col("Maximum Installs").cast("long")).as("Suma_Maximum_Installs"))
      .sort(col("Suma_Maximum_Installs").desc,col("Suma_Minimum_Installs").desc)

    println("List of Developers By Install Count")
    groupedByDeveloper.show()
  }
}
*/
