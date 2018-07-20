package de.codecentric.gatling.jdbc.action

import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc.{DB, SQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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

    val result: Validation[Future[Unit]] = for {
      tableValue <- validatedTableName
      valuesValue <- validatedValues
      sql <- Success(s"INSERT INTO $tableValue VALUES ( $valuesValue )")
    } yield {
      Future {
        DB autoCommit { implicit session =>
          SQL(sql).execute().apply()
        }
      }
    }
    result.foreach(_.onComplete(result => {
      log(start, nowMillis, result, requestName, session, statsEngine)
      next ! session
    }))

    result.onFailure(e => throw new IllegalArgumentException(e))
  }
}
