package de.codecentric.gatling.jdbc.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationActionBuilder(requestName: Expression[String], name: Expression[String], columns: ArrayBuffer[(Expression[String], Expression[String], Option[Expression[String]])]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    JdbcCreateTableAction(requestName, name, columns, statsEngine, next)
  }

}
