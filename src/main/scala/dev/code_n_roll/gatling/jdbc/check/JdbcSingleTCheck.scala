package dev.code_n_roll.gatling.jdbc.check

import java.util

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check._
import io.gatling.core.session._

object JdbcSingleTCheck {

  trait JdbcSingleTCheckType

  def singleTPreparer[T]: Preparer[List[T], T] = something => something.head.success

  def singleTCheckMaterializer[T]: CheckMaterializer[JdbcSingleTCheckType, JdbcCheck[T], List[T], T] =
    new CheckMaterializer[JdbcSingleTCheckType, JdbcCheck[T], List[T], T](identity) {

      override protected def preparer: Preparer[List[T], T] = singleTPreparer[T]

    }

  def singleTExtractor[T]: Expression[Extractor[T, T]] =
    new Extractor[T, T] {
      override def name: String = "singleT"

      override def apply(prepared: T): Validation[Option[T]] = Some(prepared).success

      override def arity: String = "single"
    }.expressionSuccess

  def singleTResult[T] = new DefaultFindCheckBuilder[JdbcSingleTCheckType, T, T](
    singleTExtractor[T],
    displayActualValue = true
  )
}
