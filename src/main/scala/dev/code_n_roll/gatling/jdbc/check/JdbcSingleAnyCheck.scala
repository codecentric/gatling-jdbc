package dev.code_n_roll.gatling.jdbc.check

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, SingleArity}
import io.gatling.core.check.{CheckMaterializer, DefaultFindCheckBuilder, Preparer, Specializer}
import io.gatling.core.session._

object JdbcSingleAnyCheck {

  trait JdbcSingleAnyCheckType

  val SingleAnyPreparer: Preparer[ManyAnyResult, Map[String, Any]] = something => something.head.success

  val SingleAnyCheckMaterializer: CheckMaterializer[JdbcSingleAnyCheckType, JdbcCheck, ManyAnyResult, Map[String, Any]] = new CheckMaterializer[JdbcSingleAnyCheckType, JdbcCheck, ManyAnyResult, Map[String, Any]] {
    override protected def preparer: Preparer[ManyAnyResult, Map[String, Any]] = SingleAnyPreparer

    override protected def specializer: Specializer[JdbcCheck, ManyAnyResult] = identity
  }

  val SingleAnyExtractor: Expression[Extractor[Map[String, Any], Map[String, Any]] with SingleArity] =
    new Extractor[Map[String, Any], Map[String, Any]] with SingleArity {
      override def name: String = "singleAny"

      override def apply(prepared: Map[String, Any]): Validation[Option[Map[String, Any]]] = Some(prepared).success
    }.expressionSuccess

  val SingleAnyResult = new DefaultFindCheckBuilder[JdbcSingleAnyCheckType, Map[String, Any], Map[String, Any]](
    SingleAnyExtractor,
    displayActualValue = true
  )
}
