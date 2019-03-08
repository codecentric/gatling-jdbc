package dev.code_n_roll.gatling.jdbc

import io.gatling.core.session.Expression
import dev.code_n_roll.gatling.jdbc.protocol.{JdbcProtocol, JdbcProtocolBuilder, JdbcProtocolBuilderBase, JdbcProtocolBuilderConnectionPoolSettingsStep}
import dev.code_n_roll.gatling.jdbc.builder.JdbcActionBuilderBase
import dev.code_n_roll.gatling.jdbc.check.JdbcCheckSupport

import scala.language.implicitConversions

/**
  * Created by ronny on 10.05.17.
  */
trait JdbcDsl extends JdbcCheckSupport {

  val jdbc = JdbcProtocolBuilderBase

  def jdbc(requestName: Expression[String]) = JdbcActionBuilderBase(requestName)

  implicit def jdbcProtocolBuilder2JdbcProtocol(protocolBuilder: JdbcProtocolBuilder): JdbcProtocol = protocolBuilder.build

  implicit def jdbcProtocolBuilderConnectionPoolSettingsStep2JdbcProtocol(protocolBuilder: JdbcProtocolBuilderConnectionPoolSettingsStep): JdbcProtocol = protocolBuilder.build

}
