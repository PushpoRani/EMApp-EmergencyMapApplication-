package pushpo.emapp.maputils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;



import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pushpo.emapp.R;
import pushpo.emapp.activity.MapsActivity;

public class MyMapUtils implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap googleMap;
    private MapsActivity mContext;
    private GoogleApiClient mGoogleApiClient;
    private List<Polyline> polylines = new ArrayList<>();

    public MyMapUtils(MapsActivity mContext, GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.mContext = mContext;
    }

    public void getDirectionOnMap(LatLng myorigin, LatLng mydest) {
        ArrayList<LatLng> markerPoints = new ArrayList<>();

        if (markerPoints.size() > 1) {
            markerPoints.clear();
        }

        markerPoints.add(myorigin);
        markerPoints.add(mydest);

        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng destination = markerPoints.get(1);
            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, destination);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            if (!polylines.isEmpty()) {
                for (Polyline line : polylines) {
                    line.remove();
                }
            }
            if (result == null) {
                return;
            }

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble(String.valueOf(point.get("lat")));
                    double lng = Double.parseDouble(String.valueOf(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(ContextCompat.getColor(mContext, R.color.color_primary));
                lineOptions.geodesic(true);

            }
            if (lineOptions == null) {
                Toast.makeText(mContext, "No path found", Toast.LENGTH_LONG).show();
                return;
            }
            // Drawing polyline in the Google Map for the i-th route
            polylines.add(googleMap.addPolyline(lineOptions));
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyDJsCR572XabzhL_yrT4__i3IHcXU7LnSc";


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // When connected it will get your current location and display marker
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(mContext, "Please check internet", Toast.LENGTH_SHORT).show();

    }

//    public String findDistanceFromMyLocation(Marker marker) {
//        double markerPositionLtd = marker.getPosition().latitude;
//        double markerPositionLng = marker.getPosition().longitude;
//      double distance = getDistanceFromLocationByLtdLng(markerPositionLtd, markerPositionLng);
//        return String.valueOf(distance);
//    }
//
//    public double getDistanceFromLocationByLtdLng(double ltd, double lng) {
//        double distance = 0;
//        LatLng latLngTo = new LatLng(ltd, lng);
//        LatLng latLngFrom = new LatLng(AppConstant.MY_LOCATION_LATITUTE, AppConstant.MY_LOCATION_LONGITUTE);
//        DecimalFormat df = new DecimalFormat("#.##");
//        distance = SphericalUtil.computeDistanceBetween(latLngFrom, latLngTo);
//        distance = distance / 1000;
//        return Double.parseDouble(df.format(distance));
//    }
}
