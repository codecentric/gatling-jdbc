package dev.code_n_roll.gatling.jdbc.action

import io.gatling.commons.util.Clock
import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.action.Action
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import scalikejdbc.{DB, SQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcDropTableAction(requestName: Expression[String],
                               tableName: Expression[String],
                               clock: Clock,
                               statsEngine: StatsEngine,
                               next: Action) extends JdbcAction {

  override def name: String = genName("jdbcDropTable")

  override def execute(session: Session): Unit = {
    val start = clock.nowMillis
    val validatedTableName = tableName.apply(session)
    validatedTableName match {
      case Success(name) =>
        val query = s"DROP TABLE $name"
        val future = Future {
          DB autoCommit { implicit session =>
            SQL(query).map(rs => rs.toMap()).execute().apply()
          }
        }
        future.onComplete(result => {
          next ! log(start, clock.nowMillis, result, requestName, session, statsEngine)
        })

      case Failure(error) => throw new IllegalArgumentException(error)
    }
  }

}
