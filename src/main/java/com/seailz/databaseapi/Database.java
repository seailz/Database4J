package com.seailz.databaseapi;

import com.seailz.databaseapi.core.table.Column;
import com.seailz.databaseapi.core.table.ColumnType;
import com.seailz.databaseapi.core.table.Table;
import com.seailz.databaseapi.core.Statement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A way to interact with databases easier
 * @author Seailz - <a href="https://www.seailz.com">Website</a>
 */
@Getter
@Setter
public class Database {

    private boolean debug;

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
     * Initiate the connection to the database
     * @author Seailz
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + getIp() + ":" + getPort() + "/" + getDatabaseName(),
                getUsername(),
                getPassword()
        );
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * Creates a table within the Database
     * @param table The table you would like to create
     * @author Seailz
     */
    public void createTable(@NotNull Table table) throws SQLException {
        debug = true;
        StringBuilder statement = new StringBuilder("CREATE TABLE `" + table.getName() + "` (\n");

        Column last = table.getColumns().get(table.getColumns().size() - 1);
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
            statement.append(",\n\tPRIMARY KEY (").append(table.getPrimaryKey()).append(")");

        statement.append("\n);");

        if (debug)
            System.out.println(statement);

        new Statement(statement.toString(), connection).execute();
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
        return null;
    }

    /**
     * Check if a table exists
     * @param tableName The table you'd like to check
     * @return A boolean if the table exists or not
     * @throws SQLException If there is an error
     */
    public boolean tableExists(@NotNull String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        return resultSet.next();
    }

    /**
     * Insert into a database
     * @param table The table you'd like to insert to
     * @param values A hashmap of keys, and values
     * @throws SQLException if there is an error
     */
    public void insert(@NotNull String table, @NotNull HashMap<String, String> values) throws SQLException {
        debug = true;
        StringBuilder statement = new StringBuilder("insert into `" + table + "` (");

        ArrayList<String> keysArray = new ArrayList<>(values.keySet());
        String lastKey = keysArray.get(keysArray.size() - 1);
        for (String key : values.keySet()) {
            if (!key.equals(lastKey))
                statement.append("`").append(key).append("`, ");
            else
                statement.append("`").append(key).append("`)");
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

        prepStatement.execute();
    }

    /**
     * Delete a row rom the database
     * @param table The table you'd like to edit
     * @param key The key, basically the identifier
     * @param value The value, such as the player's name
     */
    public void delete(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "DELETE FROM '" + table + "' WHERE '" + key + "'='" + value + "'";
        new Statement(statement, connection).execute();
    }

    /**
     * Check if a row exists
     * @param table The table you'd like to check
     * @param key The key
     * @param value The value
     * @return whether that row exists
     */
    public boolean rowExists(@NotNull String table, @NotNull String key, @NotNull String value) throws SQLException {
        String statement = "SELECT * FROM '" + table + "' WHERE '" + key + "'='" + value + "'";
        return new Statement(statement, connection).executeWithResults().next();
    }

    /**
     * Replace a current row with a new one
     * @param table The table in which the row is located
     * @param key The key you would like to check
     * @param value the value of that key
     * @param values the values of the new row you'd like to insert
     * @throws SQLException If there's an error communicating with the database
     */
    public void replace(@NotNull String table, @NotNull String key, @NotNull String value, @NotNull HashMap<String, String> values) throws SQLException {
        if (!rowExists(table, key, value)) return; // Trying to prevent as many errors as possible :/

        delete(table, key, value);
        insert(table, values);
    }
}

