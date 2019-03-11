package dev.code_n_roll.gatling.jdbc.check

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, SingleArity}
import io.gatling.core.check.{CheckMaterializer, DefaultFindCheckBuilder, Preparer, Specializer}
import io.gatling.core.session._

object JdbcSingleTCheck {

  trait JdbcSingleTCheckType

  def singleTPreparer[T]: Preparer[ManyAnyResult, T] = something => something.head.asInstanceOf[T].success

  def singleTCheckMaterializer[T]: CheckMaterializer[JdbcSingleTCheckType, JdbcCheck, ManyAnyResult, T] = new CheckMaterializer[JdbcSingleTCheckType, JdbcCheck, ManyAnyResult, T] {
    override protected def preparer: Preparer[ManyAnyResult, T] = singleTPreparer[T]

    override protected def specializer: Specializer[JdbcCheck, ManyAnyResult] = identity
  }

  def singleTExtractor[T]: Expression[Extractor[Map[String, Any], T] with SingleArity] =
    new Extractor[Map[String, Any], T] with SingleArity {
      override def name: String = "singleAny"

      override def apply(prepared: Map[String, Any]): Validation[Option[T]] = Some(prepared.asInstanceOf[T]).success
    }.expressionSuccess

  def singleTResult[T] = new DefaultFindCheckBuilder[JdbcSingleTCheckType, Map[String, Any], T](
    singleTExtractor[T],
    displayActualValue = true
  )
}
