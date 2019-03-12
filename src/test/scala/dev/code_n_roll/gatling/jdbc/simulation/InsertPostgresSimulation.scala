package dev.code_n_roll.gatling.jdbc.simulation

import java.util.UUID

import dev.code_n_roll.gatling.jdbc.Predef._
import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import org.testcontainers.containers.PostgreSQLContainer
import scalikejdbc.{GlobalSettings, LoggingSQLAndTimeSettings}

/**
  * Created by ronny on 10.05.17.
  */
class InsertPostgresSimulation extends Simulation {

  val postgres = new PostgreSQLContainer()
  postgres.start()

  val jdbcConfig = jdbc.url(postgres.getJdbcUrl).username(postgres.getUsername).password(postgres.getPassword).driver(postgres.getDriverClassName)

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true, logLevel = 'warn)

  val tableIdentFeeder = for (x <- 0 until 10) yield Map("tableId" -> x)

  val uniqueValuesFeeder = Iterator.continually(Map("unique" -> UUID.randomUUID()))

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
          dataType("VARCHAR(39)"),
          constraint("PRIMARY KEY")
        )
      )
    )

  val fillTables = feed(uniqueValuesFeeder).repeat(100, "n") {
    feed(tableIdentFeeder.iterator.toArray.random).
      exec(jdbc("insertion")
        .insert()
        .into("bar${tableId}")
        .values("'${unique}${n}'")
      )
  }


  setUp(
    createTables.inject(atOnceUsers(10)),
    scenario("fillTables").pause(5).exec(fillTables).inject(atOnceUsers(100))
  ).protocols(jdbcConfig)
    .assertions(global.successfulRequests.percent.gte(99))

}
