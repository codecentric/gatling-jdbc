package de.codecentric.gatling.jdbc

import de.codecentric.gatling.jdbc.Predef._
import de.codecentric.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.util.Random

/**
  * Created by ronny on 10.05.17.
  */
class InsertSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")
  val feeder = Iterator.continually(Map("rand" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))

  val testScenario = scenario("createTable").
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
    )
    .feed(feeder)
    .repeat(10, "n") {
      exec(jdbc("insertion")
        .insert()
        .into("bar (abc)")
        .values("'${rand} + ${n}'")
      )
    }

  setUp(testScenario.inject(atOnceUsers(10))).protocols(jdbcConfig)
}
