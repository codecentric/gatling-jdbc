package dev.code_n_roll.gatling.jdbc.action

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef._
import io.gatling.core.stats.writer.ResponseMessage
import scalikejdbc._
import dev.code_n_roll.gatling.jdbc.Predef._
import io.gatling.commons.util.DefaultClock

/**
  * Created by ronny on 15.05.17.
  */
class JdbcSelectActionSpec extends JdbcActionSpec {

  private val clock = new DefaultClock

  "JdbcSelectAction" should "use the request name in the log message" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE test_table(id INTEGER PRIMARY KEY );""".execute().apply()
    }
    val requestName = "simulation"
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction(requestName, "*", "test_table", None, List.empty, _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "add the exception message to the log message" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("simulation", "*", "non-existing-table", None, List.empty, _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].message shouldNot be(empty)
  }

  it should "select all values without where clause" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE selection(id INTEGER PRIMARY KEY ); INSERT INTO SELECTION VALUES (1);INSERT INTO SELECTION VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "SELECTION", None, List(simpleCheck(list => list.length == 2)), _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "select values specified by where clause" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE limited(id INTEGER PRIMARY KEY ); INSERT INTO LIMITED VALUES (1);INSERT INTO LIMITED VALUES (2)""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "LIMITED", Some("id=2"), List(simpleCheck(list => list.length == 1)), _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log an OK value after successful selection" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE success(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "SUCCESS", None, List.empty, _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log an KO value after unsuccessful selection" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "failure", None, List.empty, _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "log a KO value if a check fails" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE checkTable(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "CHECKTABLE", None, List(simpleCheck(_ => false)), _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "log a OK value if a check is successful" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE check_again(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val latchAction = BlockingLatchAction()
    val action = JdbcSelectAction("request", "*", "CHECK_AGAIN", None, List(simpleCheck(_ => true)), _.toMap(), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "throw an IAE when it cannot evaluate the what expression" in {
    val action = JdbcSelectAction("request", "${what}", "table", None, List(simpleCheck(_ => true)), _.toMap(), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when it cannot evaluate the from expression" in {
    val action = JdbcSelectAction("request", "*", "${from}", None, List(simpleCheck(_ => true)), _.toMap(), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when it cannot evaluate the where expression" in {
    val action = JdbcSelectAction("request", "*", "table", Some("${where}"), List(simpleCheck(_ => true)), _.toMap(), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "pass the session to the next action" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE insert_next(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val nextAction = NextAction(session)
    val action = JdbcSelectAction("request", "*", "INSERT_NEXT", None, List(simpleCheck(_ => true)), _.toMap(), clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }

  it should "pass the session to the next action even when a check crashes" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE crashes(id INTEGER PRIMARY KEY )""".execute().apply()
    }
    val nextAction = NextAction(session.markAsFailed)
    val action = JdbcSelectAction("request", "*", "CRASHES", None, List(simpleCheck(_ => throw new RuntimeException("Test error"))), _.toMap(), clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }

  it should "apply the map function to the selection and store it in the session" in {
    DB autoCommit { implicit session =>
      sql"""CREATE TABLE mapping(id INTEGER PRIMARY KEY ); INSERT INTO mapping VALUES (1);""".execute().apply()
    }
    val nextAction = new BlockingLatchAction() {
      override def execute(session: Session): Unit = {
        session("value").as[Mapping] shouldEqual Mapping(1)
        super.execute(session)
      }
    }
    val action: JdbcSelectAction[Mapping] = JdbcSelectAction(
      "request",
      "*",
      "MAPPING",
      None,
      List(singleResponse[Mapping].is(Mapping(1)).saveAs("value")),
      rs => Mapping(rs.int("id")),
      clock,
      statsEngine,
      nextAction)

    action.execute(session)

    waitForLatch(nextAction)
  }

  case class Mapping(id: Int)

}
