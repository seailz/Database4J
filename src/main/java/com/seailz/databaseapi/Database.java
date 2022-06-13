package com.seailz.databaseapi;

import com.seailz.databaseapi.core.table.Column;
import com.seailz.databaseapi.core.table.ColumnType;
import com.seailz.databaseapi.core.table.Table;
import com.seailz.databaseapi.core.Statement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>A way to interact with databases easier than JDBC.</p>
 * <p></p>
 * <p>I won't leave a full documentation here, but if you want to read</p>
 * <p>how to use this in more detail, you can find it <a href="https://github.com/Negative-Games/Framework/pull/108#issue-1265327573">here</a> </p>
 * <p></p>
 * <p>{@code Creating a Database Instance}</p>
 * <p>To create a database instance, you have to do this:</p>
 * <pre>
 *     Database db = new Database("ip", port, "username", "password", "databaseName");
 *     db.connect();
 * </pre>
 *
 * <p>This will connect to the database for you.</p>
 * <p></p>
 * <p>{@code Creating a Table}</p>
 * <p>To create a table, you have to do this:</p>
 * <pre>
 *     Table table = new Table("tableName");
 *     table.addColumn(new Column("columnName", ColumnType.EXAMPLE_TYPE));
 *     db.createTable(table);
 * </pre>
 * <p></p>
 * <p>{@code Inserting into a table}</p>
 * <p>To insert into a table, you have to do this:</p>
 * <pre>
 *     HashMap<String, String> values = new HashMap<>();
 *     values.put("columnName", "value");
 *     db.insert("tableName", "columnName", values);
 * </pre>
 * <p></p>
 * <p>{@code Closing the connection}</p>
 * <p>To close the connection, you have to do this:</p>
 * <pre>
 *     db.disconnect();
 * </pre>
 * <p></p>
 * <p>Again, a more detailed documentation can be found here <a href="https://github.com/Negative-Games/Framework/pull/108#issue-1265327573">here</a> </p>
 * @author Seailz - <a href="https://www.seailz.com">Website</a>
 */
@Getter
@Setter
public class Database {

    private boolean debug;
    private boolean inTransaction;

    private String ip;
    private int port;
    private String username;
    private String password;
    private String databaseName;

    private Connection connection;

