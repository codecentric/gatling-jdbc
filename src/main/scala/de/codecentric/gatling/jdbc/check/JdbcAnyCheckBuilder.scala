package de.codecentric.gatling.jdbc.check

import de.codecentric.gatling.jdbc.JdbcCheck
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, FindAllArity, SingleArity}
import io.gatling.core.check.{DefaultFindCheckBuilder, Extender, Preparer}
import io.gatling.core.session._

object JdbcAnyCheckBuilder {

  type ManyAnyResult = List[Map[String, Any]]

  val ManyAnyExtractor: Expression[Extractor[ManyAnyResult, ManyAnyResult] with FindAllArity] =
    new Extractor[ManyAnyResult, ManyAnyResult] with FindAllArity {
      override def name: String = "manyAny"

      override def apply(prepared: ManyAnyResult): Validation[Option[ManyAnyResult]] = Some(prepared).success
    }.expressionSuccess

  val ManyAnyExtender: Extender[JdbcCheck, ManyAnyResult] = check => check

  val ManyAnyPreparer: Preparer[ManyAnyResult, ManyAnyResult] = something => something.success

  val ManyAnyResults = new DefaultFindCheckBuilder[JdbcCheck, ManyAnyResult, ManyAnyResult, ManyAnyResult](
    ManyAnyExtender,
    ManyAnyPreparer,
    ManyAnyExtractor
  )

  val SingleAnyExtractor: Expression[Extractor[Map[String, Any], Map[String, Any]] with SingleArity] =
    new Extractor[Map[String, Any], Map[String, Any]] with SingleArity {
      override def name: String = "singleAny"

      override def apply(prepared: Map[String, Any]): Validation[Option[Map[String, Any]]] = Some(prepared).success
    }.expressionSuccess

  val SingleAnyExtender: Extender[JdbcCheck, ManyAnyResult] = check => check

  val SingleAnyPreparer: Preparer[ManyAnyResult, Map[String, Any]] = something => something.head.success

  val SingleAnyResult = new DefaultFindCheckBuilder[JdbcCheck, ManyAnyResult, Map[String, Any], Map[String, Any]](
    SingleAnyExtender,
    SingleAnyPreparer,
    SingleAnyExtractor
  )
}
