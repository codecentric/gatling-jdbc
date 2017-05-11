package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableCreationActionBuilder
import de.codecentric.gatling.jdbc.builder.column.ColumnDefinition
import io.gatling.core.session.Expression

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationBuilderBase(requestName: Expression[String]) {

  def name(name: Expression[String]) = JdbcTableCreationColumnsStep(requestName, name)

}

case class JdbcTableCreationColumnsStep(requestName: Expression[String], tableName: Expression[String]) {

  def columns(column: ColumnDefinition, moreColumns: ColumnDefinition*) = JdbcTableCreationActionBuilder(requestName, tableName, column +: moreColumns)

}
