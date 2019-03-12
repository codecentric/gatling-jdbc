package dev.code_n_roll.gatling.jdbc.check

import java.util

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, SingleArity}
import io.gatling.core.check._
import io.gatling.core.session._

object JdbcSingleTCheck {

  trait JdbcSingleTCheckType

  def singleTPreparer[T]: Preparer[List[T], T] = something => something.head.asInstanceOf[T].success

  def singleTCheckMaterializer[T]: CheckMaterializer[JdbcSingleTCheckType, JdbcCheck[T], List[T], T] = new CheckMaterializer[JdbcSingleTCheckType, JdbcCheck[T], List[T], T] {
    override protected def preparer: Preparer[List[T], T] = singleTPreparer[T]

    override protected def specializer: Specializer[JdbcCheck[T], List[T]] = identity
  }

  def singleTExtractor[T]: Expression[Extractor[T, T] with SingleArity] =
    new Extractor[T, T] with SingleArity {
      override def name: String = "singleAny"

      override def apply(prepared: T): Validation[Option[T]] = Some(prepared).success
    }.expressionSuccess

  def singleTResult[T] = new DefaultFindCheckBuilder[JdbcSingleTCheckType, T, T](
    singleTExtractor[T],
    displayActualValue = true
  )
}
