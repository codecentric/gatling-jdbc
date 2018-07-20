# gatling-jdbc
JDBC support for Gatling

[![Build Status](https://travis-ci.org/codecentric/gatling-jdbc.svg?branch=master)](https://travis-ci.org/codecentric/gatling-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/gatling-jdbc_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/gatling-jdbc_2.12)


The JDBC extension for Gatling was originally created to accompany a blog post that shows how to extend Gatling.
Currently, five SQL operations are being supported. See below for the usage.

## Usage

```scala
libraryDependencies += "de.codecentric" %% "gatling-jdbc" % "version"
```

### General

In order to use the JDBC functionality, your simulation has to import `de.codecentric.gatling.jdbc.Predef._`.
The JDBC configuration is done via `jdbc`, e.g.:

```scala
val jdbcConfig = jdbc
  .url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE")
  .username("sa")
  .password("sa")
  .driver("org.h2.Driver")
```
Those are currently all the options that can be provided and that have to be provided.

The entry point for the operations is the `jdbc()` method. The method itself takes a request name as parameter. This name will appear in the reports to represent the operation that follows.

### CREATE TABLE

Creating a table is done via `jdbc().create()`. In order to ease the creation of arbitrarily many columns, the helper class `
import de.codecentric.gatling.jdbc.builder.column.ColumnHelper._` was created. It is recommended to use it. The datatype of every column has to be provided as a string.
Additionally, constraints are optional, but also have to be passed as strings. E.g.:
```scala
scenario("createTable").
    exec(jdbc("bar table")
      .create()
      .table("bar")
      .columns(
        column(
          name("abc"),
          dataType("INTEGER"),
          constraint("PRIMARY KEY")
        ),
        column(
          name("ac"),
          dataType("INTEGER")
        )
      )
    )
```

### INSERT

Insertion is done via `jdbc().insert()`. For where to insert, two options are possible. Suppose you have the table from the example above. You can insert the values either by relying on the indices:
```scala
exec(jdbc("insertion")
  .insert()
  .into("bar")
  .values("${n}, ${n}")
)
```
or by using the column names:
```scala
exec(jdbc("insertion")
  .insert()
  .into("bar (abc, ac)")
  .values("${n}, ${n}")
)
```

### SELECT

In contrast to the previous operations, select directly requires a parameter and is called via `jdbc().select(<what>)'. The intention is to closely resemble the SQL syntax.
Using `where()` is optional for SELECT. Therefore, the following two ways are both valid:
```scala
exec(jdbc("selection")
  .select("*")
  .from("bar")
)
```
and
```scala
exec(jdbc("selection")
  .select("*")
  .from("bar")
  .where("abc=4")
)
```
Of course, as parameter to `select()`, every column or combination of columns can be used, as with basic SQL.

### DELETE

Deletion starts from `jdbc().delete()`. Here, the where clause is optional again. In order to delete certain values the following works:
```scala
repeat(5, "n"){
    exec(jdbc("deletion")
        .delete()
        .from("bar")
        .where("abc=${n}")  
    )
}
```
Alternatively, in order to delete everything:
```scala
exec(jdbc("deletion")
    .delete()
    .from("bar")
)
```
Please be careful, since no additional validation is being performed and you might lose some data.

### DROP TABLE

The last operation that is being supported is DROP TABLE via `jdbc().drop()` The method only takes a single parameter, the table name. Please be careful again, which table you drop.
Dropping the "bar" table from the first example can be done in the following way:
```scala
jdbc("drop bar table").drop().table("bar")
```

### Checks

Currently, checks are only implemented for SELECT. When importing `de.codecentric.gatling.jdbc.Predef._` the `simpleCheck` method is already provided. This method takes a function from `List[Map[String, Any]]` to `Boolean`.
Each element in the list represents a row and the map the individual columns. Checks are simply appended to the selection, e.g.:
```scala
exec(jdbc("selection")
  .select("*")
  .from("bar")
  .where("abc=4")
  .check(simpleCheck(result => result.head("FOO") == 4))
)
```
A SELECT without a WHERE clause can also be validated with a `simpleCheck`.

### Final

Covering all SQL operations is a lot of work and some special commands might not be required for performance tests.
Please keep in mind that the state of this Gatling extension can be considered experimental. Feel free to leave comments and create pull requests.

## Publishing

Firstly, you gotta have in your home `.sbt/1.0/sonatype.sbt` configured to contain your username and password for Sonatype.
Secondly, open the sbt shell an perform the following steps:
1. `set pgpSecretRing := file("/home/<user>/.sbt/gpg/secring.asc")` or where ever it is
2. `publishSigned`
3. `sonatypeRelease`
