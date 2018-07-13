package de.codecentric.gatling.jdbc.action

import java.util.concurrent.{CountDownLatch, TimeUnit}

import de.codecentric.gatling.jdbc.mock.MockStatsEngine
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers}
import scalikejdbc.ConnectionPool

/**
  * Created by ronny on 12.05.17.
  */
trait JdbcActionSpec extends FlatSpec with BeforeAndAfter with BeforeAndAfterAll with Matchers {

  val session = Session("scenario", 0)
  val next = new Action {
    override def name: String = "mockAction"

    override def execute(session: Session): Unit = {}
  }
  val statsEngine = new MockStatsEngine

  override def beforeAll(): Unit = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa")
  }

  before {
    statsEngine.dataWriterMsg = List()
  }

  override def afterAll(): Unit = {
    ConnectionPool.closeAll()
  }


  def waitForLatch(latchAction: BlockingLatchAction): Boolean = {
    latchAction.latch.await(2L, TimeUnit.SECONDS)
  }
}

case class NextAction(session: Session, var called: Boolean = false) extends BlockingLatchAction {
  override def name: String = "next Action"

  override def execute(s: Session): Unit = {
    if(s == session) called = true
    super.execute(s)
  }

}


