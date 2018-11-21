package de.codecentric.gatling.jdbc.check

import de.codecentric.gatling.jdbc.JdbcCheck
import de.codecentric.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check.extractor.{Extractor, FindAllArity}
import io.gatling.core.check.{CheckMaterializer, DefaultFindCheckBuilder, Preparer, Specializer}
import io.gatling.core.session.{Expression, _}

object JdbcManyAnyCheck {

  trait JdbcManyAnyCheckType

  val ManyAnyPreparer: Preparer[ManyAnyResult, ManyAnyResult] = something => something.success

  val ManyAnyCheckMaterializer: CheckMaterializer[JdbcManyAnyCheckType, JdbcCheck, ManyAnyResult, ManyAnyResult] = new CheckMaterializer[JdbcManyAnyCheckType, JdbcCheck, ManyAnyResult, ManyAnyResult] {
    override protected def preparer: Preparer[ManyAnyResult, ManyAnyResult] = ManyAnyPreparer

    override protected def specializer: Specializer[JdbcCheck, ManyAnyResult] = identity
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
