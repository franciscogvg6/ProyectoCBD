package main.scala.sample.components

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

import scala.swing.{Alignment, BoxPanel, Label, Orientation, ScrollPane, Table}

class DataFrameTable(dataFrame: DataFrame, selectedCols: Seq[String]) extends BoxPanel(Orientation.Vertical) {
  private val rowData = dataFrame
    .select(selectedCols.map(sc => col(sc)):_*)
    .collect().map(_.toSeq.toArray)

  val entries = rowData.size
  val rowLabel = new Label(s"There are ${entries} entries.")
  rowLabel.horizontalAlignment = Alignment.Left
  contents += rowLabel

  private val table = new Table(rowData, selectedCols) {
    rowHeight = 25
    autoResizeMode = Table.AutoResizeMode.AllColumns
  }

  contents += new ScrollPane(table)
}
