package dev.code_n_roll.gatling.jdbc.action

import dev.code_n_roll.gatling.jdbc.check.JdbcCheckActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import scalikejdbc.WrappedResultSet

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectionWithoutWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String]) extends JdbcCheckActionBuilder[Map[String, Any]] {

  def where(where: Expression[String]) = JdbcSelectionWithWhereActionBuilder(requestName, what, from, where)

  def mapResult[T](mapFunction: WrappedResultSet => T) = JdbcSelectionWithMappingActionBuilder(requestName, what, from, None, mapFunction)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcSelectAction(requestName, what, from, None, checks.toList, _.toMap(), clock, statsEngine, next)
  }

}

case class JdbcSelectionWithWhereActionBuilder(requestName: Expression[String], what: Expression[String], from: Expression[String], where: Expression[String]) extends JdbcCheckActionBuilder[Map[String, Any]] {

  def mapResult[T](mapFunction: WrappedResultSet => T) = JdbcSelectionWithMappingActionBuilder(requestName, what, from, Some(where), mapFunction)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcSelectAction(requestName, what, from, Some(where), checks.toList, _.toMap(), clock, statsEngine, next)
  }

}

case class JdbcSelectionWithMappingActionBuilder[T](requestName: Expression[String],
                                                    what: Expression[String],
                                                    from: Expression[String],
                                                    where: Option[Expression[String]],
                                                    mapFunction: WrappedResultSet => T) extends JdbcCheckActionBuilder[T] {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val statsEngine = ctx.coreComponents.statsEngine
    val clock = ctx.coreComponents.clock
    JdbcSelectAction(requestName, what, from, where, checks.toList, mapFunction, clock, statsEngine, next)
  }

}