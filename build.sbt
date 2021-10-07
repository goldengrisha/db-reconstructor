name := "db-reconstructor"
version := "0.1"
scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.16",
  "com.github.mjakubowski84" %% "parquet4s-core" % "1.9.4",
  "com.github.mjakubowski84" %% "parquet4s-akka" % "1.9.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.3",
  "org.postgresql" % "postgresql" % "42.2.24",
  "org.apache.hadoop" % "hadoop-client" % "3.3.1",
  "org.apache.hadoop" % "hadoop-aws" % "3.3.1",
  "ch.qos.logback" % "logback-classic" % "1.2.6"
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
