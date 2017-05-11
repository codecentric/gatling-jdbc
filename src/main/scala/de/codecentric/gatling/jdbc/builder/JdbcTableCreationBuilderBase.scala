package de.codecentric.gatling.jdbc.builder

import de.codecentric.gatling.jdbc.action.JdbcTableCreationActionBuilder
import io.gatling.core.session.Expression

import scala.collection.mutable.ArrayBuffer

/**
  * Created by ronny on 10.05.17.
  */
case class JdbcTableCreationBuilderBase(requestName: Expression[String]) {

  def name(name: Expression[String]): JdbcTableCreationColumnNameStep = JdbcTableCreationBuilder(requestName, name)

}

trait JdbcTableCreationColumnNameStep {

  def column(name: Expression[String]): JdbcTableCreationColumnTypeStep
}

trait JdbcTableCreationColumnTypeStep {

  def dataType(columnDataType: Expression[String]): JdbcTableCreationColumnConstraintStep

}

trait JdbcTableCreationColumnConstraintStep extends JdbcTableCreationColumnNameStep with JdbcTableCreationFinalStep {

  def constraint(constraint: Expression[String]): JdbcTableCreationAdditionalColumnStep

}

trait JdbcTableCreationAdditionalColumnStep extends JdbcTableCreationColumnNameStep with JdbcTableCreationFinalStep

trait JdbcTableCreationFinalStep {

  def create(): JdbcTableCreationActionBuilder

}

case class JdbcTableCreationBuilder(requestName: Expression[String], tableName: Expression[String]) extends JdbcTableCreationColumnNameStep with JdbcTableCreationColumnTypeStep with JdbcTableCreationColumnConstraintStep with JdbcTableCreationAdditionalColumnStep {

  private val columns: ArrayBuffer[(Expression[String], Expression[String], Option[Expression[String]])] = ArrayBuffer.empty

  private var newColumnName: Expression[String] = _

  private var newColumnDataType: Expression[String] = _

  private var newColumnConstraint: Option[Expression[String]] = None

  override def column(name: Expression[String]): JdbcTableCreationColumnTypeStep = {
    if (newColumnName != null && newColumnDataType != null) {
      val tuple = (newColumnName, newColumnDataType, newColumnConstraint)
      columns += tuple
    }
    newColumnName = name
    newColumnDataType = null
    newColumnConstraint = None
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

  override def create(): JdbcTableCreationActionBuilder = {
    val tuple = (newColumnName, newColumnDataType, newColumnConstraint)
    columns += tuple
    JdbcTableCreationActionBuilder(requestName, tableName, columns)
  }

}



