package com.seailz.databaseapi;

import com.seailz.databaseapi.annotation.DontSave;
import com.seailz.databaseapi.annotation.builder.InsertBuilder;
import com.seailz.databaseapi.annotation.builder.LoginBuilder;
import com.seailz.databaseapi.annotation.builder.TableBuilder;
import com.seailz.databaseapi.annotation.builder.general.WhereBuilder;
import com.seailz.databaseapi.annotation.constructor.DatabaseConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * <p>A way to interact with databases easier than JDBC.</p>
 * <p></p>
 * <p>I won't leave a full documentation here, but if you want to read</p>
 * <p>how to use this in more detail, you can find it <a href="https://github.com/Negative-Games/Framework/wiki/">here</a> </p>
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
 * <p>{@code Creating a TableBuilder}</p>
 * <p>To create a table, you have to do this:</p>
 * <pre>
 *     TableBuilder table = new TableBuilder("tableName");
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
 * <p>{@code Inserting Java Objects}</p>
 * <p>To insert Java objects into a table, you have to do this:</p>
 * <pre>
 *     db.writeObjectToTable("tableName", new Object());
 * </pre>
 * <p>Again, a more detailed documentation can be found <a href="https://github.com/Negative-Games/Framework/wiki/">here</a> </p>
 *
 * @author Seailz
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
    private File sqlLiteFile;

    private Connection connection;

    /**
     * Create a database instance with MySQL
     *
     * @param ip           The ip which you would like to connect to
     * @param port         The port on which the database is hosted
     * @param username     The username you'd like to use
     * @param password     The password you'd like to use.
     * @param databaseName The name of the database
     */
    @SneakyThrows
    public Database(@NotNull String ip, int port, @NotNull String username, @NotNull String password, @NotNull String databaseName) {
        this(ip, port, username, password, databaseName, false);
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    /**
     * Create a database instance using {@link LoginBuilder} with MySQL
     *
     * @param loginBuilder The {@link LoginBuilder} you'd like to use
     */
    @SneakyThrows
    public Database(@NotNull LoginBuilder loginBuilder) {
        this(loginBuilder.getIp(), loginBuilder.getPort(), loginBuilder.getUsername(), loginBuilder.getPassword(), loginBuilder.getDatabase());
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    /**
     * Create a database instance with MySQL
     *
     * @param ip           The ip which you would like to connect to
     * @param port         The port on which the database is hosted
     * @param username     The username you'd like to use
     * @param password     The password you'd like to use.
     * @param databaseName The name of the database
     * @param debug        Whether you'd like to debug the database
     */
    @SneakyThrows
    public Database(@NotNull String ip, int port, @NotNull String username, @NotNull String password, @NotNull String databaseName, boolean debug) {
        setIp(ip);
        setPort(port);
        setUsername(username);
        setPassword(password);
        setDatabaseName(databaseName);
        setDebug(debug);

        Class.forName("com.mysql.cj.jdbc.Driver");

        if (debug)
            log("Debugging enabled");
    }

    /**
     * Creates a database instance with SQLite
     *
     * @param file The file which you would like to use
     */
    public Database(File file) {
        setSqlLiteFile(file);
    }

    /**
     * Initiate the connection to the database
     */
    @SneakyThrows
    public void connect() {
        if (getSqlLiteFile() != null) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + getSqlLiteFile().getAbsolutePath());
            return;
        }

        connection = DriverManager.getConnection(
                "jdbc:mysql://" + getIp() + ":" + getPort() + "/" + getDatabaseName(),
                getUsername(),
                getPassword()
        );

        if (debug)
            log("Connected to database");
    }

    /**
     * Disconnect from the database
     */
    @SneakyThrows
    public void disconnect() {
        connection.close();
        if (debug)
            log("Disconnected from database");
    }

    /**
     * Creates a table within the Database
     *
     * @param table The table you would like to create
     * @throws IllegalStateException If the arraylist is empty
     */
    public void createTable(@NotNull TableBuilder table) throws SQLException, IllegalStateException {
        StringBuilder statement = new StringBuilder("CREATE TABLE `" + table.getName() + "` (\n");

        if (table.getColumns().isEmpty())
            throw new IllegalStateException("There are no columns for table " + table.getName() + ".");

        Column first = table.getColumns().get(0);
        Column last = table.getColumns().get(table.getColumns().size() - 1);
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
            log("Creating table " + table.getName() + ": " + statement.toString());

        new Statement(statement.toString(), connection).execute();

        table.getColumns().forEach(column -> {
            if (column.getDefaultValue() != null) {
                try {
                    setColumnDefaultValue(table.getName(), column.getName(), column.getDefaultValue());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Start a transaction
     *
     * @throws SQLException          if there is an error with the connection
     * @throws IllegalStateException if the connection is already in a transaction
     */
    public void startTransaction() throws SQLException, IllegalStateException {
        if (isInTransaction())
            throw new IllegalStateException("Transaction already started");

        connection.setAutoCommit(false);
        new Statement("START TRANSACTION", connection).execute();

        if (debug)
            log("Started transaction");
    }

    /**
     * Rollback a transaction
     *
     * @throws SQLException          if there is an error with the connection
     * @throws IllegalStateException if the connection is not in a transaction
     */
    public void rollback() throws SQLException, IllegalStateException {
        if (!isInTransaction())
            throw new IllegalStateException("No transaction to rollback");
        new Statement("ROLLBACK", connection).execute();

        if (debug)
            log("Rolled back transaction");
    }

    /**
     * Commit a transaction
     *
     * @throws SQLException          if there is an error with the connection
     * @throws IllegalStateException if there is no transaction to commit
     */
    public void commit() throws SQLException, IllegalStateException {
        if (!isInTransaction())
            throw new IllegalStateException("No transaction to commit");

        new Statement("COMMIT", connection).execute();
        connection.setAutoCommit(true);

        if (debug)
            log("Committed transaction");
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
     * @param table  the table you'd like to pull from
     * @param key    The key you'd like to check
     * @param value  The value that you'd like to check
     * @param column The column you'd like to get
     * @return An object
     * @throws SQLException if there is an error retrieving the request value
     */
    @Nullable
    public Object get(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull String column) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        ResultSet set = new Statement(statement, connection).executeWithResults();

        if (debug)
            log("Getting " + column + " from " + table + " where " + key + " = " + value);

        while (set.next()) {
            if (set.getObject(key).equals(value))
                return set.getObject(column);
        }
        if (debug)
            log("Getting value from table " + table + " failed");
        return null;
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
     * @param table  the table you'd like to pull from
     * @param key    The key you'd like to check
     * @param value  The value that you'd like to check
     * @param column The column you'd like to get
     * @return An object
     * @throws SQLException if there is an error retrieving the request value
     */
    @Nullable
    public Optional<List<Object>> getList(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull String column) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        ResultSet set = new Statement(statement, connection).executeWithResults();

        if (debug)
            log("Getting " + column + " from " + table + " where " + key + " = " + value);

        List<Object> objects = new ArrayList<>();
        while (set.next()) {
            if (set.getObject(key).equals(value))
                objects.add(set.getObject(column));
        }

        if (debug)
            log("Getting value from table " + table + " failed");

        if (objects.isEmpty())
            return Optional.empty();
        return Optional.of(objects);
    }

    /**
     * Check if a table exists
     *
     * @param tableName The table you'd like to check
     * @return A boolean if the table exists or not
     * @throws SQLException If there is an error
     */
    public boolean tableExists(@NotNull String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        if (debug)
            log("Checking if table exists: " + tableName);
        return resultSet.next();
    }

    /**
     * Insert into a database
     *
     * @param table  The table you'd like to insert to
     * @param values A hashmap of keys, and values
     * @throws SQLException if there is an error
     */
    public void insert(@NotNull String table, @NotNull HashMap<String, String> values) throws SQLException {
        StringBuilder statement = new StringBuilder("insert into `" + table + "` (\n\t");

        ArrayList<String> keysArray = new ArrayList<>(values.keySet());
        String lastKey = keysArray.get(keysArray.size() - 1);
        for (String key : values.keySet()) {
            if (!key.equals(lastKey))
                statement.append(key).append(",");
            else
                statement.append(key).append("\n)\n\t");
        }

        statement.append(" values (\n\t");

        ArrayList<String> valuesArray = new ArrayList<>(values.values());
        String lastValue = valuesArray.get(valuesArray.size() - 1);
        for (String value : values.values()) {
            if (!value.equals(lastValue))
                statement.append("?, ");
            else
                statement.append("?\n);");
        }

        if (debug)
            log(String.valueOf(statement));

        PreparedStatement prepStatement = connection.prepareStatement(statement.toString());
        int i = 0;

        for (String value : values.values()) {
            i++;
            prepStatement.setObject(i, value);
        }

        if (debug)
            log("Inserting into table: " + table + " with values: " + values);
        prepStatement.executeUpdate();
    }

    /**
     * Insert into a database
     *
     * @param builder The builder you'd like to use
     * @throws SQLException if there is an error
     */
    public void insert(@NotNull InsertBuilder builder) throws SQLException {
        StringBuilder statement = new StringBuilder("insert into `" + builder.getTable() + "` (\n\t");

        ArrayList<String> keysArray = new ArrayList<>(builder.getValues().keySet());
        String lastKey = keysArray.get(keysArray.size() - 1);
        for (String key : builder.getValues().keySet()) {
            if (!key.equals(lastKey))
                statement.append(key).append(",");
            else
                statement.append(key).append("\n)\n\t");
        }

        statement.append(" values (\n\t");

        ArrayList<String> valuesArray = new ArrayList<>(builder.getValues().values());
        String lastValue = valuesArray.get(valuesArray.size() - 1);
        for (String value : builder.getValues().values()) {
            if (!value.equals(lastValue))
                statement.append("?, ");
            else
                statement.append("?\n);");
        }

        if (debug)
            log(String.valueOf(statement));

        PreparedStatement prepStatement = connection.prepareStatement(statement.toString());
        int i = 0;

        for (String value : builder.getValues().values()) {
            i++;
            prepStatement.setObject(i, value);
        }

        if (debug)
            log("Inserting into table: " + builder.getTable() + " with values: " + builder.getValues());
        prepStatement.executeUpdate();
    }

    /**
     * Delete a row rom the database
     *
     * @param table The table you'd like to edit
     * @param key   The key, basically the identifier
     * @param value The value, such as the player's name
     */
    public void delete(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "DELETE FROM `" + table + "` WHERE '" + key + "'='" + value + "'";
        new Statement(statement, connection).execute();
        if (debug)
            log("Deleting from table: " + table + " with key: " + key + " and value: " + value);
    }

    /**
     * Check if a row exists
     *
     * @param table The table you'd like to check
     * @param key   The key
     * @param value The value
     * @return whether that row exists
     * @throws SQLException if there is an error connecting to the database
     */
    public boolean rowExists(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "SELECT * FROM `" + table + "` WHERE '" + key + "'='" + value + "'";
        if (debug)
            log("Checking if row exists: " + statement);
        return new Statement(statement, connection).executeWithResults().next();
    }

    /**
     * Check if a row exists
     *
     * @param table   The table you'd like to check
     * @param builder The builder you'd like to use
     * @return whether that row exists
     * @throws SQLException if there is an error connecting to the database
     */
    public boolean rowExists(@NotNull String table, @NotNull WhereBuilder builder) throws SQLException {
        String statement = "SELECT * FROM `" + table + "` WHERE '" + builder.getKey() + "'='" + builder.getValue() + "'";
        if (debug)
            log("Checking if row exists: " + statement);
        return new Statement(statement, connection).executeWithResults().next();
    }

    /**
     * Replace a current row with a new one
     *
     * @param table  The table in which the row is located
     * @param key    The key you would like to check
     * @param value  the value of that key
     * @param values the values of the new row you'd like to insert
     * @throws SQLException If there's an error communicating with the database
     */
    public void replace(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull HashMap<String, String> values) throws SQLException {
        if (!rowExists(table, key, value)) return;

        if (debug)
            log("Replacing row in table: " + table + " with key: " + key + " and value: " + value);

        delete(table, key, value);
        insert(table, values);
    }

    /**
     * Replace a current row with a new one
     *
     * @param table        The table in which the row is located
     * @param whereBuilder The where builder you'd like to use
     * @param values       the values of the new row you'd like to insert
     * @throws SQLException If there's an error communicating with the database
     */
    public void replace(@NotNull String table, @NotNull WhereBuilder whereBuilder, @NotNull HashMap<String, String> values) throws SQLException {
        if (!rowExists(table, whereBuilder.getKey(), whereBuilder.getValue())) return;

        if (debug)
            log("Replacing row in table: " + table + " with key: " + whereBuilder.getKey() + " and value: " + whereBuilder.getValue());

        delete(table, whereBuilder.getKey(), whereBuilder.getValue());
        insert(table, values);
    }

    /**
     * Delete a table
     *
     * @param name The name of the table you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     */
    public void deleteTable(@NotNull String name) throws SQLException {
        if (!tableExists(name)) return;
        if (debug)
            log("Deleteing table: " + name);
        new Statement("DROP TABLE " + name + ";", connection).execute();
    }

    /**
     * Update a row in a table
     *
     * @param table        The table you'd like to update
     * @param whereBuilder The where builder you'd like to use
     * @param column       The column you'd like to update
     * @param newColumn    The new value you'd like to insert
     * @throws SQLException if there is an error communicating with the database
     */
    public void update(@NotNull String table, @NotNull WhereBuilder whereBuilder, @NotNull String column, @NotNull String newColumn) throws SQLException {
        String statement = "UPDATE `" + table + "` SET `" + column + "`=`" + newColumn + "` WHERE `" + whereBuilder.getKey() + "`='" + whereBuilder.getValue() + "'";
        if (debug)
            log("Updating row with table: " + table + " with key: " + whereBuilder.getKey() + " and value: " + whereBuilder.getValue() + " with column: " + column + " and new value: " + newColumn);
        new Statement(statement, connection).execute();
    }


    /**
     * Update a table in the database
     *
     * @param table  The table you'd like to update
     * @param column The column you'd like to update
     * @param type   The type of the column
     * @throws SQLException if there is an error communicating with the database
     */
    public void addColumnToTable(String table, String column, String type, int amount) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` ADD `" + column + "` " + type + "(" + amount + ");";
        if (debug)
            log("Adding column to table: " + table + " with name: " + column + " and type: " + type);
        new Statement(statement, connection).execute();
    }

    /**
     * Remove a column from a table
     *
     * @param table  The table you'd like to remove a column from
     * @param column The column you'd like to remove
     * @throws SQLException if there is an error communicating with the database
     */
    public void removeColumnFromTable(String table, String column) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`;";
        if (debug)
            log("Removing column: " + column + " from table: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Change a column's name
     *
     * @param table   The table you'd like to change a column's name in
     * @param oldName The old name of the column
     * @param newName The new name of the column
     * @throws SQLException if there is an error communicating with the database
     */
    public void changeColumnName(String table, String oldName, String newName) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` CHANGE `" + oldName + "` `" + newName + "`;";
        if (debug)
            log("Changing column name: " + oldName + " to " + newName + " in table: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Delete a column from a table
     *
     * @param table  The table you'd like to delete a column from
     * @param column The column you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     */
    public void deleteColumnFromTable(String table, String column) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`;";
        if (debug)
            log("Deleteing column: " + column + " from table: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Export a table to a file
     *
     * @param table    The table you'd like to export
     * @param filePath The file's path you'd like to export to
     * @throws SQLException if there is an error communicating with the database
     */
    public void exportToCSV(String table, String filePath) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        if (debug)
            log("Exporting table: " + table + " to file: " + filePath);
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
     *
     * @param table    The table you'd like to import into
     * @param filePath The file's path you'd like to import from
     * @throws SQLException if there is an error communicating with the database
     */
    public void importFromFile(String table, String filePath) throws SQLException {
        String statement = "LOAD DATA INFILE '" + filePath + "' INTO TABLE `" + table + "`";
        if (debug)
            log("Importing table: " + table + " from file: " + filePath);
        new Statement(statement, connection).execute();
    }

    /**
     * Count the number of rows in a table
     *
     * @param table The table you'd like to count
     * @return The number of rows in the table
     * @throws SQLException if there is an error communicating with the database
     */
    public int countRows(String table) throws SQLException {
        String statement = "SELECT COUNT(*) FROM `" + table + "`";
        if (debug)
            log("Counting rows in table: " + table);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();
        resultSet.next();
        return resultSet.getInt(1);
    }

    /**
     * Get all tables in the database
     *
     * @return A list of all tables in the database
     * @throws SQLException if there is an error communicating with the database
     */
    public ResultSet getAllTables() throws SQLException {
        String statement = "SHOW TABLES";
        if (debug)
            log("Getting all tables");
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Get all data in a table
     *
     * @param table The table you'd like to get data from
     * @return A list of all data in the table
     * @throws SQLException if there is an error communicating with the database
     */
    public ResultSet getAllDataInTable(String table) throws SQLException {
        String statement = "SELECT * FROM `" + table + "`";
        if (debug)
            log("Getting all data in table: " + table);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Delete a table if it exists
     *
     * @param table The table you'd like to delete
     * @throws SQLException if there is an error communicating with the database
     */
    public void deleteTableIfExists(String table) throws SQLException {
        String statement = "DROP TABLE IF EXISTS `" + table + "`";
        if (debug)
            log("Deleting table if it exists: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Replace the primary key of a table
     *
     * @param table      The table you'd like to replace the primary key in
     * @param primaryKey The new primary key
     */
    public void replacePrimaryKey(String table, String primaryKey) {
        String statement = "ALTER TABLE `" + table + "` DROP PRIMARY KEY, ADD PRIMARY KEY (`" + primaryKey + "`);";
        if (debug)
            log("Changing primary key of table: " + table + " to: " + primaryKey);
        new Statement(statement, connection).execute();
    }

    /**
     * Copies the contents of one table to another
     *
     * @param table    The table you'd like to copy to
     * @param copyFrom The table you'd like to copy from
     * @throws SQLException if there is an error communicating with the database
     */
    public void copyContentsToNewTable(String table, String copyFrom) throws SQLException {
        String statement = "INSERT INTO `" + table + "` SELECT * FROM `" + copyFrom + "`;";
        if (debug)
            log("Copying contents from table: " + copyFrom + " to table: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Describe a table
     *
     * @param table The table you'd like to describe
     * @return The description of the table
     * @throws SQLException if there is an error communicating with the database
     */
    public ResultSet describeTable(String table) throws SQLException {
        String statement = "DESCRIBE `" + table + "`";
        if (debug)
            log("Describing table: " + table);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Describe a column in a table
     *
     * @param table  The table you'd like to describe
     * @param column The column you'd like to describe
     * @return The description of the column
     * @throws SQLException if there is an error communicating with the database
     */
    public ResultSet describeColumn(String table, String column) throws SQLException {
        String statement = "DESCRIBE `" + table + "` `" + column + "`";
        if (debug)
            log("Describing column: " + column + " in table: " + table);
        return new Statement(statement, connection).executeWithResults();
    }

    /**
     * Set a column's default value
     *
     * @param table  The table you'd like to set the default value in
     * @param column The column you'd like to set the default value for
     * @param value  The default value you'd like to set
     * @throws SQLException if there is an error communicating with the database
     */
    public void setColumnDefaultValue(String table, String column, String value) throws SQLException {
        String statement = "ALTER TABLE `" + table + "` ALTER `" + column + "` SET DEFAULT " + value + ";";
        if (debug)
            log("Setting default value: " + value + " for column: " + column + " in table: " + table);
        new Statement(statement, connection).execute();
    }

    /**
     * Write {@code Java Objects} to a table
     *
     * @param table  The table you'd like to write to
     * @param object The object you'd like to insert
     * @throws SQLException if there is an error communicating with the database
     */
    public void insert(String table, Object object) throws SQLException {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        // Adds all fields to the keys and values ArrayLists
        for (Field field : object.getClass().getDeclaredFields()) {
            String key = field.getName();

            // Checks the field's annotations
            if (field.isAnnotationPresent(DontSave.class)) continue;
            if (field.isAnnotationPresent(com.seailz.databaseapi.annotation.Column.class)) {
                // If there is an annotation, use the annotation's name instead of the field's name
                key = field.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value();
                return;
            }

            keys.add(key);
            try {
                field.setAccessible(true);
                values.add(field.get(object).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Adds all fields from the superclass to the keys and values ArrayLists
        for (Field field : object.getClass().getSuperclass().getDeclaredFields()) {
            String key = field.getName();

            // Checks the field's annotations
            if (field.isAnnotationPresent(DontSave.class)) continue;
            if (field.isAnnotationPresent(com.seailz.databaseapi.annotation.Column.class)) {
                // If there is an annotation, use the annotation's name instead of the field's name
                key = field.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value();
                return;
            }

            keys.add(key);
            try {
                field.setAccessible(true);
                values.add(field.get(object).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Loops through and puts everything in a HashMap
        HashMap<String, String> keyValuesHashMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            keyValuesHashMap.put(keys.get(i), values.get(i));
        }

        // Writes the HashMap to the table
        insert(table, keyValuesHashMap);

        if (debug)
            log("Wrote object to table: " + table);
    }

    /**
     * Write multiple {@code Java Objects} to a table
     *
     * @param table  The table you'd like to write to
     * @param objects The objects you'd like to insert
     */
    public void insertList(String table, List<?> objects) {
        objects.forEach(object -> {
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();

            // Adds all fields to the keys and values ArrayLists
            for (Field field : object.getClass().getDeclaredFields()) {
                String key = field.getName();

                // Checks the field's annotations
                if (field.isAnnotationPresent(DontSave.class)) continue;
                if (field.isAnnotationPresent(com.seailz.databaseapi.annotation.Column.class)) {
                    // If there is an annotation, use the annotation's name instead of the field's name
                    key = field.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value();
                    return;
                }

                keys.add(key);
                try {
                    field.setAccessible(true);
                    values.add(field.get(object).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // Adds all fields from the superclass to the keys and values ArrayLists
            for (Field field : object.getClass().getSuperclass().getDeclaredFields()) {
                String key = field.getName();

                // Checks the field's annotations
                if (field.isAnnotationPresent(DontSave.class)) continue;
                if (field.isAnnotationPresent(com.seailz.databaseapi.annotation.Column.class)) {
                    // If there is an annotation, use the annotation's name instead of the field's name
                    key = field.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value();
                    return;
                }

                keys.add(key);
                try {
                    field.setAccessible(true);
                    values.add(field.get(object).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // Loops through and puts everything in a HashMap
            HashMap<String, String> keyValuesHashMap = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                keyValuesHashMap.put(keys.get(i), values.get(i));
            }

            // Writes the HashMap to the table
            try {
                insert(table, keyValuesHashMap);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (debug)
                log("Wrote object to table: " + table);
        });
    }

    /**
     * Reads {@code Java Objects} from a table
     *
     * @param table The table you'd like to read from
     * @param key   The key you'd like to read from
     * @param value The value you'd like to read from
     * @param clazz The class you'd like to read into
     * @return The object you read into
     * @throws SQLException              if there is an error communicating with the database
     * @throws IllegalAccessException    if there is an error accessing the object
     * @throws InstantiationException    if there is an error instantiating the object
     * @throws InvocationTargetException if there is an error invoking the object
     */
    public Object get(String table, String key, String value, Class<?> clazz) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String statement = "SELECT * FROM `" + table + "` WHERE `" + key + "` = '" + value + "';";
        if (debug)
            log("Reading object from table: " + table + " with key: " + key + " and value: " + value);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();

        // Creates a new instance of the class
        Object object;
        Constructor<?> constructor = retrieveConstructor(clazz);
        ArrayList<Object> parameters = new ArrayList<>();

        HashMap<String, Object> keyValuesHashMap = new HashMap<>();

        while (resultSet.next()) {
            // Loops through all the columns and adds them to the HashMap
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                keyValuesHashMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
        }

        for (Parameter p : constructor.getParameters()) {
            if (hasAnnotation(p))
                parameters.add(keyValuesHashMap.get(p.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value()));
        }

        if (debug)
            log("Read object from table: " + table);
        object = constructor.newInstance(parameters.toArray());
        return object;
    }

   /* /**
     * Reads {@code Java Objects} from a table
     *
     * @param table The table you'd like to read from
     * @param key   The key you'd like to read from
     * @param value The value you'd like to read from
     * @param clazz The class you'd like to read into
     * @return The object you read into
     * @throws SQLException              if there is an error communicating with the database
     * @throws IllegalAccessException    if there is an error accessing the object
     * @throws InstantiationException    if there is an error instantiating the object
     * @throws InvocationTargetException if there is an error invoking the object
     */ /*
    public Optional<List<?>> getList(String table, String key, String value, Class<?> clazz) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String statement = "SELECT * FROM `" + table + "` WHERE `" + key + "` = '" + value + "';";
        if (debug)
            log("Reading objects from table: " + table + " with key: " + key + " and value: " + value);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();

        // Creates a new instance of the class
        List<Object> returnObjects = new ArrayList<>();
        Constructor<?> constructor = retrieveConstructor(clazz);
        List<List<Object>> parametersList = new ArrayList<>();

        List<HashMap<String, Object>> keyValuesHashMapList = new ArrayList<>();

        while (resultSet.next()) {
            // Loops through all the columns and adds them to the HashMap
            HashMap<String, Object> keyValuesHashMap = new HashMap<>();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                keyValuesHashMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
            keyValuesHashMapList.add(keyValuesHashMap);
        }

        for (HashMap<String, Object> stringObjectHashMap : keyValuesHashMapList) {
            stringObjectHashMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        }

        for (Parameter p : constructor.getParameters()) {
            if (hasAnnotation(p)) {
                int i = 0;
                for (HashMap<String, Object> keyValuesHashMap : keyValuesHashMapList) {
                    List<Object> parameters = new ArrayList<>();
                    parameters = (List<Object>) parameters.get(keyValuesHashMapList.indexOf(keyValuesHashMap));

                    parametersList.remove(i);

                    parameters.add(keyValuesHashMap.get(p.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value()));
                    parametersList.add(i, parameters);
                    i++;
                }
            }
        }

        System.out.println(parametersList);

        int i = 0;
        for (HashMap<String, Object> stringObjectHashMap : keyValuesHashMapList) {
            returnObjects.add(constructor.newInstance(parametersList.get(i).toArray()));
            i++;
        }

        if (debug)
            log("Read objects from table: " + table);


        if (returnObjects.isEmpty())
            return Optional.empty();
        return Optional.of(returnObjects);
    } */

    public Optional<List<?>> getList(String key, String value, String table, Class<?> clazz) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String statement = "SELECT * FROM `" + table + "` WHERE `" + key + "` = '" + value + "';";
        if (debug)
            log("Reading objects from table: " + table + " with key: " + key + " and value: " + value);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();
        List<Object> returnObjects = new ArrayList<>();

        int i = 0;
        while (resultSet.next()) {
            returnObjects.add(get(table, key, value, clazz, i));
            i++;
        }

        return returnObjects.isEmpty() ? Optional.empty() : Optional.of(returnObjects);
    }

    /**
     * Reads {@code Java Objects} from a table
     *
     * @param table The table you'd like to read from
     * @param key   The key you'd like to read from
     * @param value The value you'd like to read from
     * @param clazz The class you'd like to read into
     * @return The object you read into
     * @throws SQLException              if there is an error communicating with the database
     * @throws IllegalAccessException    if there is an error accessing the object
     * @throws InstantiationException    if there is an error instantiating the object
     * @throws InvocationTargetException if there is an error invoking the object
     */
    private Object get(String table, String key, String value, Class<?> clazz, int index) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String statement = "SELECT * FROM `" + table + "` WHERE `" + key + "` = '" + value + "';";
        if (debug)
            log("Reading object from table: " + table + " with key: " + key + " and value: " + value);
        ResultSet resultSet = new Statement(statement, connection).executeWithResults();

        // Creates a new instance of the class
        Object object;
        Constructor<?> constructor = retrieveConstructor(clazz);
        ArrayList<Object> parameters = new ArrayList<>();

        HashMap<String, Object> keyValuesHashMap = new HashMap<>();

        int resultSetIndex = 0;
        while (resultSet.next()) {
            // Loops through all the columns and adds them to the HashMap
            if (resultSetIndex == index) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    keyValuesHashMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                }
            }
            resultSetIndex++;
        }

        for (Parameter p : constructor.getParameters()) {
            if (hasAnnotation(p))
                parameters.add(keyValuesHashMap.get(p.getAnnotation(com.seailz.databaseapi.annotation.Column.class).value()));
        }

        if (debug)
            log("Read object from table: " + table);
        object = constructor.newInstance(parameters.toArray());
        return object;
    }

    /**
     * Retrieves the correct constructor for a class
     *
     * @param clazz The class you'd like to get the constructor for
     * @return The constructor you retrieved
     */
    private Constructor<?> retrieveConstructor(Class<?> clazz) {
        ArrayList<Constructor<?>> constructors = new ArrayList<>(Arrays.asList(clazz.getConstructors()));
        AtomicReference<Constructor<?>> validConstructor = new AtomicReference<>();
        for (Constructor<?> constructor : constructors) {
            constructor.setAccessible(true);
            Arrays.stream(constructor.getAnnotations()).forEach(annotation -> {
                if (annotation.annotationType().equals(DatabaseConstructor.class)) {
                    validConstructor.set(constructor);
                }
            });
        }
        return validConstructor.get();
    }

    /**
     * Checks if a parameter has the {@link com.seailz.databaseapi.annotation.Column} annotation
     *
     * @param param The parameter you'd like to check
     * @return Whether the parameter has the {@link com.seailz.databaseapi.annotation.Column} annotation
     */
    private boolean hasAnnotation(Parameter param) {
        return param.isAnnotationPresent(com.seailz.databaseapi.annotation.Column.class);
    }

    /**
     * Logs a message to the console
     *
     * @param text The message you'd like to log
     */
    private void log(@NotNull String text) {
        System.out.println("[Database] " + text);
    }
}

