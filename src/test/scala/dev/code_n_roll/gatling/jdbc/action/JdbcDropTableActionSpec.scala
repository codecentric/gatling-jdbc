package dev.code_n_roll.gatling.jdbc.action

import java.util.concurrent.TimeUnit

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.core.Predef._
import io.gatling.core.stats.writer.ResponseMessage
import scalikejdbc._
/**
  * Created by ronny on 15.05.17.
  */
class JdbcDropTableActionSpec extends JdbcActionSpec {

  private val clock = new DefaultClock

  "JdbcDropTableAction" should "use the request name in the log message" in {
    val requestName = "request"
    val latchAction = BlockingLatchAction()
    val action = JdbcDropTableAction(requestName, "table", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "drop the table with the specified name" in {
    DB autoCommit{ implicit session =>
      sql"""CREATE TABLE delete_me(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcDropTableAction("deleteRequest", "DELETE_ME", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    val result = DB readOnly { implicit session =>
      sql"""SELECT * FROM information_schema.tables WHERE TABLE_NAME = 'DELETE_ME' """.map(rs => rs.toMap()).single().apply()
    }
    result should be(empty)
  }

  it should "throw an IAE if the expression cannot be resolved" in {
    val action = JdbcDropTableAction("deleteRequest", "${table}", clock, statsEngine, next)

    an[IllegalArgumentException] shouldBe thrownBy(action.execute(session))
  }

  it should "log an OK value when being successful" in {
    DB autoCommit{ implicit session =>
      sql"""CREATE TABLE delete_other(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcDropTableAction("deleteRequest", "DELETE_OTHER", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log a KO value when being unsuccessful" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcDropTableAction("deleteRequest", "DELETE_YOU", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "pass the session to the next action" in {
    val nextAction = NextAction(session)
    val action = JdbcDropTableAction("deleteRequest", "DELETE_SOMETHING", clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }
}
