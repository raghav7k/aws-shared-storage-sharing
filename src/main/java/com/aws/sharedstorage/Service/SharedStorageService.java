package com.aws.sharedstorage.Service;

import com.aws.sharedstorage.Repository.AWSRepository;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.amazonaws.auth.BasicSessionCredentials;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@Service
public class SharedStorageService {

    AWSRepository awsRepository;
    SQLFunctions sqlFunctions;

    String RoleARN = "arn:aws:iam::844102931058:role/test_AssumeRole_VCM";
    private final String url = "jdbc:mysql://localhost:3306/jdbc-share";
    private final String user = "root";
    private final String pass = "Raghav@123#";

    @Autowired
    public SharedStorageService(AWSRepository awsRepository, SQLFunctions sqlFunctions) {
        this.awsRepository = awsRepository;
        this.sqlFunctions = sqlFunctions;
    }


    public ArrayList<String> getBuckets(String roleArn) {

        BasicSessionCredentials awsCredentials = awsRepository.assumeRole(roleArn);
        return awsRepository.getBucketsWithAssume(awsCredentials);

    }

    public JSONArray getShares(String shareId) {
        JSONArray shares = sqlFunctions.getShares(shareId);
        return shares;
    }

    public Object createShare(String body) {
        JSONObject output = new JSONObject();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(body);
            JSONArray datasets = (JSONArray) jsonObject.get("datasets");
            ArrayList<String> buckets = this.getBuckets(RoleARN);
            for ( Object o: datasets) {
                JSONObject dataset = (JSONObject) o;
                if (! buckets.contains(dataset.get("location").toString())) {
                    output.put("Error", "Failed to create the Share as dataset " + dataset.get("location").toString() + " doesn't exist");
                    return output;
                }

                int size = awsRepository.getBucketsObjects(dataset.get("location").toString());
                if (size < 1) {
                    dataset.put("warning", "Dataset is empty");
                    datasets.remove(o);
                    datasets.add(dataset);
                }

            }
            jsonObject.replace("datasets", datasets);
            sqlFunctions.createShare(jsonObject);
            output.put("Message", "Share has been created");
            output.put("Share", jsonObject);
            return output;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray checkDatasetValidity(Object data) {
        JSONArray datasets = new JSONArray();
        if ( data.getClass().getSimpleName().compareToIgnoreCase("JSONArray") == 0) {
            datasets = (JSONArray) data;
        } else {
            JSONObject jsonObject = (JSONObject) data;
            if ( jsonObject.containsKey("location")) {
                datasets.add(jsonObject);
            } else {
                return new JSONArray();
            }
        }
        ArrayList<String> buckets = this.getBuckets(RoleARN);
        for ( Object o: datasets) {
            JSONObject dataset = (JSONObject) o;
            if (! buckets.contains(dataset.get("location").toString())) {
                return new JSONArray();
            }

            int size = awsRepository.getBucketsObjects(dataset.get("location").toString());
            if ( size == -1) {
                dataset.put("warning", "Not able to find the size of the dataset");
                datasets.remove(o);
                datasets.add(dataset);
            }
            else if (size < 1) {
                dataset.put("warning", "Dataset is empty");
                datasets.remove(o);
                datasets.add(dataset);
            }

        }
        return datasets;
    }

    public Object updateShare(String shareId, String requestBody) {
        String sql = "";
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject body =  (JSONObject) jsonParser.parse(requestBody);
            sql = "Update shares SET ";
            for (String s: body.keySet()) {
                sql += s + "='" + body.get(s).toString() + "',";
            }
            sql = sql.substring(0, sql.length()-1) +  " where id = '"+ shareId + "'";
            System.out.println(sql);
            Statement statement = sqlFunctions.getStatement(this.url, this.user, this.pass);
            statement.executeUpdate(sql);
            return this.getShares(shareId);
        } catch ( ParseException | SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    public JSONObject deleteShare(String shareId) {
        return sqlFunctions.deleteShare(shareId);
    }

    public JSONObject addDatasets(String shareId, String body) {
        JSONObject output = new JSONObject();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject dataset = (JSONObject) jsonParser.parse(body);
            JSONArray datasets = new JSONArray();
            datasets.add(dataset);
            JSONArray bool;
            bool = datasets;
            bool = checkDatasetValidity(bool);
            if ( bool.isEmpty()) {
                output.put("error", "Not able to add the datasets to share due to NOT FOUND dataset in shared storage");
                return output;
            }
            sqlFunctions.insertDatasets(shareId, datasets);
            output.put("Message", "Dataset has been added to the share");
            dataset = (JSONObject) bool.get(0);
            if (dataset.containsKey("warning")) {
                output.put("Warning", dataset.get("warning"));
                dataset.remove("warning");
            }
            output.put("Dataset", dataset);
            return output;

        } catch ( ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONArray getDatasets(String shareId) {
        try {
            String sqlText = "Select * from datasets where share_id = '" + shareId + "' ";
            return sqlFunctions.getData(sqlText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getDataset(String datasetId) {
        try {
            String sqlText = "Select * from datasets where id = '" + datasetId + "' ";
            return (JSONObject) sqlFunctions.getData(sqlText).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject updateDataset(String shareId, String datasetId, String body) {
        JSONObject output = new JSONObject();
        try {
            if (body.isEmpty()) {
                output.put("Error", "Empty Body");
                return output;
            }
            JSONObject dataset = (JSONObject) new JSONParser().parse(body);

            JSONObject b = new JSONObject(dataset);
            JSONArray bool = checkDatasetValidity(b);
            if ( bool.isEmpty()) {
                output.put("Error", "Not able to add the datasets to share due to NOT FOUND dataset in shared storage");
                return output;
            } else {

                JSONObject x = (JSONObject) bool.get(0);
                if (x.containsKey("warning") ) {
                    output.put("Warning", x.get("warning"));
                }

            }
            String sql = "Update datasets SET ";
            for (String s: dataset.keySet()) {
                sql += s + "='" + dataset.get(s).toString() + "',";
            }
            sql = sql.substring(0, sql.length()-1) +  " where share_id = '"+ shareId + "' and id = '"+ datasetId + "'";
            Statement statement = sqlFunctions.getStatement(this.url, this.user, this.pass);
            statement.executeUpdate(sql);
            output.put("Message", "Dataset has been updated");
            output.put("Dataset", this.getDataset(datasetId));
            return output;
        } catch (ParseException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject deleteDataset(String shareId, String datasetId) {
        return sqlFunctions.deleteDataset(shareId, datasetId);
    }
}
