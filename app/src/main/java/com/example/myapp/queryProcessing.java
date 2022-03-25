package com.example.myapp;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class queryProcessing {


    final String DbUser = "awsuser";
    final String StatementName = "QueryStatement";


    static Hashtable<String, String> queryDict(String sqlStatement){
        Hashtable<String,String> queryDict = new Hashtable<String,String>();
        queryDict.put("ClusterIdentifier", "redshift-cluster-1");
        queryDict.put("Database", "instacart");
        queryDict.put("DbUser", "awsuser");


        sqlStatement = sqlStatement.replace(" ", "%20");
        sqlStatement = sqlStatement.replace("\"", "%22");
        sqlStatement = sqlStatement.replace("*","%2A%0A");
        sqlStatement = sqlStatement.replace("(","%28");
        sqlStatement = sqlStatement.replace(")","%29");
        sqlStatement = sqlStatement.replace("=","%3D");
        sqlStatement = sqlStatement.replace("_", "%5F");
        sqlStatement = sqlStatement.replace(",", "%2C");

        queryDict.put("Sql",sqlStatement);
        queryDict.put("StatementName","queryFromAPP");

        return queryDict;
    }

    static StringBuilder queryRDSProcess(String sqlStatement){
        StringBuilder queryURL = new StringBuilder();
        queryURL.append("?");
        sqlStatement = sqlStatement.replace(" ", "%20");
        sqlStatement = sqlStatement.replace("\"", "");
        sqlStatement = sqlStatement.replace("*","%2A%0A");
        sqlStatement = sqlStatement.replace("(","%28");
        sqlStatement = sqlStatement.replace(")","%29");
        sqlStatement = sqlStatement.replace("=","%3D");
        sqlStatement = sqlStatement.replace("_", "%5F");
        sqlStatement = sqlStatement.replace(",", "%2C");
        queryURL.append("Sql"+"="+sqlStatement);


        return queryURL;
    }

    static StringBuilder dictToQuery(Hashtable queryDict){
        StringBuilder queryUrl = new StringBuilder();
        queryUrl.append("?");
        queryUrl.append("ClusterIdentifier"+"="+queryDict.get("ClusterIdentifier"));
        queryUrl.append("&");
        queryUrl.append("Database"+"="+queryDict.get("Database"));
        queryUrl.append("&");
        queryUrl.append("DbUser"+"="+queryDict.get("DbUser"));
        queryUrl.append("&");
        queryUrl.append("Sql"+"="+queryDict.get("Sql"));
        queryUrl.append("&");
        queryUrl.append("StatementName"+"="+queryDict.get("StatementName"));

        return queryUrl;

    }

    static String[] responsePostProcessRDS(JSONObject response) {

        String[] ResponseResults = new String[0];
        try {
            ResponseResults = response.getString("Results").toString().split("],");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ResponseResults;
    }

    static JSONArray responsePostProcess(JSONObject response) throws JSONException {
        JSONArray columnMetadata = new JSONArray();
        JSONObject queryResult = new JSONObject();
        ArrayList<String> columnNames = new ArrayList<String>();
        //Pulls out Number of Records Pulled
        int recordLength = response.getJSONObject("Results").getJSONArray("Records").length();
        JSONArray queryResultsProcessed = new JSONArray();


        //Pulls Out Column Names
        try {
            columnMetadata = response.getJSONObject("Results").getJSONArray("ColumnMetadata");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < columnMetadata.length(); i++){
            columnNames.add(columnMetadata.getJSONObject(i).get("name").toString());
        }
        int fields = columnNames.size();



        for (int j = 0; j <recordLength; j++){
            JSONObject jsonObj = new JSONObject();
            for(int k = 0; k < fields; k++){
                ArrayList<String> keyVal = new ArrayList<String>();
                Iterator keys = response.getJSONObject("Results").getJSONArray("Records").getJSONArray(j).getJSONObject(k).keys();
                while(keys.hasNext()){
                    keyVal.add(keys.next().toString());
                }
                jsonObj.put(columnNames.get(k),response.getJSONObject("Results").getJSONArray("Records").getJSONArray(j).getJSONObject(k).get(keyVal.get(0)));

            }
            queryResultsProcessed.put(jsonObj);
        }


        return queryResultsProcessed;
    }

}
