package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import scalikejdbc._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcCreateTableAction(tableName: Expression[String], columns: ArrayBuffer[(Expression[String], Expression[String], Option[Expression[String]])], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "Create table action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val columnStrings = columns.map(t => (t._1.apply(session), t._2.apply(session), t._3.map(expr => expr.apply(session)).getOrElse(Success("")))).map {
      case (Success(columnName), Success(dataType), Success(constraint)) => s"$columnName $dataType $constraint"
      case _ => throw new IllegalArgumentException
    }.mkString(",")

    val validatedTableName = tableName.apply(session)
    validatedTableName match {
      case Success(name) =>
        val query = s"CREATE TABLE $name($columnStrings)"
        DB autoCommit { implicit session =>
          SQL(query).execute().apply()
        }

      case Failure(error) => throw new IllegalArgumentException(error)
    }
    val end = TimeHelper.nowMillis
    val timing = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timing, OK, None, None)

    next ! session
  }

}
