package main.scala.sample.menu

import main.scala.sample.Main
import org.apache.spark.sql.functions.{col, lower}
import sample.menu.PredictRatingsAndInstalls

import scala.swing._
import java.awt.Color
import javax.swing.SwingWorker

class Menu() extends MainFrame {
  private val FILENAME = "Google-Playstore.csv"

  background = Color.WHITE
  resizable = false

  //DEFINE MENU CLASSES
  private val search_options : Seq[MenuOption] = Seq(
    new SearchByDeveloper(),
  )

  private val list_options : Seq[MenuOption] = Seq(
    new ListMinInstalledCount(),
    new ListRatingCount(),
    new MostCommonCategory(),
    new PercentageFreeApps(),
    new AverageAndStdDeviationPrice(),
    new PercentageGoodAppsPerPrice(),
  )

  private val predict_options: Seq[MenuOption] = Seq(
    new PredictRatingsAndInstalls(),
  )

  //DEFINE MENU SECTIONS
  private val options : Map[String,Seq[MenuOption]] = Map(
    "Search" -> search_options,
    "Listings" -> list_options,
    "Predictions" -> predict_options,
  )

  //CALCULATE GRID ROWS
  private val options_max_size = options.map(section => section._2.size).max + 1

  def start(): Unit = {

    title = "Loading data..."
    //LOAD DATA FRAME
    val loadProgress = new ProgressBar() {
      indeterminate = true
    }

    contents = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(10)
      contents += new Label("Loading Data...")
      contents += loadProgress
    }
    visible = true
    pack()
    centerOnScreen()
    open()

    //BACKGROUND WORK
    val worker = new SwingWorker[Boolean, Unit] {
      override def doInBackground(): Boolean = {
        try {
          // Read file
          val df = Main.spark.read.option("header", true).csv(s"data/$FILENAME")
          Main.data = df

          Main.devs = df.select("Developer Id")
            .filter(col("Developer Id").isNotNull)
            .distinct()
            .select("Developer Id")
            .repartition(8)
            .sort(lower(col("Developer Id")).asc)

          true

        } catch {
          case e: Throwable => {
            e.printStackTrace()
            false
          }
        }
      }

      override def done(): Unit = {
        var loadResultText = "Done!"
        var buttonText = "Start"
        val result = get()
        if (!result) {
          loadResultText = "The datafile could not be loaded."
          buttonText = "Exit"
        }

        val newLabel = new Label(loadResultText)
        val boxPanel = new GridPanel(2,1) {
          border = Swing.EmptyBorder(10)
          contents += newLabel
        }
        contents = boxPanel

        // START BUTTON
        boxPanel.contents += new Button(buttonText) {
          reactions += {
            case event.ButtonClicked(_) => {
              if (result) {
                loadMenu()
              }
              close()
              if(!result) {
                System.exit(0)
              }
            }
          }
        }
        pack()

      }
    }
    worker.execute()


  }

  private def loadMenu(): Unit = {
    val mainFrame = new Frame {
      title = "Menu"
      resizable = false

      contents = new BoxPanel(Orientation.Vertical) {

        border = Swing.EmptyBorder(10)

        val optionsPanel = new GridPanel(1,3)

        for (functionality <- options) {
          val boxPanel =  new BoxPanel(Orientation.Vertical) {

            contents += new GridPanel(options_max_size, 1) {

              if (functionality._1 != "Load Data") {
                border = Swing.CompoundBorder(Swing.EmptyBorder(0, 2, 5, 2), Swing.LineBorder(Color.BLACK))
                contents += new Label(functionality._1)
              }
              for (o <- functionality._2) {
                contents += new Button(o.toString) {
                  reactions += {
                    case event.ButtonClicked(_) => o.start()
                  }
                }
              }
            }
          }

          // ACCUMULATE SECTIONS
          optionsPanel.contents += boxPanel
        }
        // ADD SECTIONS TO THE MENU
        contents += optionsPanel

        // LASTLY, THE EXIT BUTTON
        val closeButton = new FlowPanel() {
          val button = new Button("Exit") {
            reactions += {
              case event.ButtonClicked(_) => {
                Main.spark.close()
                sys.exit(0)
              }
            }
          }
          contents += new FlowPanel {
            contents += button
          }

        }
        contents += closeButton

      }

    }
    mainFrame.visible = true
    mainFrame.pack()
    mainFrame.centerOnScreen()
    mainFrame.open()
  }

}
