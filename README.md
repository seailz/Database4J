# DatabaseAPI

# Maven
```xml
<repository>
	<id>jitpack.io</id>
	 <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
	<groupId>com.github.seailz</groupId>
	<artifactId>databaseapi</artifactId>
	<version>1.1</version>
	<scope>compile</scope>
</dependency>
```

# Documentation

## Getting Started
First, we need to create our Database instance. There are two ways to do this:
The first way, is add all your values like this:

```java
Database db = new Database(
                "INSERT IP",
                3306,
                "INSERT USERNAME",
                "INSERT PASSWORD",
                "INSERT DATABASE NAME");
```
The code will automatically make a JDBC connection URL for you.

Alternatively, you could use a JDBC connection URL you already have generated like this:
```java
Database db = new Database("INSERT JDBC URL HERE");
```

Finally, we need to initiate the connection, like this:
```java
db.connect(); 
```

## Creating A Table
Great! You've got set up. Here's how to make a simple table!

First, we need to choose what our columns should be. To do that, you need to make an ArrayList of columns, and then add all your columns to it, like this:

```java
List<Column> columns = new ArrayList<>();
        columns.add(
                new Column(
                        CollumType.VARCHAR, "Test Column"
                )
        );
```
Of course you can add more if you like.
The current supported types are:
```java
    VARCHAR,
    INT,
    DOUBLE,
    BOOLEAN,
    BIGINT
```

Once you've done that, all you need to do is to create a new Table instance, like this:
```java
Table table = new Table(
                "TMy Epic Table",
                columns
        );
```

<details>
  <summary>Primary Key</summary>
  If you wish to set a primary key, you can use this:
  
  ```java
   table.setPrimaryKey("INSERT PRIMARY KEY NAME HERE");
```
</details>

Great! Now all there is left to do is tell the database to actually create this table. You can do that like this:

```java
db.createTable(table);
```

There you go! You should now have a table inside of your database.

## Reading from the database
To read from a database, it's reasonably simple.
All you need to do is this:
```java
db.get("INSERT_TABLE_NAME_HERE", "INSERT_KEY_HERE", "INSERT_VALUE_HERE", "INSERT_COLUMN_NAME_HERE");
```

Here's a more detailed explanation on what these parameters mean:

```txt
For example, if you wanted to get the details about a player,
the key parameter would be "name" or whatever it is within your table
and the value parameter would be the player's name of whom you wish to get the details of.

The "column" parameter would be the specific detail you'd like to get. For example,
if my table contained a "age" column, and I wanted to get the player's age,
I'd set the column parameter to "age"
```

## Inserting Into A Database
This is also pretty easy. All you have to do is make a HashMap of your keys, and values, like this:

```java
 HashMap<String, String> values = new HashMap<>();
        values.put("UUID", "epic uuid!");
        values.put("NAME", "SEAILZ");
```

Once you've done that, this is all you need to do:
```java
db.insert("INSERT_TABLE_NAME_HERE", values);
```

## Check if a table exists
Incredibly easy:
```java
db.tableExists("INSERT_TABLE_NAME_HERE");
```

## Deleting from the database
Also pretty easy:
```java
db.delete("INSERT_TABLE_NAME_HERE", "INSERT_KEY_HERE", "INSERT_VALUE_HERE");
```

## Checking if a row exists
Again, pretty easy:
```java
db.rowExists("INSERT_TABLE_HERE", "INSERT_KEY_HERE", "INSERT_VALUE_HERE");
```

## Disconnecting
Once your done interacting with your database, be sure to disconnect like this:
```java
db.disconnect();
```
