package dev.code_n_roll.gatling.jdbc.check

import java.util

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, FindAllArity}
import io.gatling.core.check._
import io.gatling.core.session.{Expression, _}

object JdbcManyAnyCheck {

  trait JdbcManyAnyCheckType

  val ManyAnyPreparer: Preparer[ManyAnyResult, ManyAnyResult] = something => something.success

  val ManyAnyCheckMaterializer: CheckMaterializer[JdbcManyAnyCheckType, JdbcCheck[Map[String, Any]], ManyAnyResult, ManyAnyResult] = new CheckMaterializer[JdbcManyAnyCheckType, JdbcCheck[Map[String, Any]], ManyAnyResult, ManyAnyResult] {
    override protected def preparer: Preparer[ManyAnyResult, ManyAnyResult] = ManyAnyPreparer

    override protected def specializer: Specializer[JdbcCheck[Map[String, Any]], ManyAnyResult] = identity
  }

  val ManyAnyExtractor: Expression[Extractor[ManyAnyResult, ManyAnyResult] with FindAllArity] =
    new Extractor[ManyAnyResult, ManyAnyResult] with FindAllArity {
      override def name: String = "manyAny"

      override def apply(prepared: ManyAnyResult): Validation[Option[ManyAnyResult]] = Some(prepared).success
    }.expressionSuccess

  val ManyAnyResults = new DefaultFindCheckBuilder[JdbcManyAnyCheckType, ManyAnyResult, ManyAnyResult](
    ManyAnyExtractor,
    displayActualValue = true
  )
}
