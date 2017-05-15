package de.codecentric.gatling.jdbc.check

import de.codecentric.gatling.jdbc.JdbcCheck
import io.gatling.core.action.builder.ActionBuilder

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 15.05.17.
  */
trait JdbcCheckActionBuilder extends ActionBuilder {

  protected val checks: ArrayBuffer[JdbcCheck] = ArrayBuffer.empty

  def check(check: JdbcCheck): ActionBuilder = {
    checks += check
    this
  }

}
