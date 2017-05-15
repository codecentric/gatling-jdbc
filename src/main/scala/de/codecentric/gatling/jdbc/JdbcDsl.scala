package de.codecentric.gatling.jdbc

import io.gatling.core.session.Expression
import de.codecentric.gatling.jdbc.protocol.{JdbcProtocol, JdbcProtocolBuilder, JdbcProtocolBuilderBase}
import de.codecentric.gatling.jdbc.builder.JdbcActionBuilderBase
import de.codecentric.gatling.jdbc.check.JdbcCheckSupport

import scala.language.implicitConversions

/**
  * Created by ronny on 10.05.17.
  */
trait JdbcDsl extends JdbcCheckSupport {

  val jdbc = JdbcProtocolBuilderBase

  def jdbc(requestName: Expression[String]) = JdbcActionBuilderBase(requestName)

  implicit def jdbcProtocolBuilder2JdbcProtocol(protocolBuilder: JdbcProtocolBuilder): JdbcProtocol = protocolBuilder.build

}
