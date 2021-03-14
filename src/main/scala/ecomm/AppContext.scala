package ecomm

import cats.effect.{IO, Timer}
import org.apache.spark.sql.SparkSession

class AppContext(val esOptions: Map[String, String])(implicit val spark: SparkSession, implicit val timer: Timer[IO])
