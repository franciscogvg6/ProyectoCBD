/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main.scala.sample

import org.apache.spark.sql.Row

import scala.io.StdIn
import scala.reflect.ClassTag.Any
// $example on:init_session$
import org.apache.spark.sql.SparkSession
// $example off:init_session$
// $example on:programmatic_schema$
// $example on:data_types$
import org.apache.spark.sql.types._
// $example off:data_types$
// $example off:programmatic_schema$
import org.apache.log4j.Logger
import org.apache.log4j.Level
import menu._

// Need Spark version >= v2.1
object Main {

  // $example on:create_ds$
  case class Person(name: String, age: Long)
  // $example off:create_ds$

  def main(args: Array[String]) {
    // Disable logging
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    // $example on:init_session$
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .config("spark.master", "local")
      .getOrCreate()

    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._
    // $example off:init_session$

    println("Welcome to Google App Workbench!")

    println("Loading data...")
    val df = spark.read.option("header",true).csv("data/Google-Playstore.csv")

    val menu = new Menu()

    var continue = true;
    while(continue) {
      val selectedOption = menu.askOption()

      clearConsole()
      menu.getOption(selectedOption).start(spark,df)
      clearConsole()

      continue = askContinue(spark)
    }
    //runBasicDataFrameExample(spark)
    //runDatasetCreationExample(spark)

    spark.stop()
  }

  private def askContinue(spark: SparkSession): Boolean = {
    val yeses = Seq("y", "yes")
    val noses = Seq("n", "no")

    print("Would you like to do another task? (Y/N): ")
    var cont = StdIn.readLine()
    while (!yeses.contains(cont.toLowerCase()) && !noses.contains(cont.toLowerCase())) {
      print("Would you like to do another task? (Y/N): ")
      cont = StdIn.readLine()
    }

    if (yeses.contains(cont.toLowerCase())) {
      clearConsole()
      true
    } else {
      false
    }
  }
  private def clearConsole(): Unit = {
    print("\n\n")
  }

  private def runBasicDataFrameExample(spark: SparkSession): Unit = {
    // $example on:create_df$
    val df = spark.read.json("data/__default__/example/data/people.json")

    // Displays the content of the DataFrame to stdout
    df.show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 22|Ricky|
    // | 36| Jeff|
    // | 62|Geddy|
    // +---+-----+
    // $example off:create_df$

    // $example on:untyped_ops$
    // This import is needed to use the $-notation
    import spark.implicits._
    // Print the schema in a tree format
    df.printSchema()
    // root
    // |-- age: long (nullable = true)
    // |-- name: string (nullable = true)

    // Select only the "name" column
    df.select("name").show()
    // +-----+
    // | name|
    // +-----+
    // |Ricky|
    // | Jeff|
    // |Geddy|
    // +-----+

    // Select everybody, but increment the age by 1
    df.select($"name", $"age" + 1).show()
    // +-----+---------+
    // | name|(age + 1)|
    // +-----+---------+
    // |Ricky|       23|
    // | Jeff|       37|
    // |Geddy|       63|
    // +-----+---------+

    // Select people older than 40
    df.filter($"age" > 40).show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 62|Geddy|
    // +---+-----+

    // Count people by age
    df.groupBy("age").count().show()
    // +---+-----+
    // |age|count|
    // +---+-----+
    // | 22|    1|
    // | 62|    1|
    // | 36|    1|
    // +---+-----+
    // $example off:untyped_ops$

    // $example on:run_sql$
    // Register the DataFrame as a SQL temporary view
    df.createOrReplaceTempView("people")

    val sqlDF = spark.sql("SELECT * FROM people")
    sqlDF.show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 22|Ricky|
    // | 36| Jeff|
    // | 62|Geddy|
    // +---+-----+
    // $example off:run_sql$

    // $example on:global_temp_view$
    // Register the DataFrame as a global temporary view
    df.createGlobalTempView("people")

    // Global temporary view is tied to a system preserved database `global_temp`
    spark.sql("SELECT * FROM global_temp.people").show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 22|Ricky|
    // | 36| Jeff|
    // | 62|Geddy|
    // +---+-----+

    // Global temporary view is cross-session
    spark.newSession().sql("SELECT * FROM global_temp.people").show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 22|Ricky|
    // | 36| Jeff|
    // | 62|Geddy|
    // +---+-----+
    // $example off:global_temp_view$
  }

  private def runDatasetCreationExample(spark: SparkSession): Unit = {
    import spark.implicits._
    // $example on:create_ds$
    // Encoders are created for case classes
    val caseClassDS = Seq(Person("Andy", 32)).toDS()
    caseClassDS.show()
    // +----+---+
    // |name|age|
    // +----+---+
    // |Andy| 32|
    // +----+---+

    // Encoders for most common types are automatically provided by importing spark.implicits._
    val primitiveDS = Seq(1, 2, 3).toDS()
    primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)

    // DataFrames can be converted to a Dataset by providing a class. Mapping will be done by name
    val path = "data/__default__/example/data/people.json"
    val peopleDS = spark.read.json(path).as[Person]
    peopleDS.show()
    // +---+-----+
    // |age| name|
    // +---+-----+
    // | 22|Ricky|
    // | 36| Jeff|
    // | 62|Geddy|
    // +---+-----+
    // $example off:create_ds$
  }
}