package de.codecentric.gatling.jdbc.action

import de.codecentric.gatling.jdbc.check.JdbcCheckActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectionWithoutWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String]) extends JdbcCheckActionBuilder {

  def where(where: Expression[String]) = JdbcSelectionWithWhereActionBuilder(requestName, what, from, where)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcSelectAction(requestName, what, from, None, checks.toList, clock, statsEngine, next)
  }

}

case class JdbcSelectionWithWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String], where: Expression[String]) extends JdbcCheckActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcSelectAction(requestName, what, from, Some(where), checks.toList, clock, statsEngine, next)
  }

}