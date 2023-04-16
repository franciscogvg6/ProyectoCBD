package main.scala.sample.menu

import org.apache.spark.sql.{DataFrame, SparkSession}

trait MenuOption {
  def start(spark: SparkSession, data: DataFrame): Unit
}
