package com.seailz.databaseapi;

import com.seailz.databaseapi.core.table.Column;
import com.seailz.databaseapi.core.table.ColumnType;
import com.seailz.databaseapi.core.table.Table;
import com.seailz.databaseapi.core.Statement;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A way to interact with databases easier
 * @author Seailz - <a href="https://www.seailz.com">Website</a>
 */
public class Database {

    private boolean debug;

    @Getter
    @Setter
    private String ip;

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String databaseName;

    private Connection connection;

    private String url = null;

    /**
     * Create a database instance
     * @param ip The ip which you would like to connect to
     * @param port The port on which the database is hosted
     * @param username The username you'd like to use
     * @param password The password you'd like to use.
     * @param databaseName The name of the database
     * @author Seailz
     */
    public Database(String ip, int port, String username, String password, String databaseName) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        setIp(ip);
        setPort(port);
        setUsername(username);
        setPassword(password);
        setDatabaseName(databaseName);
    }

    /**
     * Initiate a {@code Database} using a {@code URL}
     * @param url A {@link String} which is the value of your {@code JDBC} connection string
     * @author Seailz
     */
    public Database(String url) throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        this.url = url.toString();
    }

    /**
     * Initiate the connection to the database
     * @author Seailz
     */
    public void connect() throws SQLException {
        connection = url == null ? DriverManager.getConnection(
                "jdbc:mysql://" + getIp() + ":" + getPort() + "/" + getDatabaseName(),
                getUsername(),
                getPassword()
        ) : DriverManager.getConnection(url);
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * Creates a table within the Database
     * @param table The table you would like to create
     * @author Seailz
     */
    public void createTable(Table table) throws SQLException {
        debug = false;
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

            if (column.getType() == ColumnType.VARCHAR)
                statement.append("(255)");


            if (!last.equals(column))
                statement.append(",");

        }

        if (table.getPrimaryKey() != null)
            statement.append(
                    ",\n\tPRIMARY KEY (`" + table.getPrimaryKey() + "`)"
            );

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
    public Object get(String table, String key, String value, String column) throws SQLException {
        String statement = "SELECT * FROM " + table;
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
    public boolean tableExists(String tableName) throws SQLException {
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
    public void insert(String table, HashMap<String, String> values) throws SQLException {
        StringBuilder statement = new StringBuilder("insert into " + table + " (");

        ArrayList<String> keysArray = new ArrayList<>(values.keySet());
        String lastKey = keysArray.get(keysArray.size() - 1);
        for (String key : values.keySet()) {
            if (!key.equals(lastKey))
                statement.append(key).append(", ");
            else
                statement.append(key).append(")");
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
    public void delete(String table, String key, String value) throws SQLException {
        String statement = "DELETE FROM " + table + " WHERE " + key + "='" + value + "'";
        new Statement(statement, connection).execute();
    }

    /**
     * Check if a row exists
     * @param table The table you'd like to check
     * @param key The key
     * @param value The value
     * @return whether that row exists
     */
    public boolean rowExists(String table, String key, String value) throws SQLException {
        String statement = "SELECT * FROM " + table + " WHERE " + key + "='" + value + "'";
        return new Statement(statement, connection).executeWithResults().next();
    }
}
