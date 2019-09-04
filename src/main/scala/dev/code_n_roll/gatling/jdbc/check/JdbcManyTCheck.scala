package dev.code_n_roll.gatling.jdbc.check

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check._
import io.gatling.core.session.{Expression, _}

object JdbcManyTCheck {

  trait JdbcManyTCheckType

  def manyTPreparer[T]: Preparer[List[T], List[T]] = something => something.success

  def manyTCheckMaterializer[T]: CheckMaterializer[JdbcManyTCheckType, JdbcCheck[T], List[T], List[T]] =
    new CheckMaterializer[JdbcManyTCheckType, JdbcCheck[T], List[T], List[T]](identity) {

      override protected def preparer: Preparer[List[T], List[T]] = manyTPreparer

    }

  def manyTExtractor[T]: Expression[Extractor[List[T], List[T]]] =
    new Extractor[List[T], List[T]] {
      override def name: String = "manyT"

      override def apply(prepared: List[T]): Validation[Option[List[T]]] = Some(prepared).success

      override def arity: String = "findAll"
    }.expressionSuccess

  def manyTResults[T] = new DefaultFindCheckBuilder[JdbcManyTCheckType, List[T], List[T]](
    manyTExtractor,
    displayActualValue = true
  )
}
