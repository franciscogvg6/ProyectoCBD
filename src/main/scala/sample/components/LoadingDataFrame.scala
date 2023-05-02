package main.scala.sample.components

import main.scala.sample.menu
import main.scala.sample.menu.ListMinInstalledCount

import scala.swing.{BoxPanel, Component, Frame, Label, Orientation, Swing}

class LoadingDataFrame(frameTitle: String) extends Frame {
  title = frameTitle
  contents = new BoxPanel(Orientation.Vertical) {
    border = Swing.EmptyBorder(10)
    contents += new Label("Loading data...")
  }

  def start(): Unit = {
    visible = true
    pack
    centerOnScreen()
    open
  }

}
