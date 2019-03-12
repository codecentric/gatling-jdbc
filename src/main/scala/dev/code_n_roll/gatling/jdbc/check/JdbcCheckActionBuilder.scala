package dev.code_n_roll.gatling.jdbc.check

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import io.gatling.core.action.builder.ActionBuilder

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 15.05.17.
  */
trait JdbcCheckActionBuilder[T] extends ActionBuilder {

  protected val checks: ArrayBuffer[JdbcCheck[T]] = ArrayBuffer.empty

  def check(check: JdbcCheck[T]): ActionBuilder = {
    checks += check
    this
  }

}
