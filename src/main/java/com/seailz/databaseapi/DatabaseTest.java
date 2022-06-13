package com.seailz.databaseapi;

import com.seailz.databaseapi.core.table.Column;
import com.seailz.databaseapi.core.table.ColumnType;
import com.seailz.databaseapi.core.table.Table;

import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ArrayList<Column> columns = new ArrayList<>();

        Database db = new Database(
                "node.seailz.com", 3306,
                "u20_BTUFkBrXYk", "dl.@xr3Oa+4v^ZXLktBD3ejL",
                "s20_license"
        );

        db.connect();

        columns.add(
                new Column(
                        ColumnType.VARCHAR, "COLUMN"
                )
        );

        Table t = new Table("TEST_TABLE", columns);
        db.createTable(t);
    }

}
