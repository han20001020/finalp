package com.example.map;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner spinner1, spinner2,spinner3;
    private String markertitle1, markertitle2, markertitle3 , markertitle0;
    private String time, year, season , city;
    private String egg_url,egg2_url, url_getprice, chart_url, RecyclerView_url;
    private Marker marker1,marker2,marker3,marker4;
    private String url_now, url_futrue;
    private Circle circle1,circle2,circle3,circle4;
    private Marker movingMarker1,movingMarker2,movingMarker3;
    private Button clearButton, SearchButton;
    private PolylineOptions polylineOptions1,polylineOptions2,polylineOptions3;
    private LatLng location1, location2, location3, location4;
    private Bitmap markerIcon,markerIcon1,markerIcon2,markerIcon3,movingMarkerIcon;
    private boolean isAnimating = false;
    private Switch themeSwitch;

    private LatLng focus;

    private static final LatLng TAIPEI_CITY = new LatLng(25.0330, 121.5654);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        // 綁定地圖
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 綁定 UI 元件
        themeSwitch = view.findViewById(R.id.themeswitch);
        spinner1 = view.findViewById(R.id.spinner1);
        spinner2 = view.findViewById(R.id.spinner2);
        spinner3 = view.findViewById(R.id.spinner3);
        clearButton = view.findViewById(R.id.clearButton);
        SearchButton = view.findViewById(R.id.search_button);


        url_getprice = "http://3.27.231.134/getprice.php";

        if (themeSwitch != null) {
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;

            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                themeSwitch.setChecked(true);
            } else {
                themeSwitch.setChecked(false);
            }
        }

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.year_option, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.season_option, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getContext(),
                R.array.city_option, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);


        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = year + season;
                if(city != null || year != null ||season != null){
                egg_url = "http://3.27.231.134/egg.php" +"?city="+ city + "&year="+year+"&season="+season;
                egg2_url = "http://3.27.231.134/egg2.php"+"?city="+ city;
                if (time.equals("113Q1") || time.equals("113Q2")) {
                    Toast.makeText(getContext(), "此時間非未來 無法預測", Toast.LENGTH_SHORT).show();
                    return;
                }if (getFocus(city)!=null)
                {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getFocus(city),12));}
                else {mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getFocus(city),10));}
                fetchDataFromServer();
                fetchDataFromServer1();
                updateMap();}
            }
        });

        setupSpinnerListeners();

        setupClearButtonListener();

        return view;
    }

    private void setupSpinnerListeners() {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = getYear(position-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                season = getSeason(position-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = getcity(position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String getcity(int position)
    {
        switch (position)
        {
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

    private LatLng getFocus(String place)
    {
        switch (place)
        {
            case "臺北市":
                return new LatLng(25.0375, 121.5580);
            case "新北市":
                return new LatLng(24.9376,121.5615);
            case "基隆市":
                return new LatLng(25.1316,121.7327);
            default:
                return new LatLng(25.0356,121.6174);
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


    private void fetchDataFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL requestUrl = new URL(egg_url);
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
                        Log.d("ServerResponse", "Response from server: " + box.toString());

                        JSONArray jsonArray = new JSONArray(box.toString().trim());

                        if (jsonArray.length() > 0) {
                            markertitle1 = jsonArray.getString(0);
                        }
                        if (jsonArray.length() > 1) {
                            markertitle2 = jsonArray.getString(1);
                        }
                        if (jsonArray.length() > 2) {
                            markertitle3 = jsonArray.getString(2);
                        }
                        Log.d("fetchData", "markertitle1" + markertitle1);

                        location2 = getLocation(markertitle1);
                        location3 = getLocation(markertitle2);
                        location4 = getLocation(markertitle3);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateMap();
                            }
                        });
                    }

                } catch (Exception e) {
                    markertitle1 = e.toString();
                    markertitle2 = "";
                    markertitle3 = "";
                    Log.e("Fetch Data Error", "Error fetching data from server", e);
                }
            }
        }).start();
    }
    private void fetchDataFromServer1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL requestUrl = new URL(egg2_url);
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
                        Log.d("ServerResponse", "Response from server: " + box.toString());

                        JSONArray jsonArray = new JSONArray(box.toString().trim());

                        if (jsonArray.length() > 0) {
                            markertitle0 = jsonArray.getString(0);
                        }
                        Log.d("fetchData", "markertitle0" + markertitle0);

                        location1 = getLocation(markertitle0);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateMap();
                            }
                        });
                    }

                } catch (Exception e) {
                    markertitle0 = e.toString();
                    Log.e("Fetch Data Error", "Error fetching data from server", e);
                }
            }
        }).start();
    }

    private LatLng getLocation(String markertitle) {
        if(city.equals("臺北市")){
        switch (markertitle) {
            case "中山區":
                return new LatLng(25.0685, 121.5250);
            case "中正區":
                return new LatLng(25.0327,121.5183);
            case "信義區":
                return new LatLng(25.0330,121.5645);
            case "內湖區":
                return new LatLng(25.0831,121.5866);
            case "北投區":
                return new LatLng(25.1212,121.5150);
            case "南港區":
                return new LatLng(25.0385,121.6145);
            case "士林區":
                return new LatLng(25.0958,121.5245);
            case "大同區":
                return new LatLng(25.0630,121.5135);
            case "大安區":
                return new LatLng(25.0266,121.5428);
            case "文山區":
                return new LatLng(24.9882,121.5701);
            case "松山區":
                return new LatLng(25.0578,121.5578);
            case "萬華區":
                return new LatLng(25.0296,121.4972);
            default:
                return null;
            }
        }
        else if(city.equals("新北市")){
            switch (markertitle){
                case "板橋區":
                    return new LatLng(25.0110, 121.4628);
                case "三重區":
                    return new LatLng(25.0725, 121.4845);
                case "中和區":
                    return new LatLng(24.9936, 121.4941);
                case "永和區":
                    return new LatLng(25.0014, 121.5145);
                case "新莊區":
                    return new LatLng(25.0352, 121.4325);
                case "新店區":
                    return new LatLng(24.9376, 121.5395);
                case "樹林區":
                    return new LatLng(24.9743, 121.4225);
                case "鶯歌區":
                    return new LatLng(24.9540, 121.3542);
                case "三峽區":
                    return new LatLng(24.9350, 121.3686);
                case "淡水區":
                    return new LatLng(25.1645, 121.4465);
                case "汐止區":
                    return new LatLng(25.0685, 121.6402);
                case "瑞芳區":
                    return new LatLng(25.1082, 121.8025);
                case "土城區":
                    return new LatLng(24.9727, 121.4467);
                case "蘆洲區":
                    return new LatLng(25.0850, 121.4637);
                case "五股區":
                    return new LatLng(25.0822, 121.4426);
                case "泰山區":
                    return new LatLng(25.0579, 121.4321);
                case "林口區":
                    return new LatLng(25.0792, 121.3779);
                case "深坑區":
                    return new LatLng(24.9975, 121.6167);
                case "石碇區":
                    return new LatLng(24.9692, 121.6577);
                case "坪林區":
                    return new LatLng(24.9340, 121.7161);
                case "三芝區":
                    return new LatLng(25.2576, 121.5008);
                case "石門區":
                    return new LatLng(25.2902, 121.5644);
                case "八里區":
                    return new LatLng(25.1473, 121.4048);
                case "平溪區":
                    return new LatLng(25.0270, 121.7385);
                case "雙溪區":
                    return new LatLng(25.0338, 121.8387);
                case "貢寮區":
                    return new LatLng(25.0150, 121.9191);
                case "金山區":
                    return new LatLng(25.2205, 121.6370);
                case "萬里區":
                    return new LatLng(25.1746, 121.6597);
                case "烏來區":
                    return new LatLng(24.8657, 121.5500);
                default:
                    return null;
            }
        }
        else {
            switch (markertitle){
                case "仁愛區":
                    return new LatLng(25.1276, 121.7392);
                case "信義區":
                    return new LatLng(25.1223, 121.7565);
                case "中正區":
                    return new LatLng(25.1504, 121.7730);
                case "中山區":
                    return new LatLng(25.1477, 121.7240);
                case "安樂區":
                    return new LatLng(25.1530, 121.7071);
                case "暖暖區":
                    return new LatLng(25.0912, 121.7407);
                case "七堵區":
                    return new LatLng(25.0986, 121.6833);
                default:
                    return null;
            }
        }
    }


    private String getMarkertitle(Marker marker)
    {
        if(marker.equals(marker1)){
            return markertitle0;
        }
        else if(marker.equals(marker2)){
            return markertitle1;}
        else if(marker.equals(marker3)){
            return markertitle2;}
        else if(marker.equals(marker4)){
            return markertitle3;}
        else
            return null;
    }
    private String getUrl1(){

        return url_futrue=(url_getprice+"?city="+city+"&area="+markertitle0+"&year="+year+"&season="+season);
    }

    private String getUrl2(String markertitle){
        return url_futrue=(url_getprice+"?city="+city+"&area="+markertitle+"&year="+year+"&season="+season);
    }

    private String getUrlnow(String markertitle){
        return url_now = (url_getprice+"?city="+city+"&area="+markertitle+"&year="+"113"+"&season="+"Q2");
    }
    private String getChart_url(String markertitle){
        chart_url = ("http://3.27.231.134/chart.php");
        return chart_url=(chart_url+"?city="+city+"&area="+markertitle);
    }

    private String getRecyclerView_url(String markertitle){
        RecyclerView_url = ("http://3.27.231.134/RecyclerView.php");
        return RecyclerView_url=(RecyclerView_url+"?city="+city+"&area="+markertitle);
    }

    LatLng midPoint(LatLng start, LatLng end) {
        double midLat = (start.latitude + end.latitude) / 2;
        double midLng = (start.longitude + end.longitude) / 2;
        return new LatLng(midLat, midLng);
    }

    LatLng getPerpendicularDirection(LatLng start, LatLng end) {
        double latDiff = end.latitude - start.latitude;
        double lngDiff = end.longitude - start.longitude;

        double perpendicularLat = -lngDiff;
        double perpendicularLng = latDiff;

        double magnitude = Math.sqrt(perpendicularLat * perpendicularLat + perpendicularLng * perpendicularLng);
        perpendicularLat /= magnitude;
        perpendicularLng /= magnitude;

        return new LatLng(perpendicularLat, perpendicularLng);
    }

    LatLng getParabolicPeak(LatLng start, LatLng end, double heightFactor) {
        LatLng midPoint = midPoint(start, end);
        LatLng perpendicularDirection = getPerpendicularDirection(start, end);

        double peakLat = midPoint.latitude + heightFactor * perpendicularDirection.latitude;
        double peakLng = midPoint.longitude + heightFactor * perpendicularDirection.longitude;

        return new LatLng(peakLat, peakLng);
    }

    public List<LatLng> getParabolicCurve(LatLng start, LatLng end, int numPoints, double heightFactor) {
        List<LatLng> points = new ArrayList<>();

        LatLng peak = getParabolicPeak(start, end, heightFactor);

        for (int i = 0; i <= numPoints; i++) {
            double t = (double) i / numPoints;

            // 插值公式，通過 start, peak, end 生成拋物線
            double lat = (1 - t) * (1 - t) * start.latitude + 2 * (1 - t) * t * peak.latitude + t * t * end.latitude;
            double lng = (1 - t) * (1 - t) * start.longitude + 2 * (1 - t) * t * peak.longitude + t * t * end.longitude;

            points.add(new LatLng(lat, lng));
        }

        return points;
    }


    private void updateMap() {


        if (location1 != null && location2 != null && mMap != null) {
            mMap.clear();


            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    float zoomLevel = mMap.getCameraPosition().zoom;
                    updateMarkerIconSize(zoomLevel);
                }
            });

            markerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.egg);
            markerIcon1 = BitmapFactory.decodeResource(getResources(), R.drawable.first);
            markerIcon2 = BitmapFactory.decodeResource(getResources(), R.drawable.second);
            markerIcon3 = BitmapFactory.decodeResource(getResources(), R.drawable.third);
            movingMarkerIcon = BitmapFactory.decodeResource(getResources(),R.drawable.money);

            marker1 = mMap.addMarker(new MarkerOptions().position(location1).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(markerIcon, 0.03f))).title("當前蛋黃區: " + markertitle0));
            marker2 = mMap.addMarker(new MarkerOptions().position(location2).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(markerIcon1, 0.15f))).title("未來蛋黃區1st: " + markertitle1));
            marker2.showInfoWindow();
            marker3 = mMap.addMarker(new MarkerOptions().position(location3).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(markerIcon2, 0.15f))).title("未來蛋黃區2st: " + markertitle2));
            marker4 = mMap.addMarker(new MarkerOptions().position(location4).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(markerIcon3, 0.15f))).title("未來蛋黃區3rd: " + markertitle3));

            // 添加拋物線 Polyline
            List<LatLng> curvePoints1 = getParabolicCurve(marker1.getPosition(), marker2.getPosition(), 100, -0.05);
            List<LatLng> curvePoints2 = getParabolicCurve(marker1.getPosition(), marker3.getPosition(), 100, -0.05);
            List<LatLng> curvePoints3 = getParabolicCurve(marker1.getPosition(), marker4.getPosition(), 100, -0.05);
            polylineOptions1 = new PolylineOptions().addAll(curvePoints1).color(Color.RED).width(7);
            polylineOptions2 = new PolylineOptions().addAll(curvePoints2).color(Color.BLUE).width(7);
            polylineOptions3 = new PolylineOptions().addAll(curvePoints3).color(Color.BLACK).width(7);
            mMap.addPolyline(polylineOptions1);
            mMap.addPolyline(polylineOptions2);
            mMap.addPolyline(polylineOptions3);

            CircleOptions circleOptions1 = new CircleOptions()
                    .center(location1)
                    .radius(1500)
                    .strokeColor(Color.RED)
                    .fillColor(0x80FFD700)
                    .strokeWidth(2);

            CircleOptions circleOptions2 = new CircleOptions()
                    .center(location2)
                    .radius(1500)
                    .strokeColor(Color.YELLOW)
                    .fillColor(0x80FFD700)
                    .strokeWidth(2);

            CircleOptions circleOptions3 = new CircleOptions()
                    .center(location3)
                    .radius(1500)
                    .strokeColor(Color.YELLOW)
                    .fillColor(0x80FFD700)
                    .strokeWidth(2);

            CircleOptions circleOptions4 = new CircleOptions()
                    .center(location4)
                    .radius(1500)
                    .strokeColor(Color.YELLOW)
                    .fillColor(0x80FFD700)
                    .strokeWidth(2);

            circle1 = mMap.addCircle(circleOptions1);
            circle2 = mMap.addCircle(circleOptions2);
            circle3 = mMap.addCircle(circleOptions3);
            circle4 = mMap.addCircle(circleOptions4);

            if (movingMarker1 != null) {
                movingMarker1.remove();
            }
            movingMarker1 = mMap.addMarker(new MarkerOptions()
                    .position(location1)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(movingMarkerIcon,1f))));
            if (movingMarker2 != null) {
                movingMarker2.remove();
            }
            movingMarker2 = mMap.addMarker(new MarkerOptions()
                    .position(location1)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(movingMarkerIcon,1f))));
            if (movingMarker3 != null) {
                movingMarker3.remove();
            }
            movingMarker3 = mMap.addMarker(new MarkerOptions()
                    .position(location1)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(movingMarkerIcon,1f))));

            isAnimating = false;
            animateMarker(movingMarker1,location1, location2,-0.05);
            animateMarker(movingMarker2,location1, location3,-0.05);
            animateMarker(movingMarker3,location1, location4,-0.05);

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.equals(marker1)) {
                        String title = getMarkertitle(marker);
                        url_now = (url_getprice +"?city="+city+ "&area=" + markertitle0 + "&year="+"113"+"&season="+"Q2");
                        url_futrue = getUrl1();
                        chart_url = getChart_url(title);
                        RecyclerView_url = getRecyclerView_url(title);
                        onMarkerClickAction(marker1);
                        return true;
                    } else {
                        String title = getMarkertitle(marker);
                        Log.d("MarkerTitle", "Title: " + title);

                        url_now = getUrlnow(title);
                        url_futrue = getUrl2(title);
                        chart_url = getChart_url(title);
                        RecyclerView_url = getRecyclerView_url(title);
                        onMarkerClickAction(marker);
                        return true;
                    }
                }
            });
        } else {
            mMap.clear();
            location2 = null;
            if (movingMarker1 != null)
                movingMarker1.remove();
            movingMarker1 = null;
            if (movingMarker2 != null)
                movingMarker2.remove();
            movingMarker2 = null;
            if (movingMarker3 != null)
                movingMarker3.remove();
            movingMarker3 = null;
            isAnimating = false;
            polylineOptions1 = null;
            polylineOptions2 = null;
            polylineOptions3 = null;
            if(circle1!=null)
                circle1.remove();
            if(circle2!=null)
                circle2.remove();
        }
    }
    private void updateMarkerIconSize(float zoomLevel) {
        float scaleFactor = zoomLevel / 15.0f;

        Bitmap resizedIcon = resizeBitmap(markerIcon, scaleFactor);
        if (resizedIcon != null) {
            marker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        } else {
            Log.e("MapError", "Resized bitmap is null, cannot set icon.");
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, float scale) {
        if (bitmap == null) {
            Log.e("MapError", "Bitmap is null, cannot resize.");
            return null;
        }

        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);

        if (width <= 0 || height <= 0) {
            Log.e("MapError", "Invalid bitmap size, cannot resize.");
            return bitmap;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private void animateMarker(final Marker marker, final LatLng startPosition, final LatLng endPosition, final double heightFactor) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        final long duration = 5000;

        final LatLng peakPosition = getParabolicPeak(startPosition, endPosition, heightFactor);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                // 計算拋物線上的新位置
                double lat = (1 - t) * (1 - t) * startPosition.latitude + 2 * (1 - t) * t * peakPosition.latitude + t * t * endPosition.latitude;
                double lng = (1 - t) * (1 - t) * startPosition.longitude + 2 * (1 - t) * t * peakPosition.longitude + t * t * endPosition.longitude;

                // 更新 marker 位置
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    marker.setPosition(endPosition);


                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            animateMarker(marker, startPosition, endPosition, heightFactor);
                        }
                    }, 1000);
                }
            }
        });
    }



    private void setupClearButtonListener() {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMarkersAndResetSpinners();
            }
        });
    }

    private void clearMarkersAndResetSpinners() {
        if (mMap != null) {
            mMap.clear();
        }

        location1 = null;
        location2 = null;
        location3 = null;
        location4 = null;

        if (movingMarker1 != null) movingMarker1.remove();
        if (movingMarker2 != null) movingMarker2.remove();
        if (movingMarker3 != null) movingMarker3.remove();

        movingMarker1 = null;
        movingMarker2 = null;
        movingMarker3 = null;


        spinner1.setSelection(0);
        spinner2.setSelection(0);
        spinner3.setSelection(0);


        if (circle1 != null) circle1.remove();
        if (circle2 != null) circle2.remove();
        if (circle3 != null) circle3.remove();
        if (circle4 != null) circle4.remove();

        markerIcon = null;
        markerIcon1 = null;
        markerIcon2 = null;
        markerIcon3 =null;

        isAnimating = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TAIPEI_CITY, 10));
    }

    private void onMarkerClickAction(Marker marker) {

        Intent intent = new Intent(requireActivity(),MarkerDetailActivity.class);
        intent.putExtra("title", marker.getTitle());
        intent.putExtra("nowprice_url", url_now);
        intent.putExtra("futureprice_url", url_futrue);
        intent.putExtra("chart_url",chart_url);
        intent.putExtra("RecyclerView_url",RecyclerView_url);
        startActivity(intent);
    }
}
