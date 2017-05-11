package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableSelectionWithoutWhereActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcTableSelectionBuilderBase(requestName: Expression[String], what: Expression[String]) {

  def from(from: Expression[String]) = JdbcTableSelectionWithoutWhereActionBuilder(requestName, what, from)

}
