package main.scala.sample.components

import scala.collection.mutable.ListBuffer
import scala.swing.{Button, CheckBox, Component, Dimension, Frame, GridPanel}
import scala.swing.event.ButtonClicked

class FilterFrame(columnNames: Seq[String], mainFunction: Seq[String] => Unit) extends Frame {

  title = "Filter Columns"
  preferredSize = new Dimension(360, 25 * (columnNames.size + 1))
  resizable = false
  private val checkList = ListBuffer.empty[CheckBox]
  contents = new GridPanel(columnNames.size + 1, 1) {

    for (col <- columnNames) {
      val colCheckbox = new CheckBox(col)
      contents += colCheckbox
      checkList += colCheckbox
    }

    val applyButton: Component = new Button("Apply") {
      reactions += {
        case ButtonClicked(_) => {
          val selectedCols = checkList.filter(cb => cb.selected).map(cb => cb.text).toSeq
          mainFunction(selectedCols)
          close()
        }
      }
    }
    contents += applyButton
  }

  def start(): Unit = {
    visible = true
    pack()
    open()
  }

}
