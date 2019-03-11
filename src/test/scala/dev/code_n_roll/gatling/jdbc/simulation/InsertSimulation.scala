package dev.code_n_roll.gatling.jdbc.simulation

import dev.code_n_roll.gatling.jdbc.Predef._
import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scalikejdbc.{GlobalSettings, LoggingSQLAndTimeSettings}

import scala.concurrent.duration._
import scala.util.Random

/**
  * Created by ronny on 10.05.17.
  */
class InsertSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")
  val feeder = Iterator.continually(Map("rand" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true, logLevel = 'warn)

  val createTable = scenario("create table")
    .exec(jdbc("bar table")
      .create()
      .table("bar")
      .columns(
        column(
          name("abc"),
          dataType("VARCHAR"),
          constraint("PRIMARY KEY")
        )
      )
    )

  val insertion = scenario("insertion")
    .pause(3.seconds)
    .feed(feeder)
    .repeat(10, "n") {
      exec(jdbc("insertion")
        .insert()
        .into("bar (abc)")
        .values("'${rand} + ${n}'")
      )
    }

  setUp(
    createTable.inject(atOnceUsers(1)),
    insertion.inject(atOnceUsers(10))
  ).protocols(jdbcConfig)
    .assertions(global.failedRequests.count.is(0))
}
