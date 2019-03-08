package dev.code_n_roll.gatling.jdbc.protocol

import dev.code_n_roll.gatling.jdbc.Predef.jdbc
import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings}

class JdbcProtocolSpec extends FlatSpec with Matchers {

  "JdbcProtocol" should "use default connection pool setting if none are provided" in {
    jdbc
      .url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE")
      .username("sa")
      .password("sa")
      .driver("org.h2.Driver")
      .build

    ConnectionPool.get().settings should equal(ConnectionPoolSettings())
  }

  it should "use custom connection pool settings if they are given" in {
    val settings = ConnectionPoolSettings(maxSize = 20)
    jdbc
      .url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE")
      .username("sa")
      .password("sa")
      .driver("org.h2.Driver")
      .connectionPoolSettings(settings)
      .build

    ConnectionPool.get().settings should equal(settings)
  }
}
