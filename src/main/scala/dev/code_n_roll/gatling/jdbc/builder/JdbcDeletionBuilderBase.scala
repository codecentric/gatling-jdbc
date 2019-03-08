package dev.code_n_roll.gatling.jdbc.builder

import dev.code_n_roll.gatling.jdbc.action.JdbcDeletionWithoutWhereActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDeletionBuilderBase(requestName: Expression[String]) {

  def from(tableName: Expression[String]) = JdbcDeletionWithoutWhereActionBuilder(requestName, tableName)

}
