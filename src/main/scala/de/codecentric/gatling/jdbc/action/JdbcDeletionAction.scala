package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.Success
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import scalikejdbc.{DB, SQL}

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDeletionAction(requestName: Expression[String], tableName: Expression[String], where: Option[Expression[String]], statsEngine: StatsEngine, next: Action) extends ChainableAction with NameGen {

  override def name: String = genName("jdbcDelete")

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val validatedTableName = tableName.apply(session)
    val validatedWhere = where.map(w => w.apply(session))

    val sqlString = (validatedTableName, validatedWhere) match {
      case (Success(tableString), Some(Success(whereString))) => s"DELETE FROM $tableString WHERE $whereString"
      case (Success(tableString), None) => s"DELETE FROM $tableString"
      case _ => throw new IllegalArgumentException
    }

    DB autoCommit { implicit session =>
      SQL(sqlString).map(rs => rs.toMap()).execute().apply()
    }

    val end = TimeHelper.nowMillis
    val timing = ResponseTimings(start, end)
    requestName.apply(session).map { resolvedRequestName =>
      statsEngine.logResponse(session, resolvedRequestName, timing, OK, None, None)
    }
    next ! session
  }

}
