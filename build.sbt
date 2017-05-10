lazy val root = project
  .in(file("."))
  .settings(
    name := "gatling-jdbc",
    scalaVersion := "2.11.8",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
        "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.1",
        "io.gatling" % "gatling-test-framework" % "2.2.1"
    )
  ).enablePlugins(GatlingPlugin)

publishArtifact in (Test, packageBin) := true
