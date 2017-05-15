package de.codecentric.gatling.jdbc.check

import io.gatling.commons.validation.{Failure, Success}
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

/**
  * Created by ronny on 15.05.17.
  */
class JdbcSimpleCheckSpec extends FlatSpec with Matchers {

  val session = Session("scenario", 0)

  implicit val cache: mutable.Map[Any, Any] = mutable.Map.empty

  "JdbcSimpleCheck" should "log a success if the function returns true" in {
    val check = JdbcSimpleCheck(_ => true)
    val result = check.check(List.empty, session)

    result shouldBe a[Success[CheckResult]]
  }

  it should "log a failure if the function returns false" in {
    val check = JdbcSimpleCheck(_ => false)
    val result = check.check(List.empty, session)

    result shouldBe a[Failure]
  }

  it should "provide the response list to the function" in {
    val list = List(Map("foo" -> "bar"), Map("bar" -> "foo"))
    val check = JdbcSimpleCheck(response => response eq list)
    val result = check.check(list, session)

    result shouldBe a[Success[CheckResult]]
  }
}
