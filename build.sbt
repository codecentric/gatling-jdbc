lazy val root = project
  .in(file("."))
  .settings(
    name := "gatling-jdbc",
    scalaVersion := "2.11.8",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.1",
      "io.gatling" % "gatling-test-framework" % "2.2.1",
      "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",
      "com.h2database" % "h2" % "1.4.195",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "mysql" % "mysql-connector-java" % "5.1.6",
      "org.postgresql" % "postgresql" % "42.1.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  ).enablePlugins(GatlingPlugin)

publishArtifact in(Test, packageBin) := true

parallelExecution in Test := false