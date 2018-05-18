organization := "de.codecentric"
name := "gatling-jdbc"
scalaVersion := "2.12.6"
version := "1.0.0"
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.1",
  "io.gatling" % "gatling-test-framework" % "2.3.1",
  "org.scalikejdbc" %% "scalikejdbc" % "3.2.0",
  "com.h2database" % "h2" % "1.4.197",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "org.postgresql" % "postgresql" % "42.2.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
enablePlugins(GatlingPlugin)

homepage := Some(url("https://github.com/rbraeunlich/gatling-jdbc"))
scmInfo := Some(ScmInfo(url("https://github.com/rbraeunlich/gatling-jdbc"), "git@github.com:rbraeunlich/gatling-jdbc.git"))
developers := List(Developer("rbraeunlich",
  "Ronny Bräunlich",
  "ronny.braeunlich@codecentric.de",
  url("https://github.com/rbraeunlich")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

publishArtifact in(Test, packageBin) := true

parallelExecution in Test := false
