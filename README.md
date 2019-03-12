# Gatling JDBC Extension
JDBC support for Gatling

[![Build Status](https://travis-ci.org/rbraeunlich/gatling-jdbc.svg?branch=master)](https://travis-ci.org/rbraeunlich/gatling-jdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.code_n_roll.gatling/jdbc-gatling_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/dev.code_n_roll.gatling/jdbc-gatling_2.12)


The JDBC extension for Gatling was originally created to accompany a blog post that shows how to extend Gatling.
Currently, five SQL operations are being supported. See below for the usage.

## :exclamation: Attention :exclamation:

In order to avoid conflicts with `io.gatling:gatling-jdbc` the artifact name has been changed with version 2.0.1.
Instead of `gatling-jdbc` it is now called `jdbc-gatling` (see issue #8). Apart from this, nothing changes. All package names etc. stayed the same.

Also, by forking it from it's original position the group id and the packages have changed!
The correct import is now `dev.code_n_roll.gatling....` and the group id changed to `dev.code-n-roll.gatling`.

## Usage

```scala
libraryDependencies += "dev.code_n_roll.gatling" %% "gatling-jdbc" % "version"
```

### General

In order to use the JDBC functionality, your simulation has to import `dev.code_n_roll.gatling.jdbc.Predef._`.
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
import dev.code_n_roll.gatling.jdbc.builder.column.ColumnHelper._` was created. It is recommended to use it. The datatype of every column has to be provided as a string.
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

Currently, checks are only implemented for SELECT. When importing `dev.code_n_roll.gatling.jdbc.Predef._` two types of checks are provided.
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
The two types are basically `JdbcSingeTCheck` and `JdbcManyTCheck`. As the names suggest, both options allow to use the concrete types of the expected response.
To use them, you have to use the methods `singleResponse[T]` or `manyResponse[T]`.
For compatibility reasons there are also the fields `jdbcSingleResponse` and `jdbcManyResponse` which set the type to a `Map[String, Any]`.
The methods and the fields are in scope when importing the complete Predef objects.

The difference between `singleResponse` and `manyResponse` is that the former extracts the head out of the list of results. So you can only verify a single object.
Whereas the many response, like the simple checks, a `List[T]` is returned. Validation is performed via the Gatling API.
Checking a single result in the untyped way can look like this:
```scala
exec(jdbc("selectionSingleCheck")
  .select("*")
  .from("bar")
  .where("abc=4")
  .check(jdbcSingleResponse.is(("ABC" -> 4, "FOO" -> 4)))
)
```
This validates the data in the two columns "ABC" and "FOO". Please note explicit typing of the map. Without it the compiler will complain.
If you want to use the typed API, you have to provide a function that defines the mapping:
```scala
case class Stored(abc: Int, foo: Int)
...
exec(jdbc("selectionSingleCheck")
      .select("*")
      .from("bar")
      .where("abc=4")
      .mapResult(rs => Stored(rs.int("abc"), rs.int("foo")))
      .check(singleResponse[Stored].is(Stored(4, 4))
    ).pause(1)

```
For checking multiple results we can again use the deprecated, untyped way:
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
Or use the typed API:
```scala
case class Stored(abc: Int, foo: Int)
...
exec(jdbc("selectionManyCheck")
  .select("*")
  .from("bar")
  .where("abc=4 OR abc=5")
  .mapResult(rs => Stored(rs.int("abc"), rs.int("foo")))
  .check(manyResponse[Stored].is(List(
    Stored(4, 4),
    Stored(5, 5)))
  )
```
Please note that the map function defines the mapping for a single result. You don't have to map the result set into a List of your objects.

The advantage those CheckBuilder provide is that they can access certain functionality provided by the Gatling interfaces and classes they extend.
The most important one is the possibility to save the result of a selection to the current session.
By calling `saveAs` after a check you can place the result in the session under the given name. So e.g. if you want to store the result of the single check you can do it like this:
```scala
exec(jdbc("selectionSingleCheckSaving")
  .select("*")
  .from("bar")
  .where("abc=4")
  .check(singleResponse.is(("ABC" -> 4, "FOO" -> 4))
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

## Executing the intergration tests

If you have to run Docker on your machine as sudo, then to execute the integration tests, sbt has to be started as sudo, too.
Only `sudo sbt gatling:test` will then be allowed to start the container of the databases.

## Acknowledgements

I'd like to thank my former employer [codecentric](https://github.com/codecentric) for providing me the time and space to
get started on this project and transform it to a publishable library.