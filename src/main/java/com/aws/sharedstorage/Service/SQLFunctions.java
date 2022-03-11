package com.aws.sharedstorage.Service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.UUID;

@Repository
public class SQLFunctions {

    private final String url = "jdbc:mysql://localhost:3306/jdbc-share";
    private final String user = "root";
    private final String pass = "Raghav@123#";

    @Autowired
    public SQLFunctions () {}

    public Statement getStatement(String url, String user, String password) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            System.out.println(statement);
            return statement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getShares(String shareId) {
        String sqlText = "Select * from shares where model = 'shared-storage'";
        if ( shareId != null) {
            sqlText = sqlText + " and id = '" +shareId+ "'";
        }
        try {
            JSONArray result = this.getData(sqlText);
            return result;
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertDatasets(String shareId, JSONArray datasets) {

        String sqlText;
        try {
            for (Object o : datasets) {
                UUID datasetId = UUID.randomUUID();
                JSONObject dataset = (JSONObject) o;
                sqlText = "INSERT INTO datasets (id, share_id, dataset_name, description, location) Values (" +
                        "'" + datasetId + "'," +
                        "'" + shareId + "'," +
                        "'" + dataset.get("name").toString() + "'," +
                        "'" + dataset.get("description").toString() + "'," +
                        "'" + dataset.get("location").toString() + "')";

                Statement statement = this.getStatement(this.url, this.user, this.pass);
                statement.executeUpdate(sqlText);
            }
        } catch (SQLException e ) {
            e.printStackTrace();
        }

    }
    public String createShare(JSONObject body) {

        UUID shareId = UUID.randomUUID();

        String sqlText = "INSERT INTO shares (id, name, direction, description, model) Values (" +
                "'" +shareId+ "'," +
                "'" +body.get("name").toString() + "'," +
                "'" +body.get("direction").toString() + "'," +
                "'" +body.get("description").toString() + "'," +
                "'" +body.get("model").toString() + "')";
        try {

            Statement statement = this.getStatement(this.url, this.user, this.pass);
            statement.executeUpdate(sqlText);

            // insert datasets
            this.insertDatasets(shareId.toString(), (JSONArray) body.get("datasets"));

        } catch ( SQLException e) {
            e.printStackTrace();
        }

        return "share created";
    }

    public JSONArray getData(String sql) {
        try {
            Statement statement = this.getStatement(this.url, this.user, this.pass);
            ResultSet resultSet = statement.executeQuery(sql);

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnsNumber = resultSetMetaData.getColumnCount();

            JSONArray output = new JSONArray();
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= columnsNumber; i++) {
                    if (resultSet.getString(i) != null)
                        jsonObject.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
                }
                output.add(jsonObject);
            }
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONObject deleteDataset(String shareId, String datasetId) {
        String sql = "Delete from datasets where share_id = '" +shareId+ "' and id = '" +datasetId+ "'";
        Statement statement = this.getStatement(this.url, this.user, this.pass);
        JSONObject output = new JSONObject();
        try {
            statement.executeUpdate(sql);
            output.put("Message", "Dataset has been deleted");
        } catch ( SQLException e) {
            e.printStackTrace();
            output.put("Error", "Failed to delete the dataset");
        }
        return output;
    }

    public JSONObject deleteShare( String shareId) {
        String sql = "Delete from shares where id = '" +shareId+ "'";
        Statement statement = this.getStatement(this.url, this.user, this.pass);
        JSONObject output = new JSONObject();
        try {
            statement.executeUpdate(sql);
            output.put("Message", "Share has been deleted");
        } catch ( SQLException e) {
            e.printStackTrace();
            output.put("Error", "Failed to delete the Share");
        }
        return output;
    }
}
