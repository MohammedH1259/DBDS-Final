package com.example.myapp;

//import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    Button btn_sendQuery;
    Button btn_displayQuery;
    Button btn_sendQueryRDS;
    EditText et_queryInput;
    ListView lv_resultsDisplay;
    ArrayList<String> display= new ArrayList<String>();
    String[] queryResultsRDS = new String[0];
    String getQueryResultsRed = "";

    final String urlRDS = "https://2sembc73t2.execute-api.us-east-1.amazonaws.com/RDSQuery";
    final String urlRedshift = "https://5yjoylm9ig.execute-api.us-east-1.amazonaws.com/getProducts";
    JSONObject queryResults = new JSONObject();
    Context context;
    final int MY_SOCKET_TIMEOUT_MS = 30000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestQueue queue = Volley.newRequestQueue(this);


        //Display List

        //UI Components

        btn_sendQuery = findViewById(R.id.querySender);
        btn_displayQuery = findViewById(R.id.displayResultsBtn);
        et_queryInput = findViewById((R.id.queryInput));
        lv_resultsDisplay = findViewById(R.id.queryDisplay);
        btn_sendQueryRDS = findViewById(R.id.querySenderRDS);
        ArrayAdapter displayResults = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, display);

        //On click listeners

        btn_sendQueryRDS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display.clear();
                long time1= System.currentTimeMillis();
                String sql = et_queryInput.getText().toString();
                String url = urlRDS + queryProcessing.queryRDSProcess(sql);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //queryResults = response;
                                queryResultsRDS = queryProcessing.responsePostProcessRDS(response);
                                Log.d("Query Loaded", queryResultsRDS.toString());
                                StringBuilder tempReplace = new StringBuilder();

                                for(int i = 0; i<queryResultsRDS.length; i++){
                                    tempReplace.append(queryResultsRDS[i].toString()+"]");
                                    display.add(tempReplace.toString());
                                    tempReplace.setLength(0);
                                }
                                long time2= System.currentTimeMillis();
                                Toast.makeText(MainActivity.this, "Query loaded in "+Float.toString(time2-time1)+" ms", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Query Failed", Toast.LENGTH_SHORT).show();

                    }
                }
                );

                request.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS*2,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);



            }
        });




        btn_displayQuery.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                lv_resultsDisplay.setAdapter(displayResults);

            }
        });

        btn_sendQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display.clear();
                long time1= System.currentTimeMillis();
                String sql = et_queryInput.getText().toString();
                String queryUrl = urlRedshift + queryProcessing.dictToQuery(queryProcessing.queryDict(sql)).toString();

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, queryUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                queryResults = response;

                                try {
                                    JSONArray tempHolder = queryProcessing.responsePostProcess(queryResults);
                                    for(int i = 0; i <tempHolder.length(); i++){
                                        display.add(tempHolder.getJSONObject(i).toString());
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                long time2= System.currentTimeMillis();
                                Toast.makeText(MainActivity.this, "Query loaded in "+Float.toString(time2-time1)+" ms", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Query Failed", Toast.LENGTH_SHORT).show();

                    }
                }
                );


                request.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS*2,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);

    }
});
    };
}
