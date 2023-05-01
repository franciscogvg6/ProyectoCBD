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

import org.apache.spark.sql.{DataFrame}

import org.apache.spark.sql.SparkSession
import org.apache.log4j.Logger
import org.apache.log4j.Level
import menu._


object Main {
  var data : DataFrame = null
  var spark : SparkSession = null
  private def config(): SparkSession = {
    // Disable logging
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val spark = SparkSession
      .builder()
      .appName("SparkCBD")
      .master("local[*]")
      .config("spark.driver.memory", "512m")
      .config("spark.executor.memory", "512m")
      .getOrCreate()

    spark
  }

  def main(args: Array[String]) {

    spark = config()
    val menu = new Menu()
    menu.start()

  }

}