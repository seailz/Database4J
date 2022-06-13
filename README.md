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
	<version>46c3d69fed8a158a03b2e3ae1665feb4004dea28</version>
	<scope>compile</scope>
</dependency>
```

# Documentation

## Getting Started
First, we need to create our Database instance:
```java
Database db = new Database(
                "INSERT IP",
                3306,
                "INSERT USERNAME",
                "INSERT PASSWORD",
                "INSERT DATABASE NAME");
```
The code will automatically make a JDBC connection URL for you.
Finally, we need to initiate the connection, like this:
```java
db.connect(); 
```

## Creating A Table
Great! You've got set up. Here's how to make a simple table!

First, we need to choose what our columns should be. To do that, you need to make an ArrayList of columns, and then add all your columns to it, like this:

```java
List<Column> columns = new ArrayList<>();
        
        Column column = new Column(ColumnType.VARCHAR, "Test Column");

        columns.add(column);
```

<details>
<summary> Custom Lengths </summary>

By default the column's length is set to 255, but your able to change that like this:
```java
column.setLength(INSERT_LENGTH_HERE);
```
</details>
<details>
<summary> Disallow Null </summary>

If you want to deny null in your column, all you need to do is this:
```java
column.setAllowNull(false); // (by default this is set to true)
```

</details>

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

## Transactions (PLEASE READ)
Before we move on I want to leave an important note. Transactions are an essential part of working on a database and makes sure you don't make any errors. Here's how to use them:

Starting a transaction:
```java
db.startTransaction();
```

Committing a transaction:
```java
 db.commit();
```

Rolling back a transaction:
```java
db.rollback();
```

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

## Sending your own commands
Do you want to send you own MySQL commands to your database easily? Well read on :)

There's a custom class called "Statement" which can be used to easily send statements to your server. All you need to do is this:

```java
Statement s = new Statement("INSERT_MYSQL_COMMAND_HERE", db.getConnection();
```

Now to execute the command, you have two options:

<details>
<summary> With Results Set </summary>
This should be used if the command is supposed to return some results. Here's his:

```java
ResultsSet r = s.executeWithResults();
```
</details>
<details>
<summary> Without Results Set </summary>
This should be used when the command is not expected to return results. For example, creating a new table.

```java
s.execute();
```

</details>

## Disconnecting
Once your done interacting with your database, be sure to disconnect like this:
```java
db.disconnect();
```

Other than that there are a bunch of other weird and wonderful things added to the database class, so make sure to check it out! It has about every single MySQL command I could find! (thanks copilot). Here's a few highlights

- Exporting and importing from a CSV file
- Counting the amount of rows in a table
- Altering columns
