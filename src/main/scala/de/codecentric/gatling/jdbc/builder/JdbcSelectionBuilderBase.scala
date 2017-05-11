package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcSelectionWithoutWhereActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectionBuilderBase(requestName: Expression[String], what: Expression[String]) {

  def from(from: Expression[String]) = JdbcSelectionWithoutWhereActionBuilder(requestName, what, from)

}
