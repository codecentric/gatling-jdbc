package dev.code_n_roll.gatling.jdbc.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.core.Predef._
import io.gatling.core.stats.writer.ResponseMessage
import scalikejdbc._

/**
  * Created by ronny on 15.05.17.
  */
class JdbcInsertActionSpec extends JdbcActionSpec {

  private val clock = new DefaultClock

  "JdbcInsertAction" should "use the request name in the log message" in {
    val requestName = "request"
    val latchAction = BlockingLatchAction()
    val action = JdbcInsertAction(requestName, "table", "", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "insert the specified values" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE insert_me(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcInsertAction("insert", "INSERT_ME", "42", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    val result = DB readOnly { implicit session =>
      sql"""SELECT * FROM INSERT_ME WHERE id = 42 """.map(rs => rs.toMap()).single().apply()
    }
    result should not be empty
  }

  it should "log an OK value when being successful" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE insert_again(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcInsertAction("insert", "INSERT_AGAIN", "42", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log a KO value when being unsuccessful" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcInsertAction("insert", "INSERT_NOBODY", "42", clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "throw an IAE when it cannot evaluate the table expression" in {
    val action = JdbcInsertAction("insert", "${table}", "42", clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when it cannot evaluate the value expression" in {
    val action = JdbcInsertAction("insert", "table", "${value}", clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "pass the session to the next action" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE insert_next(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val nextAction = NextAction(session)
    val action = JdbcInsertAction("insert", "INSERT_NEXT", "42", clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }
}
