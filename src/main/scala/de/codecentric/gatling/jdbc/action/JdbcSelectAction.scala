package de.codecentric.gatling.jdbc.action

import java.sql.ResultSet

import de.codecentric.gatling.jdbc.JdbcCheck
import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import scalikejdbc.{DB, SQL}

import scala.util.{Failure, Try}

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectAction(requestName: Expression[String],
                            what: Expression[String],
                            from: Expression[String],
                            where: Option[Expression[String]],
                            checks: List[JdbcCheck],
                            statsEngine: StatsEngine,
                            next: Action) extends JdbcAction {

  override def name: String = genName("jdbcSelect")

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val validatedWhat = what.apply(session)
    val validatedFrom = from.apply(session)
    val validatedWhere = where.map(w => w.apply(session))

    val sqlString = (validatedWhat, validatedFrom, validatedWhere) match {
      case (Success(whatString), Success(fromString), Some(Success(whereString))) => s"SELECT $whatString FROM $fromString WHERE $whereString"
      case (Success(whatString), Success(fromString), None) => s"SELECT $whatString FROM $fromString"
      case _ => throw new IllegalArgumentException
    }

    val tried = Try(DB autoCommit { implicit session =>
      SQL(sqlString).map(rs => rs.underlying).single().apply()
    })

    if (tried.isSuccess) {
      performChecks(session, start, tried.get)
    } else {
      log(start, TimeHelper.nowMillis, tried, requestName, session, statsEngine)
      next ! session
    }
  }

  private def performChecks(session: Session, start: Long, tried: Option[ResultSet]) = {
    val (modifySession, error) = Check.check(tried.get, session, checks)
    val newSession = modifySession(session)
    error match {
      case Some(failure) =>
        requestName.apply(session).map { resolvedRequestName =>
          statsEngine.logResponse(session, resolvedRequestName, ResponseTimings(start, TimeHelper.nowMillis), KO, None, None)
        }
        next ! newSession.markAsFailed
      case _ =>
        log(start, TimeHelper.nowMillis, scala.util.Success(""), requestName, session, statsEngine)
        next ! newSession
    }
  }
}
