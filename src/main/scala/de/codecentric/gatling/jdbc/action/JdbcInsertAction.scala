package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.TimeHelper
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import scalikejdbc.{DB, SQL}

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcInsertAction(requestName: Expression[String], tableName: Expression[String], values: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction with NameGen {

  override def name: String = genName("jdbcInsert")

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val validatedTableName = tableName.apply(session)
    val validatedValues = values.apply(session)
    val sqlString: Validation[String] = for {
      tableValue <- validatedTableName
      valuesValue <- validatedValues
    } yield s"INSERT INTO $tableValue VALUES ( $valuesValue )"

    sqlString match {
      case Success(s) =>
        DB autoCommit { implicit session =>
          SQL(s).execute().apply()
        }

      case Failure(error) => throw new IllegalArgumentException(error)
    }

    val end = TimeHelper.nowMillis
    val timing = ResponseTimings(start, end)
    requestName.apply(session).map { resolvedRequestName =>
      statsEngine.logResponse(session, resolvedRequestName, timing, OK, None, None)
    }
    next ! session
  }
}
