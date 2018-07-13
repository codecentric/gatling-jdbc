package de.codecentric.gatling.jdbc.action

import de.codecentric.gatling.jdbc.JdbcCheck
import io.gatling.commons.stats.KO
import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import scalikejdbc.{DB, SQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

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
    val start = nowMillis
    val validatedWhat = what.apply(session)
    val validatedFrom = from.apply(session)
    val validatedWhere = where.map(w => w.apply(session))

    val sqlString = (validatedWhat, validatedFrom, validatedWhere) match {
      case (Success(whatString), Success(fromString), Some(Success(whereString))) => s"SELECT $whatString FROM $fromString WHERE $whereString"
      case (Success(whatString), Success(fromString), None) => s"SELECT $whatString FROM $fromString"
      case _ => throw new IllegalArgumentException
    }

    val future: Future[List[Map[String, Any]]] = Future {
      DB autoCommit { implicit session =>
        SQL(sqlString).map(rs => rs.toMap()).toList().apply()
      }
    }
    future.onComplete {
      case scala.util.Success(value) => performChecks(session, start, value)
      case fail: Failure[_] => log(start, nowMillis, fail, requestName, session, statsEngine)
        next ! session
    }
  }

  private def performChecks(session: Session, start: Long, tried: List[Map[String, Any]]): Unit = {
    val (modifySession, error) = Check.check(tried, session, checks)
    val newSession = modifySession(session)
    error match {
      case Some(failure) =>
        requestName.apply(session).map { resolvedRequestName =>
          statsEngine.logResponse(session, resolvedRequestName, ResponseTimings(start, nowMillis), KO, None, None)
        }
        next ! newSession.markAsFailed
      case _ =>
        log(start, nowMillis, scala.util.Success(""), requestName, session, statsEngine)
        next ! newSession
    }
  }
}
