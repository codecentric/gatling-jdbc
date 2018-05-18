package de.codecentric.gatling.jdbc.action

import de.codecentric.gatling.jdbc.builder.column.ColumnDefinition
import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc._

import scala.util.Try

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcCreateTableAction(requestName: Expression[String],
                                  tableName: Expression[String],
                                  columns: Seq[ColumnDefinition],
                                  statsEngine: StatsEngine,
                                  next: Action) extends JdbcAction {

  override def name: String = genName("jdbcCreateTable")

  override def execute(session: Session): Unit = {
    val start = nowMillis
    val columnStrings = columns.map(
      t => (
        t.name.name.apply(session),
        t.dataType.dataType.apply(session),
        t.columnConstraint.map(constr => constr.constraint).map(expr => expr.apply(session)).getOrElse(Success(""))))
      .map {
        case (Success(columnName), Success(dataType), Success(constraint)) => s"$columnName $dataType $constraint"
        case _ => throw new IllegalArgumentException
      }.mkString(",")

    val validatedTableName = tableName.apply(session)
    validatedTableName match {
      case Success(name) =>
        val query = s"CREATE TABLE $name($columnStrings)"
        val tried = Try(DB autoCommit { implicit session =>
          SQL(query).execute().apply()
        })
        log(start, nowMillis, tried, requestName, session, statsEngine)

      case Failure(error) => throw new IllegalArgumentException(error)
    }
    next ! session
  }

}
