package de.codecentric.gatling.jdbc.action

import de.codecentric.gatling.jdbc.JdbcCheck
import io.gatling.commons.stats.KO
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.check.Check
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import scalikejdbc.{DB, SQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcSelectAction(requestName: Expression[String],
                            what: Expression[String],
                            from: Expression[String],
                            where: Option[Expression[String]],
                            checks: List[JdbcCheck],
                            clock: Clock,
                            statsEngine: StatsEngine,
                            next: Action) extends JdbcAction {

  override def name: String = genName("jdbcSelect")

  override def execute(session: Session): Unit = {
    val start = clock.nowMillis
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
      case scala.util.Success(value) =>
        next ! Try(performChecks(session, start, value)).recover {
          case err =>
            statsEngine.logCrash(session, requestName.apply(session).get, err.getMessage)
            session.markAsFailed
        }.get
      case fail: Failure[_] =>
        log(start, clock.nowMillis, fail, requestName, session, statsEngine)
        next ! session
    }
  }

  private def performChecks(session: Session, start: Long, tried: List[Map[String, Any]]): Session = {
    val (modifySession, error) = Check.check(tried, session, checks)
    val newSession = modifySession(session)
    error match {
      case Some(failure) =>
        requestName.apply(session).map { resolvedRequestName =>
          statsEngine.logResponse(session, resolvedRequestName, start, clock.nowMillis, KO, None, None)
        }
        newSession.markAsFailed
      case _ =>
        log(start, clock.nowMillis, scala.util.Success(""), requestName, session, statsEngine)
        newSession
    }
  }
}
