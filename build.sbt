name := "ecomm-analyzer"

version := "0.1"

scalaVersion := "2.11.8"
val sparkVersion = "2.3.2"

libraryDependencies ++= Seq(
  "org.lz4" % "lz4-java" % "1.4.0",
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided, // As it is already present in the Spark distribution.
  "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier
    "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier   "tests",
  "org.apache.spark" %% "spark-catalyst" % sparkVersion % Test     classifier "tests",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion %
    Provided exclude ("net.jpountz.lz4", "lz4"),
  "org.elasticsearch" %% "elasticsearch-spark-20" % "7.3.2"
)

// Avoids SI-3623
target := file("/tmp/sbt/ecomm-analyzer")

// assembly
assemblyOption in assembly := (assemblyOption in
  assembly).value.copy(includeScala = false) // exclude all scala runtime JARs
test in assembly := {} // skip the tests when running the assembly task
mainClass in assembly := Some("ecomm.BatchProducerRunnerSpark") // declare main task