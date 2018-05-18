package de.codecentric.gatling.jdbc.action

import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.Success
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc.{DB, SQL}

import scala.util.Try

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDeletionAction(requestName: Expression[String],
                              tableName: Expression[String],
                              where: Option[Expression[String]],
                              statsEngine: StatsEngine,
                              next: Action) extends JdbcAction {

  override def name: String = genName("jdbcDelete")

  override def execute(session: Session): Unit = {
    val start = nowMillis
    val validatedTableName = tableName.apply(session)
    val validatedWhere = where.map(w => w.apply(session))

    val sqlString = (validatedTableName, validatedWhere) match {
      case (Success(tableString), Some(Success(whereString))) => s"DELETE FROM $tableString WHERE $whereString"
      case (Success(tableString), None) => s"DELETE FROM $tableString"
      case _ => throw new IllegalArgumentException
    }

    val tried = Try(DB autoCommit { implicit session =>
      SQL(sqlString).map(rs => rs.toMap()).execute().apply()
    })

    log(start, nowMillis, tried, requestName, session, statsEngine)

    next ! session
  }

}
