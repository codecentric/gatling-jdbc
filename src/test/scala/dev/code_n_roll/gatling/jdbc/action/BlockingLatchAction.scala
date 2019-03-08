package dev.code_n_roll.gatling.jdbc.action

import java.util.concurrent.CountDownLatch

import io.gatling.core.action.Action
import io.gatling.core.session.Session

/**
  * This action has a latch with a count 1 on which one can wait in order
  * to test async execution. When execute is being called the latch is opened.
  */
class BlockingLatchAction extends Action{

  val latch: CountDownLatch = new CountDownLatch(1)

  override def name: String = "latch action"

  override def execute(session: Session): Unit = latch.countDown()
}

object BlockingLatchAction{
  def apply(): BlockingLatchAction = new BlockingLatchAction()
}