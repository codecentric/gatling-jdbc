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

In contrast to the previous operations, select directly requires a parameter and is called via `jdbc().select(<what>)`. The intention is to closely resemble the SQL syntax.
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

Currently, checks are only implemented for SELECT. When importing `de.codecentric.gatling.jdbc.Predef._` two types of checks are provided.
The first type is the SimpleCheck.

#### SimpleCheck

The `simpleCheck` method (importet via `Predef`) allows for very basic checks. This method takes a function from `List[Map[String, Any]]` to `Boolean`.
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

There is also another type of check that is more closely integrated with Gatling, the `CheckBuilders`.

#### CheckBuilder

`CheckBuilder` is actually a class provided by Gatling. Based on the Gatling classes, Gatling JDBC provides two types of them.
The `JdbcAnyCheckBuilder` object contains the instances `SingleAnyResult` and `ManyAnyResults`. Both can be used in the tests quickly by calling either `jdbcSingleResponse` or `jdbcManyResponse`.

The difference between the two is that the single response extracts the head out of the list of results. So you can only verify a `Map[String, Any]`.
Whereas the many response, like the simple checks, returns a `List[Map[String, Any]]`. Validation is performed via the Gatling API.
E.g. checking a single result can look like this:
```scala
exec(jdbc("selectionSingleCheck")
  .select("*")
  .from("bar")
  .where("abc=4")
  .check(jdbcSingleResponse.is(Map[String, Any]("ABC" -> 4, "FOO" -> 4)))
)
```
This validates the data in the two columns "ABC" and "FOO". Please note explicit typing of the map. Without it the compiler will complain.

A check with multiple results doesn't look very different:
```scala
exec(jdbc("selectionManyCheck")
  .select("*")
  .from("bar")
  .where("abc=4 OR abc=5")
  .check(jdbcManyResponse.is(List(
    Map("ABC" -> 4, "FOO" -> 4),
    Map("ABC" -> 5, "FOO" -> 5)))
  )
)
```

The advantage those CheckBuilder provide is that they can access certain functionality provided by the Gatling interfaces and classes they extend.
The most important one is the possibility to save the result of a selection to the current session.
By calling `saveAs` after a check you can place the result in the session under the given name. So e.g. if you want to store the result of the single check you can do it like this:
```scala
exec(jdbc("selectionSingleCheckSaving")
  .select("*")
  .from("bar")
  .where("abc=4")
  .check(jdbcSingleResponse.is(Map[String, Any]("ABC" -> 4, "FOO" -> 4))
  .saveAs("myResult"))
)
```

### Final

Covering all SQL operations is a lot of work and some special commands might not be required for performance tests.
Please keep in mind that the state of this Gatling extension can be considered experimental. Feel free to leave comments and create pull requests.

## Publishing

Firstly, you gotta have in your home `.sbt/1.0/sonatype.sbt` configured to contain your username and password for Sonatype.
Secondly, open the sbt shell an perform the following steps:
1. `set pgpSecretRing := file("/home/<user>/.sbt/gpg/secring.asc")` or where ever it is
2. `release`