    /**
     * Create a database instance
     * @param ip The ip which you would like to connect to
     * @param port The port on which the database is hosted
     * @param username The username you'd like to use
     * @param password The password you'd like to use.
     * @param databaseName The name of the database
     * @author Seailz
     */
    public Database(@NotNull String ip, int port, @NotNull String username, @NotNull String password, @NotNull String databaseName) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        setIp(ip);
        setPort(port);
        setUsername(username);
        setPassword(password);
        setDatabaseName(databaseName);
    }

    /**
     * Create a database instance
     * @param ip The ip which you would like to connect to
     * @param port The port on which the database is hosted
     * @param username The username you'd like to use
     * @param password The password you'd like to use.
     * @param databaseName The name of the database
     * @param debug Whether you'd like to debug the database
     * @author Seailz
     */
    public Database(@NotNull String ip, int port, @NotNull String username, @NotNull String password, @NotNull String databaseName, boolean debug) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        setIp(ip);
        setPort(port);
        setUsername(username);
        setPassword(password);
        setDatabaseName(databaseName);
        setDebug(debug);

        if (debug)
            System.out.println("[Database] Debugging enabled");
    }

    /**
     * Initiate the connection to the database
     * @author Seailz
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + getIp() + ":" + getPort() + "/" + getDatabaseName(),
                getUsername(),
                getPassword()
        );

        if (debug)
            System.out.println("[Database] Connected to database");
    }

    /**
     * Disconnect from the database
     * @throws SQLException If the connection is already closed
     * @author Seailz
     */
    public void disconnect() throws SQLException {
        connection.close();
        if (debug)
            System.out.println("[Database] Disconnected from database");
    }

    /**
     * Creates a table within the Database
     * @param table The table you would like to create
     * @throws IllegalStateException If the arraylist is empty
     * @author Seailz
     */
    public void createTable(@NotNull Table table) throws SQLException, IllegalStateException {
        StringBuilder statement = new StringBuilder("CREATE TABLE `" + table.getName() + "` (\n");

        Column last = table.getColumns().get(table.getColumns().size() - 1);
        if (!table.getColumns().stream().findFirst().isPresent())
            throw new IllegalStateException("Empty ArrayList");
        Column first = table.getColumns().stream().findFirst().get();
        for (Column column : table.getColumns()) {
            String type = column.getType().toString();
            String name = column.getName();

            if (first == column)
                statement.append("\t`").append(name).append("` ").append(type);
            else
                statement.append("\n\t`").append(name).append("` ").append(type);


            statement.append("(").append(column.getLength()).append(")");

            if (!column.isAllowNull())
                statement.append(" NOT NULL");


            if (!last.equals(column))
                statement.append(",");

        }

        if (table.getPrimaryKey() != null)
            statement.append(",\n\tPRIMARY KEY (`").append(table.getPrimaryKey()).append("`)");

        statement.append("\n);");

        if (debug)
            System.out.println("[Database] Creating table: " + statement.toString());

        new Statement(statement.toString(), connection).execute();
    }

    /**
     * Start a transaction
     * @throws SQLException if there is an error with the connection
     * @throws IllegalStateException if the connection is already in a transaction
     * @author Seailz
     */
    public void startTransaction() throws SQLException, IllegalStateException {
        if (isInTransaction())
            throw new IllegalStateException("Transaction already started");

        connection.setAutoCommit(false);
        new Statement("START TRANSACTION", connection).execute();

        if (debug)
            System.out.println("[Database] Started transaction");
    }

    /**
     * Rollback a transaction
     * @throws SQLException if there is an error with the connection
     * @throws IllegalStateException if the connection is not in a transaction
     * @author Seailz
     */
    public void rollback() throws SQLException, IllegalStateException {
        if (!isInTransaction())
            throw new IllegalStateException("No transaction to rollback");
        new Statement("ROLLBACK", connection).execute();

        if (debug)
            System.out.println("[Database] Rolled back transaction");
    }

    /**
     * Commit a transaction
     * @throws SQLException if there is an error with the connection
     * @throws IllegalStateException if there is no transaction to commit
     * @author Seailz
     */
    public void commit() throws SQLException, IllegalStateException {
        if (!isInTransaction())
            throw new IllegalStateException("No transaction to commit");

        new Statement("COMMIT", connection).execute();
        connection.setAutoCommit(true);

        if (debug)
            System.out.println("[Database] Committed transaction");
    }

    /**
     * Get something from the database
     * <p></p>
     * <p>For example, if you wanted to get the details about a player,</p>
     * <p>the key parameter would be "name" or whatever it is within your table</p>
     * <p>and the value parameter would be the player's name of whom you wish to get the details of.</p>
     * <p></p>
     * <p>The "column" parameter would be the specific detail you'd like to get. For example, </p>
     * <p>if my table contained a "age" column, and I wanted to get the player's age,</p>
     * <p>I'd set the column parameter to "age"</p>
     * <p>
     *
     * @param table the table you'd like to pull from
     * @param key The key you'd like to check
     * @param value The value that you'd like to check
     * @param column The column you'd like to get
     * @return An object
     * @throws SQLException if there is an error retrieving the request value
     * @author Seailz
     */
    public Object get(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull String column) throws SQLException {
        String statement = "SELECT * FROM '" + table + "'";
        ResultSet set = new Statement(statement, connection).executeWithResults();

        while (set.next()) {
            if (set.getObject(key).equals(value))
                return set.getObject(column);
        }
        if (debug)
            System.out.println("[Database] Getting value: " + statement);
        return null;
    }

    /**
     * Check if a table exists
     * @param tableName The table you'd like to check
     * @return A boolean if the table exists or not
     * @throws SQLException If there is an error
     * @author Seailz
     */
    public boolean tableExists(@NotNull String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        if (debug)
            System.out.println("[Database] Checking if table exists: " + tableName);
        return resultSet.next();
    }

    /**
     * Insert into a database
     * @param table The table you'd like to insert to
     * @param values A hashmap of keys, and values
     * @throws SQLException if there is an error
     * @author Seailz
     */
    public void insert(@NotNull String table, @NotNull HashMap<String, String> values) throws SQLException {
        StringBuilder statement = new StringBuilder("insert into '" + table + "' (");

        ArrayList<String> keysArray = new ArrayList<>(values.keySet());
        String lastKey = keysArray.get(keysArray.size() - 1);
        for (String key : values.keySet()) {
            if (!key.equals(lastKey))
                statement.append("'").append(key).append("', ");
            else
                statement.append("'").append(key).append(")");
        }

        statement.append(" values (");

        ArrayList<String> valuesArray = new ArrayList<>(values.values());
        String lastValue = valuesArray.get(valuesArray.size() - 1);
        for (String value : values.values()) {
            if (!value.equals(lastValue))
                statement.append("?, ");
            else
                statement.append("?)");
        }

        if (debug)
            System.out.println(statement);

        PreparedStatement prepStatement = connection.prepareStatement(statement.toString());
        int i = 0;

        for (String value : values.values()) {
            i++;
            prepStatement.setObject(i, value);
        }

        if (debug)
            System.out.println("[Database] Inserting into table: " + statement.toString());
        prepStatement.execute();
    }

    /**
     * Delete a row rom the database
     * @param table The table you'd like to edit
     * @param key The key, basically the identifier
     * @param value The value, such as the player's name
     * @author Seailz
     */
    public void delete(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "DELETE FROM '" + table + "' WHERE '" + key + "'='" + value + "'";
        new Statement(statement, connection).execute();
        if (debug)
            System.out.println("[Database] Deleting from table: " + statement);
    }

    /**
     * Check if a row exists
     * @param table The table you'd like to check
     * @param key The key
     * @param value The value
     * @return whether that row exists
     * @throws SQLException if there is an error connecting to the database
     * @author Seailz
     */
    public boolean rowExists(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "SELECT * FROM `" + table + "` WHERE '" + key + "'='" + value + "'";
        if (debug)
            System.out.println("[Database] Checking if row exists: " + statement);
        return new Statement(statement, connection).executeWithResults().next();
    }

    /**
     * Replace a current row with a new one
     * @param table The table in which the row is located
     * @param key The key you would like to check
     * @param value the value of that key
     * @param values the values of the new row you'd like to insert
     * @throws SQLException If there's an error communicating with the database
     * @author Seailz
     */
    public void replace(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull HashMap<String, String> values) throws SQLException {
        if (!rowExists(table, key, value)) return; // Trying to prevent as many errors as possible :/

        if (debug)
            System.out.println("[Database] Replacing row: " + table + "." + key + "=" + value);

        delete(table, key, value);
        insert(table, values);
    }

    /**
     * Delete a table
     * @param name The name of the table you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void deleteTable(@NotNull String name) throws SQLException {
        if (!tableExists(name)) return;
        if (debug)
            System.out.println("[Database] Deleting table: " + name);
        new Statement("DROP TABLE " + name + ";", connection).execute();
    }

    /**
     * Update a row in a table
     * @param table The table you'd like to update
     * @param key The key you'd like to check
     * @param value The value you'd like to check
     * @param column The column you'd like to update
     * @param newColumn The new value you'd like to insert
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void update(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull String column, @NotNull String newColumn) throws SQLException {
        String statement = "UPDATE `" + table + "` SET `" + column + "`=`" + newColumn + "` WHERE `" + key + "`='" + value + "'";
        if (debug)
            System.out.println("[Database] Updating row: " + statement);
        new Statement(statement, connection).execute();
    }


    /**
     * Update a table in the database
     * @param table The table you'd like to update
     * @param column The column you'd like to update
     * @param type The type of the column
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void addColumnToTable(String table, String column, String type) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` ADD `" + column + "` " + type + ";";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Remove a column from a table
     * @param table The table you'd like to remove a column from
     * @param column The column you'd like to remove
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void removeColumnFromTable(String table, String column) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`;";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Change a column's name
     * @param table The table you'd like to change a column's name in
     * @param oldName The old name of the column
     * @param newName The new name of the column
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void changeColumnName(String table, String oldName, String newName) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` CHANGE `" + oldName + "` `" + newName + "`;";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Delete a column from a table
     * @param table The table you'd like to delete a column from
     * @param column The column you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void deleteColumnFromTable(String table, String column) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`;";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Export a table to a file
     * @param table The table you'd like to export
     * @param filePath The file's path you'd like to export to
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void exportToCSV(String table, String filePath) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        if (debug)
            System.out.println("[Database] Exporting table: " + statement);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();
        try {
            FileWriter writer = new FileWriter(filePath);
            while (resultSet.next()) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    writer.write(resultSet.getString(i));
                    if (i != resultSet.getMetaData().getColumnCount())
                        writer.write(",");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Import a table from a file
     * @param table The table you'd like to import into
     * @param filePath The file's path you'd like to import from
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void importFromFile(String table, String filePath) throws SQLException {
        String statement = "LOAD DATA INFILE '" + filePath + "' INTO TABLE `" + table + "`";
        if (debug)
            System.out.println("[Database] Importing table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Count the number of rows in a table
     * @param table The table you'd like to count
     * @return The number of rows in the table
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public int countRows(String table) throws SQLException {
        String statement = "SELECT COUNT(*) FROM `" + table + "`";
        if (debug)
            System.out.println("[Database] Counting rows: " + statement);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();
        resultSet.next();
        return resultSet.getInt(1);
    }

    /**
     * Get all tables in the database
     * @return A list of all tables in the database
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public ResultSet getAllTables() throws SQLException {
        String statement = "SHOW TABLES";
        if (debug)
            System.out.println("[Database] Getting all tables: " + statement);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Get all data in a table
     * @param table The table you'd like to get data from
     * @return A list of all data in the table
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public ResultSet getAllDataInTable(String table) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        if (debug)
            System.out.println("[Database] Getting all data in table: " + statement);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Delete a table if it exists
     * @param table The table you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void deleteTableIfExists(String table) throws SQLException {
        String statement = "DROP TABLE IF EXISTS `" + table + "`";
        if (debug)
            System.out.println("[Database] Deleting table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Replace the primary key of a table
     * @param table The table you'd like to replace the primary key in
     * @param primaryKey The new primary key
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void replacePrimaryKey(String table, String primaryKey) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` DROP PRIMARY KEY, ADD PRIMARY KEY (`" + primaryKey + "`);";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Copies the contents of one table to another
     * @param table The table you'd like to copy to
     * @param copyFrom The table you'd like to copy from
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public void copyContentsToNewTable(String table, String copyFrom) throws SQLException {
        String statement = "INSERT INTO `" + table + "` SELECT * FROM `" + copyFrom + "`;";
        if (debug)
            System.out.println("[Database] Altering table: " + statement);
        new Statement(statement, connection).execute();
    }

    /**
     * Describe a table
     * @param table The table you'd like to describe
     * @return The description of the table
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public ResultSet describeTable(String table) throws SQLException {
        String statement = "DESCRIBE `" + table + "`";
        if (debug)
            System.out.println("[Database] Describing table: " + statement);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Describe a column in a table
     * @param table The table you'd like to describe
     * @param column The column you'd like to describe
     * @return The description of the column
     * @throws SQLException if there is an error communicating with the database
     * @author Seailz
     */
    public ResultSet describeColumn(String table, String column) throws SQLException {
        String statement = "DESCRIBE `" + table + "` `" + column + "`";
        if (debug)
            System.out.println("[Database] Describing column: " + statement);
        return new Statement(statement, connection).executeWithResults();
    }
}