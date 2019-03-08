package dev.code_n_roll.gatling.jdbc.action

import dev.code_n_roll.gatling.jdbc.builder.column.ColumnDefinition
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationActionBuilder(requestName: Expression[String], name: Expression[String], columns: Seq[ColumnDefinition]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcCreateTableAction(requestName, name, columns, clock, statsEngine, next)
  }

}
