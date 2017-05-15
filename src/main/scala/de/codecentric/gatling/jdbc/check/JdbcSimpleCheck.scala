package de.codecentric.gatling.jdbc.check

import java.sql.ResultSet

import io.gatling.commons.validation.{Failure, Validation}
import io.gatling.core.check.{Check, CheckResult}
import io.gatling.core.session.Session

import scala.collection.mutable

/**
  * Created by ronny on 15.05.17.
  */
case class JdbcSimpleCheck(func: ResultSet => Boolean) extends Check[ResultSet] {
  override def check(response: ResultSet, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
    if (func(response)) {
      CheckResult.NoopCheckResultSuccess
    } else {
      Failure("JDBC check failed")
    }
  }
}
