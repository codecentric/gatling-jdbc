package de.codecentric.gatling.jdbc.check

import de.codecentric.gatling.jdbc.JdbcCheck
import de.codecentric.gatling.jdbc.check.JdbcAnyCheckBuilder.ManyAnyResult
import io.gatling.core.check.DefaultFindCheckBuilder

/**
  * Created by ronny on 15.05.17.
  */
trait JdbcCheckSupport {

  def simpleCheck = JdbcSimpleCheck

  val jdbcSingleResponse: DefaultFindCheckBuilder[JdbcCheck, ManyAnyResult, Map[String, Any], Map[String, Any]] = JdbcAnyCheckBuilder.SingleAnyResult

  val jdbcManyResponse: DefaultFindCheckBuilder[JdbcCheck, ManyAnyResult, ManyAnyResult, ManyAnyResult] = JdbcAnyCheckBuilder.ManyAnyResults
}
