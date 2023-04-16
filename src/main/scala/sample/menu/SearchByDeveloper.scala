package main.scala.sample.menu
import org.apache.spark.sql.functions.{col, to_date, unix_timestamp}
import org.apache.spark.sql.types.{DateType, DoubleType, LongType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import spire.implicits.eqOps

import scala.io.StdIn

class SearchByDeveloper extends MenuOption {
  override def start(spark: SparkSession, data: DataFrame): Unit = {
    val devs = data.select("Developer Id").distinct()
    val selectedDev = askDeveloper(devs)
    val sorting = askSorting()

    if(sorting == "Rating"){
      val search = data.filter(col("Developer Id") === selectedDev).sort(col(sorting).cast("double").desc)
      search.show()
    }
    else if(sorting == "Minimum Installs"){
      val search = data.filter(col("Developer Id") === selectedDev).sort(col(sorting).cast("long").desc)
      search.show()
    }
    else if(sorting == "Released"){
      val search = data.filter(col("Developer Id") === selectedDev).sort(unix_timestamp(col(sorting), "MMM d, yyyy").desc)
      search.show()
    }

  }

  override def toString(): String = {
    "Search Apps By Developer"
  }

  private def askDeveloper(devs: DataFrame): String = {
    print("Write which Developer you want: ")
    var dev = StdIn.readLine()
    while(devs.filter(col("Developer Id") === dev.stripMargin).count() < 1){
      println("That Developer does not exist")
      print("Write which Developer you want: ")
      dev = StdIn.readLine()
    }
    dev
  }

  private def askSorting(): String = {
    val acceptedSortings = Seq("Rating","Minimum Installs","Released")

    println("------------------")
    for(s <- acceptedSortings){
      println(s)
    }
    println("------------------")

    print("Select which Sorting would you like: ")
    var sorting = StdIn.readLine()
    while (!acceptedSortings.contains(sorting.stripMargin)) {
      println("That is not an accpeted Sorting")
      print("Select which Sorting would you like: ")
      sorting = StdIn.readLine()
    }
    sorting
  }
}
