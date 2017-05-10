package de.codecentric.gatling.jdbc

import io.gatling.core.scenario.Simulation
import de.codecentric.gatling.jdbc.Predef._
import io.gatling.core.Predef._
/**
  * Created by ronny on 10.05.17.
  */
class CreateTableSimulation extends Simulation {

  val jdbcConfig = jdbc.url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE").username("sa").password("sa").driver("org.h2.Driver")

  val testScenario = scenario("createTable").
    exec(jdbc("foo table").create().name("foo"))

  setUp(testScenario.inject(atOnceUsers(1))).protocols(jdbcConfig)
}
