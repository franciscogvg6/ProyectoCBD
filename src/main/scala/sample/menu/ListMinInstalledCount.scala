package main.scala.sample.menu

import org.apache.spark.sql.functions.{avg, col, sum}
import org.apache.spark.sql.{DataFrame, SparkSession}

class ListMinInstalledCount extends MenuOption {
  override def toString(): String = {
    "List Best Apps By Minimum Install Count"
  }

  override def start(spark: SparkSession, data:DataFrame): Unit = {
    val filterEditors = data.filter(col("Editors Choice") === "True")
    val sortedInstalls = filterEditors
//      .filter(col("Minimum Installs") > avgInstalls)
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
