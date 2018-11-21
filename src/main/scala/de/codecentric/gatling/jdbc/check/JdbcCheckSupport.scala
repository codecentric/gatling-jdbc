package de.codecentric.gatling.jdbc.check

import de.codecentric.gatling.jdbc.JdbcCheck
import de.codecentric.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.core.check._

import scala.annotation.implicitNotFound

/**
  * Created by ronny on 15.05.17.
  */
trait JdbcCheckSupport {

  def simpleCheck = JdbcSimpleCheck

  val jdbcSingleResponse = JdbcSingleAnyCheck.SingleAnyResult
  implicit val jdbcSingleAnyCheckMaterializer: CheckMaterializer[JdbcSingleAnyCheck.JdbcSingleAnyCheckType, JdbcCheck, ManyAnyResult, Map[String, Any]] = JdbcSingleAnyCheck.SingleAnyCheckMaterializer

  val jdbcManyResponse = JdbcManyAnyCheck.ManyAnyResults
  implicit val jdbcManyCheckMaterializer: CheckMaterializer[JdbcManyAnyCheck.JdbcManyAnyCheckType, JdbcCheck, ManyAnyResult, ManyAnyResult] = JdbcManyAnyCheck.ManyAnyCheckMaterializer

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def findCheckBuilder2JdbcCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, P, X])(implicit CheckMaterializer: CheckMaterializer[A, JdbcCheck, ManyAnyResult, P]): JdbcCheck =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def checkBuilder2JdbcCheck[A, P, X](checkBuilder: CheckBuilder[A, P, X])(implicit materializer: CheckMaterializer[A, JdbcCheck, ManyAnyResult, P]): JdbcCheck =
    checkBuilder.build(materializer)

}