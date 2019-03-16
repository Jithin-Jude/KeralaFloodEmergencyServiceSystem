package com.zealhack.keralafloodemergencyservicesystem;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GetHelpActivity extends AppCompatActivity {

    String latitudeString;
    String longitudeString;

    JSONObject dataElavation;
    JSONArray dataElavationArray;
    JSONObject dataElavationObject;
    double elevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_help);
        setTitle("Get Help");

        Intent intent = getIntent();
        latitudeString = intent.getStringExtra("LATITUDE");
        longitudeString = intent.getStringExtra("LONGITUDE");

        //Toast.makeText(this,"altitude : "+latitudeString, Toast.LENGTH_SHORT).show();
    }

    public void ConnectVolunteers(View view){
        Intent myIntent = new Intent(this, ConnectVolunteersActivity.class);
        startActivity(myIntent);
    }

    public void HelpLineFunction(View view){
        try {
            Intent callIntent = new Intent("com.android.phone.EmergencyDialer.DIAL");
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            callIntent.setData(Uri.parse("tel:112"));
            startActivity(callIntent);
        } catch (Exception e) {
            // in case something went wrong ...
        }
    }

    public void SafeLocation(View view){

        double x = Double.valueOf(latitudeString);
        double y = Double.valueOf(longitudeString);

        double altOneLat = x+0.01;
        double altOneLon = y;

        double altTwoLat = x-0.01;
        double altTwoLon = y;

        double altThreeLat = x;
        double altThreeLon = y+0.01;

        double altFourLat = x;
        double altFourLon = y-0.01;

        //case_1
        double altitudeOne = getAltitude(x+0.01, y);
        double altitudeTwo = getAltitude(x-0.01, y);
        double altitudeThree = getAltitude(x, y+0.01);
        double altitudeFour = getAltitude(x, y-0.01);

        String highAlt = placeWithHighAltitude(altitudeOne, altitudeTwo, altitudeThree, altitudeFour);

        Toast.makeText(this,"Move to Safe Location "+highAlt, Toast.LENGTH_SHORT).show();

        if(highAlt.equals("altitudeOne")){
            String geoUri = "http://maps.google.com/maps?q=loc:" + altOneLat + "," + altOneLon + " (Safe Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);

        }else if(highAlt.equals("altitudeTwo")){
            String geoUri = "http://maps.google.com/maps?q=loc:" + altTwoLat + "," + altTwoLon + " (Safe Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);
        }else if(highAlt.equals("altitudeThree")){
            String geoUri = "http://maps.google.com/maps?q=loc:" + altThreeLat + "," + altThreeLon + " (Safe Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);
        }else if(highAlt.equals("altitudeFour")){
            String geoUri = "http://maps.google.com/maps?q=loc:" + altFourLat + "," + altFourLon + " (Safe Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);
        }else{
            String geoUri = "http://maps.google.com/maps?q=loc:" + x + "," + y + " (Safe Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);
        }
    }

    String placeWithHighAltitude(double altitudeOne, double altitudeTwo, double altitudeThree, double altitudeFour){
        String highAltitude;
        if(altitudeOne >= altitudeTwo && altitudeOne >= altitudeThree && altitudeOne >= altitudeFour){
            highAltitude = "altitudeOne";
        }else if(altitudeTwo >= altitudeOne && altitudeTwo >= altitudeThree && altitudeTwo >= altitudeFour){
            highAltitude = "altitudeTwo";
        }else if(altitudeThree >= altitudeOne && altitudeThree >= altitudeTwo && altitudeThree >= altitudeThree){
            highAltitude = "altitudeThree";
        }else {
            highAltitude = "altitudeFour";
        }

        return highAltitude;
    }

    public double getAltitude(double x, double y){


        try{
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
                }
            }.execute().get(5, TimeUnit.MILLISECONDS);
        }catch (Exception e){}

        return elevation;
    }
}
