package com.aws.sharedstorage.Service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.UUID;

public class SQLFunctions {

    public Statement getStatement(String url, String username, String password) {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            return statement;
        } catch ( SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*public String insertDatasets(String shareId, JSONArray datasets) {

        String sqlText;
        try {
            for (Object o : datasets) {
                JSONObject dataset = (JSONObject) o;
                sqlText = "INSERT INTO datasets (share_id, name, email) Values (" +
                        "'" + shareId + "'," +
                        "'" + s.get("name").toString() + "'," +
                        "'" + s.get("email").toString() + "')";

                Statement statement = myJDBC.getStatement("jdbc:mysql://localhost:3306/jdbc-share", "root", "Raghav@123#");
                statement.executeUpdate(sqlText);
            }
        } catch ( )
    }*/
    public String createShare(JSONObject body) {

        UUID shareId = UUID.randomUUID();
        Statement statement = this.getStatement("jdbc:mysql://localhost:3306/jdbc-share", "root", "Raghav@123#");
        String sqlText = "INSERT INTO shares (id, name, direction, description, model) Values (" +
                "'" +shareId+ "'," +
                "'" +body.get("name").toString() + "'," +
                "'" +body.get("direction").toString() + "'," +
                "'" +body.get("description").toString() + "'," +
                "'" +body.get("model").toString() + "')";
        try {
            statement.executeUpdate(sqlText);

            // insert datasets
            // this.insertDatasets(shareId.toString(), (JSONArray) body.get("datasets"))

        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }
}
