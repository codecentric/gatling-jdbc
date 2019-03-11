package dev.code_n_roll.gatling.jdbc.check

import dev.code_n_roll.gatling.jdbc.JdbcCheck
import dev.code_n_roll.gatling.jdbc.Predef.ManyAnyResult
import io.gatling.core.check._

import scala.annotation.implicitNotFound

/**
  * Created by ronny on 15.05.17.
  */
trait JdbcCheckSupport {

  def simpleCheck = JdbcSimpleCheck

  val jdbcSingleResponse = jdbcSingleResponse[Map[String, Any]]

  def jdbcSingleResponse[T]: DefaultFindCheckBuilder[JdbcSingleTCheck.JdbcSingleTCheckType, Map[String, Any], T] = JdbcSingleTCheck.singleTResult[T]

  implicit def jdbcSingleAnyCheckMaterializer[T]: CheckMaterializer[JdbcSingleTCheck.JdbcSingleTCheckType, JdbcCheck, ManyAnyResult, T] = JdbcSingleTCheck.singleTCheckMaterializer[T]

  val jdbcManyResponse = JdbcManyAnyCheck.ManyAnyResults
  implicit val jdbcManyCheckMaterializer: CheckMaterializer[JdbcManyAnyCheck.JdbcManyAnyCheckType, JdbcCheck, ManyAnyResult, ManyAnyResult] = JdbcManyAnyCheck.ManyAnyCheckMaterializer

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def findCheckBuilder2JdbcCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, P, X])(implicit CheckMaterializer: CheckMaterializer[A, JdbcCheck, ManyAnyResult, P]): JdbcCheck =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def checkBuilder2JdbcCheck[A, P, X](checkBuilder: CheckBuilder[A, P, X])(implicit materializer: CheckMaterializer[A, JdbcCheck, ManyAnyResult, P]): JdbcCheck =
    checkBuilder.build(materializer)

}