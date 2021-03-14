package ecomm

import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.FiniteDuration

class BatchProducerSpec extends WordSpec with Matchers with SharedSparkSession {

  "BatchProducer.idxToTransactions" should {
    implicit object FakeTimer extends Timer[IO] {
      private var clockRealTimeInMillis: Long = Instant.parse("2021-03-04T17:30:00Z").toEpochMilli

      def clockRealTime(unit: TimeUnit): IO[Long] =
        IO(unit.convert(clockRealTimeInMillis, TimeUnit.MILLISECONDS))

      def sleep(duration: FiniteDuration): IO[Unit] = IO {
        clockRealTimeInMillis = clockRealTimeInMillis + duration.toMillis
      }

      def shift: IO[Unit] = ???

      def clockMonotonic(unit: TimeUnit): IO[Long] = ???
    }

    val esOptions = Map(
      // TODO: 연결할 es 주소 입력
      "es.nodes" -> "",
      "es.nodes.wan.only" -> "true",
      "es.nodes.discovery" -> "false",
      "es.nodes.data.only" -> "false",
      "es.read.field.as.array.include" -> "category,manufacturer,products,sku"
    )

    implicit val appContext: AppContext = new AppContext(esOptions)

    "create a DataSet from a es index" in {
      val ds = BatchProducer.idxToTransactions.unsafeRunSync()
      ds.show(10)

      val currentInstant = BatchProducer.currentInstant.unsafeRunSync()
      val filtered = BatchProducer.filterTransactions(ds, currentInstant.minusSeconds(60*60*24), currentInstant)
      filtered.show(4)
    }
  }
}