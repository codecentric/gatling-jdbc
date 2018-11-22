package de.codecentric.gatling.jdbc.simulation

import de.codecentric.gatling.jdbc.Predef._
import de.codecentric.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

/**
  * Created by ronny on 10.05.17.
  */
class SelectAnyCheckSimulation extends Simulation {

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
        ),
        column(
          name("foo"),
          dataType("INTEGER")
        )
      )
    ).repeat(10, "n") {
    exec(jdbc("insertion")
      .insert()
      .into("bar")
      .values("${n}, ${n}")
    )
  }.pause(1).
    exec(jdbc("selectionSingleCheck")
      .select("*")
      .from("bar")
      .where("abc=4")
      .check(jdbcSingleResponse.is(Map[String, Any]("ABC" -> 4, "FOO" -> 4))
        .saveAs("myResult"))
    ).pause(1).
    exec(jdbc("selectionManyCheck")
      .select("*")
      .from("bar")
      .where("abc=4 OR abc=5")
      .check(jdbcManyResponse.is(List(
        Map("ABC" -> 4, "FOO" -> 4),
        Map("ABC" -> 5, "FOO" -> 5)))
      )
    )
  //.exec(session => session("something").as[List[Map[String, Any]]])


  setUp(testScenario.inject(atOnceUsers(1)))
    .protocols(jdbcConfig)
    .assertions(global.failedRequests.count.is(0))

}
