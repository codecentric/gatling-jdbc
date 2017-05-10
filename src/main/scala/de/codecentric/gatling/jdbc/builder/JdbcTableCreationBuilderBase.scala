package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableCreationActionBuilder
import io.gatling.core.session.Expression

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationBuilderBase() {

  def name(name: Expression[String]): JdbcTableCreationColumnNameStep = JdbcTableCreationBuilder(name)

}

trait JdbcTableCreationColumnNameStep {

  def column(name: Expression[String]): JdbcTableCreationColumnTypeStep
}

trait JdbcTableCreationColumnTypeStep {

  def dataType(columnDataType: Expression[String]): JdbcTableCreationColumnConstraintStep

}

trait JdbcTableCreationColumnConstraintStep {

  def constraint(constraint: Expression[String]): JdbcTableCreationAdditionalColumnStep

  def noConstraint(): JdbcTableCreationAdditionalColumnStep
}

trait JdbcTableCreationAdditionalColumnStep {

  def addColumn(name: Expression[String]): JdbcTableCreationColumnTypeStep

  def create(): JdbcTableCreationActionBuilder

}

case class JdbcTableCreationBuilder(tableName: Expression[String]) extends JdbcTableCreationColumnNameStep with JdbcTableCreationColumnTypeStep with JdbcTableCreationColumnConstraintStep with JdbcTableCreationAdditionalColumnStep {

  private val columns: ArrayBuffer[(Expression[String], Expression[String], Option[Expression[String]])] = ArrayBuffer.empty

  private var newColumnName: Expression[String] = _

  private var newColumnDataType: Expression[String] = _

  private var newColumnConstraint: Option[Expression[String]] = _

  override def column(name: Expression[String]): JdbcTableCreationColumnTypeStep = {
    newColumnName = name
    this
  }

  override def dataType(columnDataType: Expression[String]): JdbcTableCreationColumnConstraintStep = {
    newColumnDataType = columnDataType
    this
  }

  override def constraint(constraint: Expression[String]): JdbcTableCreationAdditionalColumnStep = {
    newColumnConstraint = Some(constraint)
    this
  }

  override def addColumn(name: Expression[String]): JdbcTableCreationColumnTypeStep = {
    val tuple = (newColumnName, newColumnDataType, newColumnConstraint)
    columns += tuple
    newColumnName = name
    newColumnDataType = null
    newColumnConstraint = null
    this
  }

  override def create(): JdbcTableCreationActionBuilder = {
    val tuple = (newColumnName, newColumnDataType, newColumnConstraint)
    columns += tuple
    JdbcTableCreationActionBuilder(tableName, columns)
  }

  override def noConstraint(): JdbcTableCreationAdditionalColumnStep = {
    newColumnConstraint = None
    this
  }
}



