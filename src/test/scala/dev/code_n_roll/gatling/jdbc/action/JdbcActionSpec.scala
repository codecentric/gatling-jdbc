package dev.code_n_roll.gatling.jdbc.action

import java.time.Instant
import java.util.concurrent.TimeUnit

import dev.code_n_roll.gatling.jdbc.mock.MockStatsEngine
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import scalikejdbc.{ConnectionPool, GlobalSettings, LoggingSQLAndTimeSettings}

/**
  * Created by ronny on 12.05.17.
  */
trait JdbcActionSpec extends FlatSpec with BeforeAndAfter with BeforeAndAfterAll with Matchers {

  val session = Session("scenario", 0, Instant.now().getEpochSecond)
  val next = new Action {
    override def name: String = "mockAction"

    override def execute(session: Session): Unit = {}
  }
  val statsEngine = new MockStatsEngine

  override def beforeAll(): Unit = {
    Class.forName("org.h2.Driver")
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true, logLevel = 'warn)
    ConnectionPool.singleton("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa")
  }

  before {
    statsEngine.dataWriterMsg = List()
  }

  override def afterAll(): Unit = {
    ConnectionPool.closeAll()
  }


  def waitForLatch(latchAction: BlockingLatchAction): Unit = {
    latchAction.latch.await(2L, TimeUnit.SECONDS) shouldBe true
  }
}

case class NextAction(session: Session, var called: Boolean = false) extends BlockingLatchAction {
  override def name: String = "next Action"

  override def execute(s: Session): Unit = {
    if(s == session) called = true
    super.execute(s)
  }

}


