package com.zealhack.keralafloodemergencyservicesystem;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    GoogleMap googleMap;

    LocationTrack locationTrack;

    TextView textViewLocation;
    TextView textViewStatus;

    JSONObject data = null;
    JSONObject mainObject;
    String humidity;

    JSONObject dataElavation;
    JSONArray dataElavationArray;
    JSONObject dataElavationObject;
    double elevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLocation = (TextView)findViewById(R.id.current_location);
        textViewStatus = (TextView)findViewById(R.id.flood_alert_status);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;}

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_1:
                Intent myIntent = new Intent(this, FloodedRoads.class);
                this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void refreshButtonFunction(View view){

        if(checkPermission())
            googleMap.setMyLocationEnabled(true);
        else askPermission();

        googleMap.setMyLocationEnabled(true);

        locationTrack = new LocationTrack(MainActivity.this);


        double latitude = locationTrack.getLatitude();
        double longitude = locationTrack.getLongitude();

        LatLng coordinate = new LatLng(latitude, longitude); //Store these lat lng values somewhere. These should be constant.
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                coordinate, 15);
        googleMap.animateCamera(location);

        String latitudeString = String.valueOf(latitude);
        String longitudeString = String.valueOf(longitude);

        getHumidity(latitudeString, longitudeString);
        getElevation(latitudeString, longitudeString);

        String stringLatitude = String.valueOf(latitudeString+", "+longitudeString);
        textViewLocation.setText(stringLatitude);
    }

    public void getHumidity(final String latitudeString, final String longitudeString) {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+latitudeString+"&lon="+longitudeString+"&APPID=ea574594b9d36ab688642d5fbeab847e");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    data = new JSONObject(json.toString());

                    if(data.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }


                } catch (Exception e) {

                    System.out.println("Exception "+ e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(data!=null){
                    Log.d("my weather received",data.toString());

                    try{
                        mainObject = data.getJSONObject("main");
                        humidity = mainObject.getString("humidity");
                    }
                    catch (Exception e){}



                    if(checkFloodProbabilityStatus(Integer.parseInt(humidity), elevation)){
                        textViewStatus.setText("Your location have a humidity of "+humidity+" g/m3 and altitude of "+elevation+" m , which may cause long lasting heavy rainfall. So there may be a probability of flood in your area.");
                    }else{
                        textViewStatus.setText("Your location have a humidity of "+humidity+" g/m3 and altitude of "+elevation+" m , there won't be any heavy rainfall . So you are safe from flood.");
                    }
                }

            }
        }.execute();
    }

    public void getElevation(final String latitudeString, final String longitudeString){

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations="+latitudeString+","+longitudeString+"&key=AIzaSyCpcX-lrVxsuZruEPmSq9dbhBdkB9ccu48");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    dataElavation = new JSONObject(json.toString());

                    if(dataElavation.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }


                } catch (Exception e) {

                    System.out.println("Exception "+ e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if(dataElavation!=null){
                    Log.d("my altitude JSON",dataElavation.toString());

                    try{
                        dataElavationArray = dataElavation.getJSONArray("results");

                        dataElavationObject = dataElavationArray.getJSONObject(0);
                        elevation = dataElavationObject.getDouble("elevation");
                    }
                    catch (Exception e){}
                }

                Log.d("my altitude received",String.valueOf(elevation));

                if(checkFloodProbabilityStatus(Integer.parseInt(humidity), elevation)){
                    textViewStatus.setText("Your location have a humidity of "+humidity+" g/m3 and altitude of "+elevation+" m , which may cause long lasting heavy rainfall. So there may be a probability of flood in your area.");
                }else{
                    textViewStatus.setText("Your location have a humidity of "+humidity+" g/m3 and altitude of "+elevation+" m , there won't be any heavy rainfall . So you are safe from flood.");
                }
            }
        }.execute();
    }

    public boolean checkFloodProbabilityStatus(int humidity, double elevation){
        if(humidity >= 80 && elevation < 7){
            return true;
        }else {
            return false;
        }
    }
}
