package dev.code_n_roll.gatling.jdbc.check

import java.time.Instant
import java.util

import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

/**
  * Created by ronny on 15.05.17.
  */
class JdbcSimpleCheckSpec extends FlatSpec with Matchers {

  val session = Session("scenario", 0, Instant.now.getEpochSecond)

  implicit val cache: util.Map[Any, Any] = new util.HashMap[Any, Any]()

  "JdbcSimpleCheck" should "log a success if the function returns true" in {
    val check = JdbcSimpleCheck(_ => true)
    val result = check.check(List.empty, session, null)

    result should equal(CheckResult.NoopCheckResultSuccess)
  }

  it should "log a failure if the function returns false" in {
    val check = JdbcSimpleCheck(_ => false)
    val result = check.check(List.empty, session, null)

    result should equal(Failure("JDBC check failed"))
  }

  it should "provide the response list to the function" in {
    val list = List(Map("foo" -> "bar"), Map("bar" -> "foo"))
    val check = JdbcSimpleCheck(response => response eq list)
    val result = check.check(list, session, null)

    result should equal(CheckResult.NoopCheckResultSuccess)
  }
}
