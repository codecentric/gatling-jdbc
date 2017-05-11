package de.codecentric.gatling.jdbc.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectionWithoutWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String]) extends ActionBuilder {

  def where(where: Expression[String]) = JdbcSelectionWithWhereActionBuilder(requestName, what, from, where)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcSelectAction(requestName, what, from, None, statsEngine, next)
  }

}

case class JdbcSelectionWithWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String], where: Expression[String]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcSelectAction(requestName, what, from, Some(where), statsEngine, next)
  }

}
