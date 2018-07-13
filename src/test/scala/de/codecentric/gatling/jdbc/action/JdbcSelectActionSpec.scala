package de.codecentric.gatling.jdbc.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef._
import io.gatling.core.stats.writer.ResponseMessage
import scalikejdbc._
import de.codecentric.gatling.jdbc.Predef._

/**
  * Created by ronny on 15.05.17.
  */
class JdbcSelectActionSpec extends JdbcActionSpec {

  "JdbcSelectAction" should "use the request name in the log message" in {
    val requestName = "simulation"
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction(requestName, "*", "table", None, List.empty, statsEngine, next)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "select all values without where clause" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE selection(id INTEGER PRIMARY KEY ); INSERT INTO SELECTION VALUES (1);INSERT INTO SELECTION VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "SELECTION", None, List(simpleCheck(list => list.length == 2)), statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "select values specified by where clause" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE limited(id INTEGER PRIMARY KEY ); INSERT INTO LIMITED VALUES (1);INSERT INTO LIMITED VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "LIMITED", Some("id=2"), List(simpleCheck(list => list.length == 1)), statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log an OK value after successful selection" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE success(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "SUCCESS", None, List.empty, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log an KO value after unsuccessful selection" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "failure", None, List.empty, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "log a KO value if a check fails" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE checkTable(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "CHECKTABLE", None, List(simpleCheck(_ => false)), statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "log a OK value if a check is successful" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE check_again(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "CHECK_AGAIN", None, List(simpleCheck(_ => true)), statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "throw an IAE when it cannot evaluate the what expression" in {
    val action = JdbcSelectAction("request", "${what}", "table", None, List(simpleCheck(_ => true)), statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when it cannot evaluate the from expression" in {
    val action = JdbcSelectAction("request", "*", "${from}", None, List(simpleCheck(_ => true)), statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when it cannot evaluate the where expression" in {
    val action = JdbcSelectAction("request", "*", "table", Some("${where}"), List(simpleCheck(_ => true)), statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "pass the session to the next action" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE insert_next(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val nextAction = NextAction(session)
    val action = JdbcSelectAction("request", "*", "INSERT_NEXT", None, List(simpleCheck(_ => true)), statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }
}
