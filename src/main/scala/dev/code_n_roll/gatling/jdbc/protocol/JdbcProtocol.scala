package dev.code_n_roll.gatling.jdbc.protocol

import io.gatling.core.{CoreComponents, protocol}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import scalikejdbc.ConnectionPool.CPSettings
import scalikejdbc._

/**
  * Created by ronny on 10.05.17.
  */
class JdbcProtocol(url: String, username: String, pwd: String, driver: String, connectionPoolSettings: Option[CPSettings]) extends Protocol {

  Class.forName(driver)

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)

  if (connectionPoolSettings.isDefined) {
    ConnectionPool.singleton(url, username, pwd, connectionPoolSettings.get)
  } else {
    ConnectionPool.singleton(url, username, pwd)
  }
}

object JdbcProtocol {

  val jdbcProtocolKey: ProtocolKey[JdbcProtocol, JdbcComponents] = new ProtocolKey[JdbcProtocol, JdbcComponents] {

    override def protocolClass: Class[protocol.Protocol] = classOf[JdbcProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): JdbcProtocol =
      throw new IllegalStateException("Can't provide a default value for JdbcProtocol")

    override def newComponents(coreComponents: CoreComponents): JdbcProtocol => JdbcComponents = {
      protocol => JdbcComponents(protocol)
    }

  }

  def apply(url: String, username: String, pwd: String, driver: String, connectionPoolSettings: Option[CPSettings]): JdbcProtocol =
    new JdbcProtocol(url, username, pwd, driver, connectionPoolSettings)
}