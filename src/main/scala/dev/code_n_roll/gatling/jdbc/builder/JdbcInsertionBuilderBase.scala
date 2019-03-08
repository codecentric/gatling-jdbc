package dev.code_n_roll.gatling.jdbc.builder

import dev.code_n_roll.gatling.jdbc.action.JdbcInsertionActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcInsertionBuilderBase(requestName: Expression[String]) {

  /**
    * The name can either be the table name (INSERT INTO table_name VALUES (...)) or the table name followed by the column names (INSERT INTO table_name (column1, ...) VALUES (...)).
    * For the latter one has to provide the string "table_name (column1, ...)" INCLUDING the parenthesis.
    */
  def into(name: Expression[String]) = JdbcInsertionValuesStep(requestName, name)

}

case class JdbcInsertionValuesStep(requestName: Expression[String], tableName: Expression[String]) {

  /**
    * Although inserting several values is possible, they should be all in a single string.
    */
  def values(values: Expression[String]) = JdbcInsertionActionBuilder(requestName, tableName, values)

}
