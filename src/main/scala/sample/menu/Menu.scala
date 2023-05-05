package main.scala.sample.menu

import main.scala.sample.Main

import scala.swing._
import java.awt.Color
import javax.swing.SwingWorker

class Menu() extends MainFrame {
  private val FILENAME = "Google-Playstore.csv"

  background = Color.WHITE
  private var dataLoaded : Boolean = Main.data != null

  //DEFINE MENU CLASSES
  private val search_options : Seq[MenuOption] = Seq(
    new SearchByDeveloper(),
  )

  private val list_options : Seq[MenuOption] = Seq(
    new ListMinInstalledCount(),
    new ListRatingCount(),
    new MostCommonCategory(),
    new PercentageFreeApps(),
    new AveragePrice(),
    new DTPrice(),
    new PercentageGoodAppsPerPrice(),
  )

  private val predict_options: Seq[MenuOption] = Seq.empty[MenuOption]

  //DEFINE MENU SECTIONS
  private val options : Map[String,Seq[MenuOption]] = Map(
    "Search" -> search_options,
    "Listings" -> list_options,
    "Predictions" -> predict_options,
  )

  //CALCULATE GRID ROWS
  private var options_size = options.size
  for (sub_options <- options){
    options_size += sub_options._2.size
  }

  def start(): Unit = {

    title = "Loading data..."
    //LOAD DATA FRAME
    contents = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(10)
      contents += new Label("Loading Data...")
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
          true

        } catch {
          case e: Throwable => {
            false
          }
        }
      }

      override def done(): Unit = {
        var loadResultText = "Done!"
        val result = get()
        if (!result) {
          loadResultText = "The datafile text was not found."
        }

        val boxPanel = contents.head.asInstanceOf[BoxPanel]
        val newLabel = new Label(loadResultText)
        boxPanel.contents += newLabel

        boxPanel.contents += new Button("Close") {
          reactions += {
            case event.ButtonClicked(_) => {
              if (result) {
                loadMenu()
              }
              close()
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
      background = Color.getHSBColor(60,75,90)

      contents = new GridBagPanel() {
        def constraints(x: Int, y: Int,
                        gridwidth: Int = 1, gridheight: Int = 1,
                        weightx: Double = 0.0, weighty: Double = 0.0,
                        fill: GridBagPanel.Fill.Value = GridBagPanel.Fill.None)
        : Constraints = {
          val c = new Constraints
          c.gridx = x
          c.gridy = y
          c.gridwidth = gridwidth
          c.gridheight = gridheight
          c.weightx = weightx
          c.weighty = weighty
          c.fill = fill
          c
        }

        border = Swing.EmptyBorder(10)

        var it = 0
        for (sub_option <- options) {
          val boxPanel =  new BoxPanel(Orientation.Vertical) {
            contents += new GridPanel(sub_option._2.size + 1, 1) {

              if (sub_option._1 != "Load Data") {
                border = Swing.CompoundBorder(Swing.EmptyBorder(0, 2, 5, 2), Swing.LineBorder(Color.BLACK))
                contents += new Label(sub_option._1)
              }
              for (o <- sub_option._2) {
                contents += new Button(o.toString) {
                  reactions += {
                    case event.ButtonClicked(_) => o.start()
                  }
                }
              }
            }
          }
          add(boxPanel,constraints(it,0,1,1,fill=GridBagPanel.Fill.Both))
          it += 1
        }
        val closeButton = new GridPanel(1, 1) {
          contents += new Button("Close") {
            reactions += {
              case event.ButtonClicked(_) => {
                Main.spark.close()
                sys.exit(0)
              }
            }
          }
        }
        add(closeButton,constraints(0,2,options_size,fill=GridBagPanel.Fill.Both))

      }

    }
    mainFrame.visible = true
    mainFrame.pack()
    mainFrame.centerOnScreen()
    mainFrame.open()
  }

}
