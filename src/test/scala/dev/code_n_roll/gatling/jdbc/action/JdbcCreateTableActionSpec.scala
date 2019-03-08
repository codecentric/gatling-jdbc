package dev.code_n_roll.gatling.jdbc.action

import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.core.Predef._
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.stats.writer.ResponseMessage
import org.scalatest.Matchers._
import org.scalatest._
import scalikejdbc._

/**
  * Created by ronny on 12.05.17.
  */
class JdbcCreateTableActionSpec extends JdbcActionSpec {

  private val clock = new DefaultClock

  "JdbcCreateTableAction" should "use the request name in the log message" in {
    val requestName = "name"
    val latchAction = BlockingLatchAction()
    val action = JdbcCreateTableAction(requestName, "table", Seq(column(name("foo"), dataType("INTEGER"))), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].name should equal(requestName)
  }

  it should "create the table with given name and columns" in {
    val action = JdbcCreateTableAction("request", "new_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, next)

    action.execute(session)

    val result = DB readOnly { implicit session =>
      sql"""SELECT * FROM information_schema.tables WHERE TABLE_NAME = 'NEW_TABLE' """.map(rs => rs.toMap()).single().apply()
    }
    result should not be empty
  }

  it should "log an OK message when successfully creating the table" in {
    val latchAction = BlockingLatchAction()
    val action = JdbcCreateTableAction("request", "ok_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, latchAction)

    action.execute(session)

    waitForLatch(latchAction)
    statsEngine.dataWriterMsg should have length 1
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(OK)
  }

  it should "log a KO message if an error occurs" in {
    val latchAction = BlockingLatchAction()
    val latchAction2 = BlockingLatchAction()
    val action = JdbcCreateTableAction("request", "ko_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, latchAction)
    val action2 = JdbcCreateTableAction("request", "ko_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, latchAction2)

    action.execute(session)
    waitForLatch(latchAction)
    action2.execute(session)

    waitForLatch(latchAction2)
    statsEngine.dataWriterMsg should have length 2
    statsEngine.dataWriterMsg.head(session).toOption.get.asInstanceOf[ResponseMessage].status should equal(KO)
  }

  it should "throw an IAE when the column name cannot be validated" in {
    val action = JdbcCreateTableAction("request", "exc_table", Seq(column(name("${foo}"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when the column data type cannot be validated" in {
    val action = JdbcCreateTableAction("request", "exc_table", Seq(column(name("foo"), dataType("${INTEGER}"), constraint("PRIMARY KEY"))), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when the column constraint cannot be validated" in {
    val action = JdbcCreateTableAction("request", "exc_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("${constraint}"))), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "throw an IAE when the table name cannot be validated" in {
    val action = JdbcCreateTableAction("request", "${exc_table}", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, next)

    an[IllegalArgumentException] should be thrownBy action.execute(session)
  }

  it should "pass the session to the next action" in {
    val nextAction = NextAction(session)
    val action = JdbcCreateTableAction("request", "next_table", Seq(column(name("foo"), dataType("INTEGER"), constraint("PRIMARY KEY"))), clock, statsEngine, nextAction)

    action.execute(session)

    waitForLatch(nextAction)
    nextAction.called should be(true)
  }

}
