import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

//Fichero de prueba

object SparkRDD {

  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder.
      master("local")
      .appName("spark RDD")
      .getOrCreate()

    val jsonFile = sparkSession.read.json("data/__default__/example/data/people.json")
    jsonFile.printSchema()
    jsonFile.show()
  }
}