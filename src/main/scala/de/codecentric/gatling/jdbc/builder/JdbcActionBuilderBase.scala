package de.codecentric.gatling.jdbc.builder

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcActionBuilderBase(requestName: Expression[String]) {

  def create() = JdbcTableCreationBuilderBase(requestName)

  def insert() = JdbcTableInsertionBuilderBase(requestName)

}
