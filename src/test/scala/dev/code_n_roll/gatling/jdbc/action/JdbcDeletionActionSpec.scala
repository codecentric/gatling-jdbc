package dev.code_n_roll.gatling.jdbc.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.core.stats.writer.ResponseMessage
import org.scalatest.Matchers.equal
import org.scalatest.Matchers._
import io.gatling.core.Predef._
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import scalikejdbc.DB
import scalikejdbc._

/**
  * Created by ronny on 12.05.17.
  */
class JdbcDeletionActionSpec extends JdbcActionSpec {

  private val clock = new DefaultClock

  "JdbcDeletionAction" should "use the request name in the log message" in {
    val requestName = "name"
    val latchAction = BlockingLatchAction()
    val action = JdbcDeletionAction(requestName, "table", None, clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "delete the data specied by the where clause" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE foo(bar INTEGER ); INSERT INTO foo VALUES (1);INSERT INTO foo VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcDeletionAction("request", "foo", Some("bar = 2"), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    val result = DB readOnly { implicit session =>
      sql"""SELECT COUNT(*) FROM foo""".map(rs => rs.int(1)).single().apply()
    }
    result should contain(1)
  }

  it should "delete all data when no where specified" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE bar(foo INTEGER ); INSERT INTO bar VALUES (1);INSERT INTO bar VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcDeletionAction("request", "bar", None, clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    val result = DB readOnly { implicit session =>
      sql"""SELECT COUNT(*) FROM bar""".map(rs => rs.int(1)).single().apply()
    }
    result should contain(0)
  }

  it should "log an OK value when being successful" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE table_1(bar INTEGER ); INSERT INTO table_1 VALUES (1);""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcDeletionAction("request", "table_1", Some("bar = 1"), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log an KO value when being unsuccessful" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcDeletionAction("request", "non_existing", Some("bar = 1"), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "throw an IAE when the table name cannot be resolved" in {
    val action = JdbcDeletionAction("request", "${what}", Some("bar = 1"), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when the where clause cannot be resolved" in {
    val action = JdbcDeletionAction("request", "what", Some("${bar} = 1"), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "pass the session to the next action" in {
    val nextAction = NextAction(session)
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE what(nothing INTEGER )""".execute().apply()
    }
    val action = JdbcDeletionAction("request", "what", None, clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }
}
