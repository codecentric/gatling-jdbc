package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import scalikejdbc._

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcCreateTableAction(tableName: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "Create table action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val validatedTableName = tableName.apply(session)
    validatedTableName match {
      case Success(name) =>
        DB autoCommit { implicit session =>
          sql"CREATE TABLE ${name}(id INTEGER PRIMARY KEY)".execute().apply()
        }

      case Failure(error) => throw new IllegalArgumentException(error)
    }
    val end = TimeHelper.nowMillis
    val timing = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timing, OK, None, None)
  }

}
