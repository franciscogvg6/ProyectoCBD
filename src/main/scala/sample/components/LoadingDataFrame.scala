package main.scala.sample.components

import main.scala.sample.menu
import main.scala.sample.menu.ListMinInstalledCount

import scala.swing.{BoxPanel, Component, Dimension, Frame, Label, Orientation, Swing}

class LoadingDataFrame(frameTitle: String, loadingText: String) extends Frame {
  title = frameTitle
  preferredSize = new Dimension(400,100)
  contents = new BoxPanel(Orientation.Vertical) {
    border = Swing.EmptyBorder(10)
    contents += new Label(loadingText)
  }

  def start(): Unit = {
    visible = true
    pack
    centerOnScreen()
    open
  }

}
