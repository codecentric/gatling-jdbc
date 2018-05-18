package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.OK
import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import scalikejdbc.{DB, SQL}

import scala.util.Try

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcInsertAction(requestName: Expression[String],
                            tableName: Expression[String],
                            values: Expression[String],
                            statsEngine: StatsEngine,
                            next: Action) extends JdbcAction {

  override def name: String = genName("jdbcInsert")

  override def execute(session: Session): Unit = {
    val start = nowMillis
    val validatedTableName = tableName.apply(session)
    val validatedValues = values.apply(session)
    val sqlString: Validation[String] = for {
      tableValue <- validatedTableName
      valuesValue <- validatedValues
    } yield s"INSERT INTO $tableValue VALUES ( $valuesValue )"

    sqlString match {
      case Success(s) =>
        val tried = Try(DB autoCommit { implicit session =>
          SQL(s).execute().apply()
        })
        log(start, nowMillis, tried, requestName, session, statsEngine)

      case Failure(error) => throw new IllegalArgumentException(error)
    }

    next ! session
  }
}
