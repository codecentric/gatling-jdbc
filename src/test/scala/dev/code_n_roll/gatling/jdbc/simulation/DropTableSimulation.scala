package dev.code_n_roll.gatling.jdbc.simulation

import dev.code_n_roll.gatling.jdbc.Predef._
import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scalikejdbc.{GlobalSettings, LoggingSQLAndTimeSettings}

/**
  * Created by ronny on 10.05.17.
  */
class DropTableSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true, logLevel = 'warn)

  val testScenario = scenario("createTable").
    exec(jdbc("bar table")
      .create()
      .table("bar")
      .columns(
        column(
          name("abc"),
          dataType("INTEGER"),
          constraint("PRIMARY KEY")
        ),
        column(
          name("ac"),
          dataType("INTEGER")
        )
      )
    ).exec(jdbc("drop bar table").drop().table("bar"))


  setUp(testScenario.inject(atOnceUsers(1)))
    .protocols(jdbcConfig)
    .assertions(global.failedRequests.count.is(0))

}
