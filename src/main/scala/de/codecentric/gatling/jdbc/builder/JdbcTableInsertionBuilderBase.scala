package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableInsertionActionBuilder
import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
case class JdbcTableInsertionBuilderBase(requestName: Expression[String]) {

  /**
    * The name can either be the table name (INSERT INTO table_name VALUES (...)) or the table name followed by the column names (INSERT INTO table_name (column1, ...) VALUES (...)).
    * For the latter one has to provide the string "table_name (column1, ...)" INCLUDING the parenthesis.
    */
  def into(name: Expression[String]) = JdbcTableInsertionValuesStep(requestName, name)

}

case class JdbcTableInsertionValuesStep(requestName: Expression[String], tableName: Expression[String]) {

  /**
    * Although inserting several values is possible, they should be all in a single string.
    */
  def values(values: Expression[String]) = JdbcTableInsertionActionBuilder(requestName, tableName, values)

}
