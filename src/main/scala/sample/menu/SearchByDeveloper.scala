package main.scala.sample.menu

import main.scala.sample.Main
import main.scala.sample.components.{DataFrameTable, LoadingDataFrame}
import org.apache.spark.sql.functions.{avg, col, sum, unix_timestamp}
import org.apache.spark.sql.{DataFrame, Encoders}

import java.awt.Toolkit
import javax.swing.SwingWorker
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, ButtonGroup, ComboBox, Dimension, FlowPanel, GridPanel, Label, Orientation, Point, ProgressBar, RadioButton, Swing}

class SearchByDeveloper extends MenuOption {
  override def toString(): String = {
    "Search Analytics By Developer"
  }
  override def start(): Unit = {

    val window = new LoadingDataFrame(frameTitle = "Loading developer data...",
      loadingText = "Loading developer data...")
    window.start()

    val worker = new SwingWorker[Seq[String],Unit] {
      override def doInBackground(): Seq[String] = {
        Main.devs.collect()
          .map(_.getString(0)).toSeq
      }

      override def done(): Unit = {
        window.title = "Search Panel"

        // COMPONENTS
        val devComboBox = new ComboBox[String](get().toSeq)
        val acceptedSortings = Seq("Rating", "Minimum Installs", "Released")
        val sortingRadios = acceptedSortings.map(sorting => new RadioButton(sorting)).toSeq
        val sortingGroup = new ButtonGroup(sortingRadios: _*)
        sortingGroup.select(sortingRadios.head)
        val continueButton = new Button("Search") {
          reactions += {
            case ButtonClicked(_) => {
              loadSearch(devComboBox.selection.item, sortingGroup.buttons.filter(button => button.selected).head.text)
              window.close()
            }
          }
        }

        // PUTTING COMPONENTS ON SCREEN
        window.contents = new GridPanel(3, 1) {
          border = Swing.EmptyBorder(10)

          contents += new FlowPanel() {
            contents += new Label("Developer: ")
            contents += Swing.HStrut(5)
            contents += devComboBox
          }

          contents += new FlowPanel() {
            contents += new Label("Sort apps by: ")
            sortingRadios.foreach(radio => contents += radio)
          }

          contents += continueButton

        }
        window.preferredSize = null
        window.visible = true
        window.centerOnScreen()
        window.pack()
      }
    }
    worker.execute()
  }

  private def loadSearch(developer: String,sorting: String) : Unit = {

    val window = new LoadingDataFrame(SearchByDeveloper.this.toString(),"Loading data...")
    window.start()

    val worker = new SwingWorker[(DataFrame, DataFrame),Unit] {
      override def doInBackground(): (DataFrame, DataFrame) = {
        val dfAggregations = processDataFrameAnalytics(developer,sorting)
        val dfApps = processDataFrameApps(developer,sorting)
        (dfAggregations,dfApps)
      }

      override def done(): Unit = {
        // COMPONENTS
        val dataTables: (DataFrame, DataFrame) = get()
        val dfAggregations = dataTables._1

        val dfApps = dataTables._2

        // DISPLAY COMPONENTS
        window.contents = new BoxPanel(Orientation.Vertical) {
          contents += new FlowPanel() {
            contents += new Label("Developer Analytics")
          }
          val tableAggregations = new DataFrameTable(dfAggregations,dfAggregations.columns)
          tableAggregations.border = Swing.EmptyBorder(10)
          contents += tableAggregations

          contents += new FlowPanel() {
            contents += new Label("Developer Apps")
          }
          val tableApps = new DataFrameTable(dfApps, dfApps.columns)
          tableApps.border = Swing.EmptyBorder(10)
          contents += tableApps

          val screenSize = Toolkit.getDefaultToolkit.getScreenSize
          window.preferredSize = new Dimension(960, 540)
          window.location = new Point(0, 0)
          window.pack()
        }
      }
    }
    worker.execute()

  }


  private def processDataFrameAnalytics(developerId: String,sorting: String): DataFrame = {
    /*
    GETS:
    - SUM OF MINIMUM INSTALLS
    - SUM OF MAX INSTALLS
    - AVG OF RATINGS
    - SUM OF RATING COUNT
     */
    val groupedByDeveloper = Main.data
      .filter(col("Developer Id") === developerId)
      .groupBy(col("Developer Id"))
      .agg(sum(col("Minimum Installs").cast("long")).as("Minimum Installs Sum"),
        sum(col("Maximum Installs").cast("long")).as("Maximum Installs Sum"),
        avg(col("Rating")).as("Average Rating"),
        sum(col("Rating Count")).as("Rating Count Sum"))

    groupedByDeveloper
  }

  private def processDataFrameApps(developerId: String, sorting: String): DataFrame = {
    val appsByDeveloper = Main.data
      .filter(col("Developer Id") === developerId)
      .select(col("App Name"), col("Category"),
        col("Rating"), col("Rating Count"),
        col("Minimum Installs"), col("Maximum Installs"),
        col("Released"))

    if(sorting == "Rating"){
      val returnDF = appsByDeveloper.sort(col(sorting).cast("double").desc)
      returnDF
    }else if(sorting == "Minimum Installs") {
      val returnDF = appsByDeveloper.sort(col(sorting).cast("long").desc)
      returnDF
    }else if(sorting == "Released") {
      val returnDF = appsByDeveloper.sort(unix_timestamp(col(sorting), "MMM d, yyyy").desc)
      returnDF
    }else {
      null
    }
  }

}



/*
val devs = Main.data.select("Developer Id").distinct()
val selectedDev = askDeveloper(devs)
val sorting = askSorting()

if(sorting == "Rating"){
  val search = Main.data.filter(col("Developer Id") === selectedDev).sort(col(sorting).cast("double").desc)
  search.show()
}
else if(sorting == "Minimum Installs"){
  val search = Main.data.filter(col("Developer Id") === selectedDev).sort(col(sorting).cast("long").desc)
  search.show()
}
else if(sorting == "Released"){
  val search = Main.data.filter(col("Developer Id") === selectedDev).sort(unix_timestamp(col(sorting), "MMM d, yyyy").desc)
  search.show()
}
 */
