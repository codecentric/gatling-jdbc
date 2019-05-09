package dev.code_n_roll.gatling.jdbc.action

import io.gatling.commons.util.Clock
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc.{DB, SQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDeletionAction(requestName: Expression[String],
                              tableName: Expression[String],
                              where: Option[Expression[String]],
                              clock: Clock,
                              statsEngine: StatsEngine,
                              next: Action) extends JdbcAction {

  override def name: String = genName("jdbcDelete")

  override def execute(session: Session): Unit = {
    val start = clock.nowMillis
    val validatedTableName = tableName.apply(session)

    val wherePart = where.fold("")(_.apply(session) match {
      case Success(whereString) => s"WHERE $whereString"
      case Failure(e) => throw new IllegalArgumentException(e)
    })

    val result: Validation[Future[Unit]] = for {
      tableString <- validatedTableName
      sqlString <- Success(s"DELETE FROM $tableString $wherePart")
    } yield Future {
        DB autoCommit { implicit session =>
          SQL(sqlString).map(rs => rs.toMap()).execute().apply()
        }
      }

    result.foreach(_.onComplete(result => {
      next ! log(start, clock.nowMillis, result, requestName, session, statsEngine)
    }))

    result.onFailure(e => throw new IllegalArgumentException(e))
  }
}