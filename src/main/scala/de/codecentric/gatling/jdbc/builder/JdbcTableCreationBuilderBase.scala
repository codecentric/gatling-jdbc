package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableCreationActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationBuilderBase() {

  def name(name: Expression[String]) = JdbcTableCreationActionBuilder(name)

}
