package de.codecentric.gatling.jdbc.protocol

/**
  * Created by ronny on 10.05.17.
  */
case object JdbcProtocolBuilderBase {

  def url(url: String) = JdbcProtocolBuilderUsernameStep(url)

}

case class JdbcProtocolBuilderUsernameStep(url: String) {

  def username(name: String) = JdbcProtocolBuilderPasswordStep(url, name)

}

case class JdbcProtocolBuilderPasswordStep(url: String, username: String) {

  def password(pwd: String) = JdbcProtocolBuilderDriverStep(url, username, pwd)

}

case class JdbcProtocolBuilderDriverStep(url: String, username: String, password: String){

  /**
    * The fully qualified name of the driver as usually loaded by Class.forName(...)
    */
  def driver(driver: String) = JdbcProtocolBuilder(url, username, password, driver)

}

case class JdbcProtocolBuilder(url: String, username: String, pwd: String, driver: String) {

  def build = JdbcProtocol(url, username, pwd, driver)

}
