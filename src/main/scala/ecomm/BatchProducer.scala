package ecomm

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.{explode, lit}
import org.apache.spark.sql.types._
import org.elasticsearch.spark.sql.sparkDatasetFunctions

object BatchProducer {

  def idxToTransactions()(implicit appContext: AppContext): IO[Dataset[Transaction]] = {
    import appContext._
    import spark.implicits._
    IO {
      spark.read.format("org.elasticsearch.spark.sql")
        .options(esOptions)
        .load("kibana_sample_data_ecommerce")
        .withColumn("products", explode($"products"))
        .select(
          $"order_id".cast(IntegerType),
          $"order_date".cast(LongType).cast(TimestampType).as("order_ts"),
          $"order_date".cast(LongType).cast(TimestampType).cast(DateType).as("order_date"),
          $"customer_id".cast(IntegerType),
          $"products".getItem("product_id").cast(IntegerType).as("product_id"),
          $"products".getItem("category").cast(StringType).as("product_category"),
          $"products".getItem("base_price").cast(FloatType).as("product_price"),
          $"products".getItem("discount_percentage").cast(FloatType).as("product_discount_rate"),
          $"products".getItem("quantity").cast(IntegerType).as("product_quantity"),
          $"products".getItem("created_on").cast(TimestampType).as("product_created_date")
        )
        .as[Transaction]
    }
  }

  def fetchOneHourTransactions(transactions: Dataset[Transaction],
                               from: Instant,
                               until: Instant)
                              (implicit appContext: AppContext): IO[Unit] = {
    val filteredTxs = filterTransactions(transactions, from, until)
    // 분석 로직이 들어감. 여기서는 단순히 시간 기준으로 자름
    val esIndex = "ecomm_data.order.1d." + from.truncatedTo(ChronoUnit.DAYS).toString.toLowerCase.split(":")(0)
    BatchProducer.saveToIndex(filteredTxs, esIndex)
  }

  def filterTransactions(transactions: Dataset[Transaction],
                         from: Instant, until: Instant): Dataset[Transaction] = {
    import transactions.sparkSession.implicits._
    transactions.filter(($"order_date" >= lit(from.getEpochSecond).cast(TimestampType)) &&
      ($"order_date" < lit(until.getEpochSecond).cast(TimestampType)))
  }

  def saveToIndex(transactions: Dataset[Transaction], index: String)(implicit appContext: AppContext): IO[Unit] = {
    IO(unsafeSaveToIndex(transactions, index))
  }

  def unsafeSaveToIndex(transactions: Dataset[Transaction], index: String)(implicit appContext: AppContext): Unit = {
    import appContext._
    transactions
      .saveToEs(index, esOptions)
  }

  def currentInstant(implicit timer: Timer[IO]): IO[Instant] =
    timer.clockRealTime(TimeUnit.SECONDS) map Instant.ofEpochSecond


}
