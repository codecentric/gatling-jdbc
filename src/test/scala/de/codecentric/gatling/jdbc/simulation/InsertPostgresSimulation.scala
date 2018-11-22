package de.codecentric.gatling.jdbc.simulation

import de.codecentric.gatling.jdbc.Predef._
import de.codecentric.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import org.testcontainers.containers.PostgreSQLContainer

/**
  * Created by ronny on 10.05.17.
  */
class InsertPostgresSimulation extends Simulation {

  val postgres = new PostgreSQLContainer()
  postgres.start()

  val jdbcConfig = jdbc.url(postgres.getJdbcUrl).username(postgres.getUsername).password(postgres.getPassword).driver(postgres.getDriverClassName)

  val tableIdentFeeder = for (x <- 0 until 10) yield Map("tableId" -> x)

  val uniqueNumberFeeder = for (x <- 0 until 100) yield Map("unique" -> x)

  after {
    postgres.stop()
  }

  val createTables = scenario("createTable").feed(tableIdentFeeder.iterator).
    exec(jdbc("create")
      .create()
      .table("bar${tableId}")
      .columns(
        column(
          name("abc"),
          dataType("INTEGER"),
          constraint("PRIMARY KEY")
        )
      )
    )

  val fillTables = feed(uniqueNumberFeeder).repeat(100, "n") {
    feed(tableIdentFeeder.iterator.toArray.random).
      exec(jdbc("insertion")
        .insert()
        .into("bar${tableId}")
        .values("${unique}${n}")
      )
  }


  setUp(
    createTables.inject(atOnceUsers(10)),
    scenario("fillTables").pause(5).exec(fillTables).inject(atOnceUsers(100))
  ).protocols(jdbcConfig)
    .assertions(global.successfulRequests.percent.gte(99))

}
