package de.codecentric.gatling.jdbc.simulation

import de.codecentric.gatling.jdbc.Predef._
import de.codecentric.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

/**
  * Created by ronny on 10.05.17.
  */
class SelectSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")

  val testScenario = scenario("createTable").
    exec(jdbc("bar table")
      .create()
      .table("bar")
      .columns(
        column(
          name("abc"),
          dataType("INTEGER"),
          constraint("PRIMARY KEY")
        )
      )
    ).repeat(10, "n") {
    exec(jdbc("insertion")
      .insert()
      .into("bar")
      .values("${n}")
    )
  }.pause(1).
    exec(jdbc("selection")
      .select("*")
      .from("bar")
      .where("abc=4")
    )


  setUp(testScenario.inject(atOnceUsers(1)))
    .protocols(jdbcConfig)
    .assertions(global.failedRequests.count.is(0))

}
