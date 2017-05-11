package de.codecentric.gatling.jdbc.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * Created by ronny on 11.05.17.
  */

case class JdbcDeletionWithoutWhereActionBuilder(requestName: Expression[String], tableName: Expression[String]) extends ActionBuilder {

  def where(where: Expression[String]) = JdbcDeletionWithWhereActionBuilder(requestName, tableName, where)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcDeletionAction(requestName, tableName, None, statsEngine, next)
  }
}

case class JdbcDeletionWithWhereActionBuilder(requestName: Expression[String], tableName: Expression[String], where: Expression[String]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcDeletionAction(requestName, tableName, Some(where), statsEngine, next)
  }

}
