package com.example.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MarkerDetailActivity extends AppCompatActivity {

    private String nowprice, futureprice;
    private MyAdapter adapter;
    private URL url;
    private String title,nowprice_url,futureprice_url;
    private String chart_url,RecyclerView_url;
    private List<MyDataModel> dataList = new ArrayList<>();
    private Button chartbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_detail);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        nowprice_url = intent.getStringExtra("nowprice_url");
        futureprice_url = intent.getStringExtra("futureprice_url");
        chart_url = intent.getStringExtra("chart_url");
        RecyclerView_url = intent.getStringExtra("RecyclerView_url");

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        chartbutton = findViewById(R.id.chartbutton);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(dataList);
        recyclerView.setAdapter(adapter);


        chartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentchart = new Intent(MarkerDetailActivity.this, ChartActivity.class);
                intentchart.putExtra("chart_url",chart_url);
                startActivity(intentchart);
            }
        });

        fetchDataFromServer1();
        fetchDataFromServer2();
        fetchDataFromServer3();

        titleTextView.setText(title);


    }

    private void fetchDataFromServer1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL requestUrl = new URL(nowprice_url);
                    HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                        StringBuilder box = new StringBuilder();
                        String line;
                        while ((line = bufReader.readLine()) != null) {
                            box.append(line).append("\n");
                        }
                        inputStream.close();
                        nowprice = box.toString();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLocationTextView();
                            }
                        });
                    }

                } catch (Exception e) {
                    nowprice = e.toString();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLocationTextView();
                        }
                    });
                }
            }
        }).start();
    }
    private void fetchDataFromServer2() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                URL requestUrl = new URL(futureprice_url);  // 假設您還要在 URL 中傳遞參數
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    String box = "";
                    String line;
                    while ((line = bufReader.readLine()) != null) {
                        box += line + "\n";
                    }
                    inputStream.close();
                    futureprice = box;
                }

            } catch (Exception e) {
                futureprice = e.toString();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateLocationTextView();
                }
            });
        }
    }).start();
}
    private void updateLocationTextView() {
        TextView locationTextView = findViewById(R.id.locationTextView);
        locationTextView.setText("當前平均每坪房價: " + nowprice + " ===>" +"\n"+ " 未來平均每坪房價: " + futureprice);
    }

    private void fetchDataFromServer3() {
        new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(RecyclerView_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    StringBuilder resultBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        resultBuilder.append(line);
                    }

                    inputStream.close();
                    final String result = resultBuilder.toString();


                    List<MyDataModel> fetchedData = parseResult(result);


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            dataList.clear();
                            dataList.addAll(fetchedData);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();
}
    private List<MyDataModel> parseResult(String result) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<MyDataModel>>() {}.getType();
        List<MyDataModel> dataList = gson.fromJson(result, listType);

        for(MyDataModel data : dataList)
        {
            String year = data.getYear();
            String month = data.getMonth();
            String date = year + "-"+ (month.length() == 1 ? "0" + month : month);
            data.setDate(date);
        }
        return dataList;
    }
}
