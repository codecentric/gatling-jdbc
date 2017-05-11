package de.codecentric.gatling.jdbc.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcInsertionActionBuilder(requestName: Expression[String], tableName: Expression[String], values: Expression[String]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcInsertAction(requestName, tableName, values, statsEngine, next)
  }

}
