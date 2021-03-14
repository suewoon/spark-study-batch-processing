package ecomm

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.SparkSession

class BatchProducerRunner extends IOApp with StrictLogging {

  implicit val spark: SparkSession = SparkSession.builder.master("local[*]").getOrCreate()

  override def run(args: List[String]): IO[ExitCode] = {
    val esOptions = Map(
      "es.nodes" -> args(0),
      "es.nodes.wan.only" -> "true",
      "es.nodes.discovery" -> "false",
      "es.nodes.data.only" -> "false",
      "es.read.field.as.array.include" -> "category,manufacturer,products,sku",
      "es.mapping.id" -> "order_id"
    )

    implicit val appContext: AppContext = new AppContext(esOptions)

    val result = for {
      txs <- BatchProducer.idxToTransactions
      until <- BatchProducer.currentInstant
      from = until.minusSeconds(60 * 60 * 24)
      _ <- BatchProducer.fetchOneHourTransactions(txs, from, until)
    } yield (until, from)
    result.map(_ => ExitCode.Success)

  }

}

object BatchProducerRunnerSpark extends BatchProducerRunner

