package com.example.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    TextView addressTextView, subjectTextView, priceTextView, dateTextView;
    private GoogleMap mMap;
    private String address;
    private static final String API_KEY = "AIzaSyBB7R--aYp3WppDQLUkP_98QaQv02HNqmM";
    private Bitmap mrtIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mrtIcon = BitmapFactory.decodeResource(getResources(), R.drawable.mrt);;

        addressTextView = findViewById(R.id.detailAddress);
        subjectTextView = findViewById(R.id.detailSubject);
        priceTextView = findViewById(R.id.detailPrice);
        dateTextView = findViewById(R.id.detailDate);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        String subject = intent.getStringExtra("subject");
        String price = intent.getStringExtra("price");
        String date = intent.getStringExtra("date");

        addressTextView.setText("地址: " + address);
        subjectTextView.setText("交易標的: " + subject);
        priceTextView.setText("坪數單價: " + price);
        dateTextView.setText("交易日期: " + date);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                LatLng location = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                mMap.addMarker(new MarkerOptions().position(location).title(address));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

                searchNearbySubwayStations(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.setOnCameraIdleListener(() -> {
            float zoomLevel = mMap.getCameraPosition().zoom;
            updateMarkerIconSize(zoomLevel);
        });
    }

    private void searchNearbySubwayStations(LatLng location) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.latitude + "," + location.longitude +
                "&radius=500" +  // 搜索半徑500公尺
                "&type=subway_station" +  // 查詢捷運站
                "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseNearbySearchResults(responseData);
                }
            }
        });
    }

    private void parseNearbySearchResults(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray resultsArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject place = resultsArray.getJSONObject(i);
                String placeName = place.getString("name");
                JSONObject geometry = place.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                runOnUiThread(() -> {
                    LatLng placeLocation = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(placeLocation).icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(mrtIcon, 0.1f))).title(placeName));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMarkerIconSize(float zoomLevel) {
        float scaleFactor = zoomLevel / 15.0f;
    }
    private Bitmap resizeBitmap(Bitmap bitmap, float scale) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }
}


