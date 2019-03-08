package dev.code_n_roll.gatling.jdbc.builder.column

import io.gatling.core.session.Expression

/**
  * Created by ronny on 11.05.17.
  */
object ColumnHelper {

  def column(name: ColumnName, dataType: ColumnDataType): ColumnDefinition = ColumnDefinition(name, dataType, None)

  def column(name: ColumnName, dataType: ColumnDataType, columnConstraint: ColumnConstraint): ColumnDefinition = ColumnDefinition(name, dataType, Some(columnConstraint))

  def name(name: Expression[String]) = ColumnName(name)

  def dataType(dataType: Expression[String]) = ColumnDataType(dataType)

  def constraint(constraint: Expression[String]) = ColumnConstraint(constraint)

}

case class ColumnName(name: Expression[String])

case class ColumnDataType(dataType: Expression[String])

case class ColumnConstraint(constraint: Expression[String])

case class ColumnDefinition(name: ColumnName, dataType: ColumnDataType, columnConstraint: Option[ColumnConstraint])
