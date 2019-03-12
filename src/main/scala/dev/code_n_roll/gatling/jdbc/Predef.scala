package dev.code_n_roll.gatling.jdbc

/**
  * Created by ronny on 10.05.17.
  */
object Predef extends JdbcDsl {

  @Deprecated
  type ManyAnyResult = List[Map[String, Any]]

}
