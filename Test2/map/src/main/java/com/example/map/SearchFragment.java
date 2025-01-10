package com.example.map;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {
    private String area, year, season,city;
    private String nowprice, futureprice;
    private MyAdapter adapter;
    private String[] spinner1_option;
    private List<MyDataModel> dataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_search, container, false);


        Spinner spinner1 = view.findViewById(R.id.spinner1);
        Spinner spinner2 = view.findViewById(R.id.spinner2);
        Spinner spinner3 = view.findViewById(R.id.spinner3);
        Spinner spinner4 = view.findViewById(R.id.spinner4);
        Button Search_button = view.findViewById(R.id.Search_button);
        Button chart_button = view.findViewById(R.id.button);
        TextView price_or_null = view.findViewById(R.id.price_or_null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyAdapter(dataList);
        recyclerView.setAdapter(adapter);


        List<String> spinner1OptionList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.null_option)));

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinner1OptionList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);


        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(getActivity(),
                R.array.city_option, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner4.setAdapter(adapter4);


        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.year_option, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(),
                R.array.season_option, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);


        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        spinner1_option = getResources().getStringArray(R.array.null_option);
                        break;
                    case 1:
                        spinner1_option = getResources().getStringArray(R.array.Taipei_option);
                        break;
                    case 2:
                        spinner1_option = getResources().getStringArray(R.array.NewTaipei_option);
                        break;
                    case 3:
                        spinner1_option = getResources().getStringArray(R.array.Keelung_option);
                        break;
                }
                city = getcity(position - 1);

                spinner1OptionList.clear();


                spinner1OptionList.addAll(Arrays.asList(spinner1_option));  // 使用 Arrays.asList 將數組轉為 List
                adapter1.notifyDataSetChanged();  // 通知適配器數據已更新
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position != 0) {
                            area = getArea(position - 1);
                        } else return;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = getYear(position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                season = getSeason(position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 設置按鈕點擊事件
        Search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String now_price = "http://3.27.231.134/getprice.php" +"?city="+ city +"&area=" + area + "&year=" + "113" + "&season=" + "Q2";
                Log.d("url","now_price"+now_price);
                String future_price = "http://3.27.231.134/getprice.php" + "?city="+ city +"&area=" + area +"&year="+year+ "&season=" + season;
                String RecyclerView_url = "http://3.27.231.134/RecyclerView.php" +"?city="+city+ "&area=" + area;

                if (area == null || year == null || season == null) {
                    price_or_null.setText("選項有空，無法搜尋");
                    dataList.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    fetchDataFromServer1(now_price);
                    fetchDataFromServer2(future_price);
                    fetchDataFromServer3(RecyclerView_url);
                }
            }
        });

        chart_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(city!=null && area != null){
                Intent chart_intent = new Intent(requireActivity(),ChartActivity.class);
                chart_intent.putExtra("charturl","http://3.27.231.134/chart.php"+"?city="+city+"&area="+area);
                startActivity(chart_intent);}
                else {
                    price_or_null.setText("選項有空，無法搜尋");
                }
            }
        });

        return view;
    }
    private String getcity(int position)
    {
        switch (position){
            case 0:
                return "臺北市";
            case 1:
                return "新北市";
            case 2:
                return "基隆市";
            default:
                return null;
        }
    }
    private String getArea(int position) {
        if(city.equals("臺北市")) {
            switch (position)
                {
                    case 0:
                        return "中山區";
                    case 1:
                        return "中正區";
                    case 2:
                        return "信義區";
                    case 3:
                        return "內湖區";
                    case 4:
                        return "北投區";
                    case 5:
                        return "南港區";
                    case 6:
                        return "士林區";
                    case 7:
                        return "大同區";
                    case 8:
                        return "大安區";
                    case 9:
                        return "文山區";
                    case 10:
                        return "松山區";
                    case 11:
                        return "萬華區";
                    default:
                        return null;
                }
            }else if(city.equals("新北市")){
                switch (position)
                {
                    case 0:
                        return "板橋區";
                    case 1:
                        return "三重區";
                    case 2:
                        return "中和區";
                    case 3:
                        return "永和區";
                    case 4:
                        return "新莊區";
                    case 5:
                        return "新店區";
                    case 6:
                        return "樹林區";
                    case 7:
                        return "鶯歌區";
                    case 8:
                        return "三峽區";
                    case 9:
                        return "淡水區";
                    case 10:
                        return "汐止區";
                    case 11:
                        return "瑞芳區";
                    case 12:
                        return "土城區";
                    case 13:
                        return "蘆洲區";
                    case 14:
                        return "五股區";
                    case 15:
                        return "泰山區";
                    case 16:
                        return "林口區";
                    case 17:
                        return "深坑區";
                    case 18:
                        return "石碇區";
                    case 19:
                        return "坪林區";
                    case 20:
                        return "三芝區";
                    case 21:
                        return "石門區";
                    case 22:
                        return "八里區";
                    case 23:
                        return "平溪區";
                    case 24:
                        return "雙溪區";
                    case 25:
                        return "貢寮區";
                    case 26:
                        return "金山區";
                    case 27:
                        return "萬里區";
                    case 28:
                        return "烏來區";

                    default:
                        return null;
                }
            }else {
                switch (position)
                {
                    case 0:
                        return "仁愛區";
                    case 1:
                        return "信義區";
                    case 2:
                        return "中正區";
                    case 3:
                        return "中山區";
                    case 4:
                        return "安樂區";
                    case 5:
                        return "暖暖區";
                    case 6:
                        return "七堵區";

                    default:
                        return null;
            }
        }
    }

    private String getYear(int position) {
        switch (position) {
            case 0:
                return "113";
            case 1:
                return "114";
            case 2:
                return "115";
            case 3:
                return "116";
            case 4:
                return "117";
            case 5:
                return "118";
            case 6:
                return "119";

            default:
                return null;
        }
    }

    private String getSeason(int position) {
        switch (position) {
            case 0:
                return "Q1";
            case 1:
                return "Q2";
            case 2:
                return "Q3";
            case 3:
                return "Q4";
            default:
                return null;
        }
    }

    private void fetchDataFromServer1(String nowprice_url) {
        new Thread(() -> {
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
                }

            } catch (Exception e) {
                nowprice = e.toString();
            }
            requireActivity().runOnUiThread(this::updateLocationTextView);
        }).start();
    }

    private void fetchDataFromServer2(String futureprice_url) {
        new Thread(() -> {
            try {
                URL requestUrl = new URL(futureprice_url);
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
                    futureprice = box.toString();
                }

            } catch (Exception e) {
                futureprice = e.toString();
            }
            requireActivity().runOnUiThread(this::updateLocationTextView);
        }).start();
    }

    private void updateLocationTextView() {
        TextView price_or_null = getView().findViewById(R.id.price_or_null);
        price_or_null.setText("當前平均每坪房價: " + nowprice + " ===>" + "\n" + " 未來平均每坪房價: " + futureprice);
    }

    private void fetchDataFromServer3(String RecyclerView_url) {
        new Thread(() -> {
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

                    new Handler(Looper.getMainLooper()).post(() -> {
                        new Handler(Looper.getMainLooper()).post(() -> {

                            dataList.clear();
                            dataList.addAll(fetchedData);
                            adapter.notifyDataSetChanged();
                        });
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<MyDataModel> parseResult(String result) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<MyDataModel>>() {
        }.getType();
        List<MyDataModel> dataList = gson.fromJson(result, listType);

        for (MyDataModel data : dataList) {
            String year = data.getYear();
            String month = data.getMonth();
            String date = year + "-" + (month.length() == 1 ? "0" + month : month);
            data.setDate(date);
        }
        return dataList;
    }
}
