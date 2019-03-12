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

  @Deprecated
  val jdbcSingleResponse = singleResponse[Map[String, Any]]

  def singleResponse[T]: DefaultFindCheckBuilder[JdbcSingleTCheck.JdbcSingleTCheckType, T, T] = JdbcSingleTCheck.singleTResult[T]

  implicit def jdbcSingleTCheckMaterializer[T]: CheckMaterializer[JdbcSingleTCheck.JdbcSingleTCheckType, JdbcCheck[T], List[T], T] = JdbcSingleTCheck.singleTCheckMaterializer[T]

  @Deprecated
  val jdbcManyResponse = manyResponse[Map[String, Any]]

  def manyResponse[T]: DefaultFindCheckBuilder[JdbcManyTCheck.JdbcManyTCheckType, List[T], List[T]] = JdbcManyTCheck.manyTResults[T]

  implicit def jdbcTCheckMaterializer[T]: CheckMaterializer[JdbcManyTCheck.JdbcManyTCheckType, JdbcCheck[T], List[T], List[T]] = JdbcManyTCheck.manyTCheckMaterializer[T]

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def findCheckBuilder2JdbcCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, P, X])(implicit CheckMaterializer: CheckMaterializer[A, JdbcCheck[P], List[P], P]): JdbcCheck[P] =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def checkBuilder2JdbcCheck[A, P, X](checkBuilder: CheckBuilder[A, P, X])(implicit materializer: CheckMaterializer[A, JdbcCheck[P], List[P], P]): JdbcCheck[P] =
    checkBuilder.build(materializer)

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def findManyCheckBuilder2JdbcCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, List[P], X])(implicit CheckMaterializer: CheckMaterializer[A, JdbcCheck[P], List[P], List[P]]): JdbcCheck[P] =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for JDBC.")
  implicit def checkManyBuilder2JdbcCheck[A, P, X](checkBuilder: CheckBuilder[A, List[P], X])(implicit materializer: CheckMaterializer[A, JdbcCheck[P], List[P], List[P]]): JdbcCheck[P] =
    checkBuilder.build(materializer)

}