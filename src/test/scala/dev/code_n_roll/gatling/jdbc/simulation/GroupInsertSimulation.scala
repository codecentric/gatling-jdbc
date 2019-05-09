package dev.code_n_roll.gatling.jdbc.simulation

import dev.code_n_roll.gatling.jdbc.Predef._
import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scalikejdbc.{GlobalSettings, LoggingSQLAndTimeSettings}

import scala.concurrent.duration._
import scala.util.Random

/**
  * Simulation to check that the reporting of times also works when using a group.
  * Therefore, the response time is set to be between 0 and 1000. When the group
  * time doesn't work the time will be just zero.
  */
class GroupInsertSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true, logLevel = 'warn)

  val scn = scenario("group create insert")
    .group("group") {
      exec(jdbc("bar table")
        .create()
        .table("bar")
        .columns(
          column(
            name("abc"),
            dataType("VARCHAR"),
            constraint("PRIMARY KEY")
          )
        )
      ).pause(3.seconds)
        .exec(jdbc("insertion")
          .insert()
          .into("bar (abc)")
          .values("'1'"))
    }

  setUp(
    scn.inject(atOnceUsers(1)),
  ).protocols(jdbcConfig)
    .assertions(details("group").responseTime.mean.between(0, 1000, inclusive = false))
}
