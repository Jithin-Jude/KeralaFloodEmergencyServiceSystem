package com.zealhack.keralafloodemergencyservicesystem;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    GoogleMap googleMap;

    LocationTrack locationTrack;

    TextView textViewLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if(checkPermission())
            googleMap.setMyLocationEnabled(true);
        else askPermission();

        googleMap.setMyLocationEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(8.5241,76.9366), 10));
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }
    private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                7
        );
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_options_menu, menu);
        return true;
    }

    public void refreshButtonFunction(View view){
        Toast.makeText(this,"getting location",Toast.LENGTH_SHORT).show();

        if(checkPermission())
            googleMap.setMyLocationEnabled(true);
        else askPermission();

        locationTrack = new LocationTrack(MainActivity.this);




            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            String longitudeString = String.valueOf(longitude);
            String latitudeString = String.valueOf(latitude);

        String stringLatitude = String.valueOf(longitudeString+", "+latitudeString);
        textViewLocation = (TextView)findViewById(R.id.current_location);
        textViewLocation.setText(stringLatitude);
    }
}
