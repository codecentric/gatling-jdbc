organization := "de.codecentric"
name := "jdbc-gatling"
scalaVersion := "2.12.6"
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.3",
  "io.gatling"            % "gatling-test-framework"    % "3.0.3",
  "org.scalikejdbc"       %% "scalikejdbc"              % "3.3.2",
  "com.h2database"        % "h2"                        % "1.4.197",
  "ch.qos.logback"        % "logback-classic"           % "1.2.3",
  "mysql"                 % "mysql-connector-java"      % "8.0.13"  % "test",
  "org.postgresql"        % "postgresql"                % "42.2.5"  % "test",
  "org.scalatest"         %% "scalatest"                % "3.0.5"   % "test",
  "org.testcontainers"    % "postgresql"                % "1.10.6"  % "test",
  "org.testcontainers"    % "mysql"                     % "1.10.6"  % "test"
)
enablePlugins(GatlingPlugin)

parallelExecution in Test := false

//everything below this line is related to the project release
homepage := Some(url("https://github.com/codecentric/gatling-jdbc"))
scmInfo := Some(ScmInfo(url("https://github.com/codecentric/gatling-jdbc"), "git@github.com:codecentric/gatling-jdbc.git"))
developers := List(Developer("rbraeunlich",
  "Ronny Bräunlich",
  "ronny.braeunlich@codecentric.de",
  url("https://github.com/rbraeunlich")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

pomIncludeRepository := { _ => false }
publishArtifact in Test := false
publishMavenStyle := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseProcess += releaseStepCommand("sonatypeRelease")
