package dev.code_n_roll.gatling.jdbc.check

import java.sql.ResultSet
import java.util

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import io.gatling.commons.validation.{Failure, Validation}
import io.gatling.core.check.{Check, CheckResult}
import io.gatling.core.session.Session
import scalikejdbc.WrappedResultSet

import scala.collection.mutable

/**
  * Created by ronny on 15.05.17.
  */
case class JdbcSimpleCheck(func: List[Map[String, Any]] => Boolean) extends JdbcCheck[Map[String, Any]] {
  override def check(response: List[Map[String, Any]], session: Session, preparedCache: util.Map[Any, Any]): Validation[CheckResult] = {
    if (func(response)) {
      CheckResult.NoopCheckResultSuccess
    } else {
      Failure("JDBC check failed")
    }
  }
}
