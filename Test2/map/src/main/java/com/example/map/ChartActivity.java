package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ChartActivity extends AppCompatActivity {
    private LineChart lineChart;
    private String chart_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Intent intentchart = getIntent();
        chart_url = intentchart.getStringExtra("chart_url");

        Intent intent_chart = getIntent();
        if(chart_url == null){
        chart_url = intent_chart.getStringExtra("charturl");}


        lineChart = findViewById(R.id.lineChart);


        fetchDataFromServer();

    }
    private void fetchDataFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(chart_url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                        StringBuilder resultBuilder = new StringBuilder();
                        String line;
                        while ((line = bufReader.readLine()) != null) {
                            resultBuilder.append(line);
                        }
                        inputStream.close();
                        final String result = resultBuilder.toString();

                        Log.d("ServerResponse", "Received data: " + result);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                List<priceEntry> priceEntries = parseResult(result);
                                updateChart(priceEntries);
                            }
                        });
                    }
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void updateChart(List<priceEntry> priceEntries) {
        if (priceEntries == null || priceEntries.isEmpty()) {
            Log.e("Chart", "priceEntries is empty or null, cannot update chart");
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        final ArrayList<String> season = new ArrayList<>();

        int index = 0;
        for (priceEntry priceEntry : priceEntries) {
            entries.add(new Entry(index, priceEntry.price));
            season.add(priceEntry.season);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "價格資料");
        dataSet.setDrawValues(false);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(5, false);
        xAxis.setValueFormatter(new CustomXAxisValueFormatter(season));


        lineChart.invalidate();
    }

    private List<priceEntry> parseResult(String result) {
        List<priceEntry> priceEntries = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                float price = (float) jsonObject.getDouble("price");
                String season = jsonObject.getString("season");
                priceEntries.add(new priceEntry(price, season));
            }
            Log.d("ParsedDataSize", "Parsed " + priceEntries.size() + " entries.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return priceEntries;
    }
}

class priceEntry {
    String season;
    float price;

    public priceEntry(float price, String season) {
        this.season = season;
        this.price = price;
    }
}

class CustomXAxisValueFormatter extends IndexAxisValueFormatter {
    private final List<String> values;

    public CustomXAxisValueFormatter(List<String> values) {
        super(values);
        this.values = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        if (index % 5 == 0 && index < values.size()) {
            return values.get(index);
        } else {
            return "";
        }
    }
}
