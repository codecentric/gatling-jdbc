package de.codecentric.gatling.jdbc.action

import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc.{DB, SQL}

import scala.util.Try

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDropTableAction(requestName: Expression[String],
                               tableName: Expression[String],
                               statsEngine: StatsEngine,
                               next: Action) extends JdbcAction {

  override def name: String = genName("jdbcDropTable")

  override def execute(session: Session): Unit = {
    val start = nowMillis
    val validatedTableName = tableName.apply(session)
    validatedTableName match {
      case Success(name) =>
        val query = s"DROP TABLE $name"
        val tried = Try(DB autoCommit { implicit session =>
          SQL(query).execute().apply()
        })
        log(start, nowMillis, tried, requestName, session, statsEngine)

      case Failure(error) => throw new IllegalArgumentException(error)
    }

    next ! session
  }
}
