package com.june.artourguide;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.PrintStream;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private Task locationResult;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 18;
    //mDefaultLocation set as Kampar
    private final LatLng mDefaultLocation = new LatLng(4.2509284,101.0585763);

    private static final LatLng LOCATION_A = new LatLng(4.329377, 101.136374);
    private static final LatLng LOCATION_B = new LatLng(4.329332, 101.135703);
    private static final LatLng LOCATION_C = new LatLng(4.329597, 101.135443);

    private Marker mLocationA;
    private Marker mLocationB;
    private Marker mLocationC;

    private static final String directionsTag = "Directions API Request";
    private JSONObject directionsJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Welcome to Kampar"));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        getDeviceLocation();
        addMarkers();

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!mPermissionDenied) {
                locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location)task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            getDirections();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        Toast.makeText(this,
                marker.getTitle(),
                Toast.LENGTH_SHORT).show();


        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    public void addMarkers() {
        // Add some markers to the map, and add a data object to each marker.
        mLocationA = mMap.addMarker(new MarkerOptions()
                .position(LOCATION_A)
                .title("Location A"));
        mLocationA.setTag(0);

        mLocationB = mMap.addMarker(new MarkerOptions()
                .position(LOCATION_B)
                .title("Location B"));
        mLocationB.setTag(0);

        mLocationC = mMap.addMarker(new MarkerOptions()
                .position(LOCATION_C)
                .title("Location C"));
        mLocationA.setTag(0);

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
    }

    public void getDirections() {
        String directionsBaseURL = "https://maps.googleapis.com/maps/api/directions/json?";
        String APIkey = getResources().getString(R.string.google_maps_key);
        String origin = mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
        String destination = LOCATION_C.latitude + "," + LOCATION_C.longitude;
        LatLng[] waypoints = {LOCATION_A, LOCATION_B};

        String strWaypoints = "";

        for (int i = 0; i < waypoints.length; i++) {
            strWaypoints += waypoints[i].latitude + "," + waypoints[i].longitude;

            if (i < waypoints.length - 1) {
                strWaypoints += "|";
            }
        }

        String directionsURL = directionsBaseURL
                + "origin=" + origin
                + "&destination=" + destination
                + "&waypoints=" + strWaypoints
                + "&key=" + APIkey;

        Log.d("directionsURL", directionsURL);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a json response from the provided URL.
        final JsonObjectRequest directionsJSONRequest = new JsonObjectRequest
                (Request.Method.GET, directionsURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(directionsTag, "Response: " + response.toString());
                        directionsJSON = response;
                        try {
                            JSONArray routes = directionsJSON.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedPoints = overviewPolyline.getString("points");
                            List<LatLng> decodedPoints = decodePoly(encodedPoints);

                            //Create PolylineOptions
                            PolylineOptions polylineOptions = new PolylineOptions();

                            for (int i = 0; i < decodedPoints.size(); i++) {
                                polylineOptions.add(decodedPoints.get(i));
                            }

                            // Add polylines to the map.
                            // Polylines are useful to show a route or some other connection between points.
                            Polyline polyline1 = mMap.addPolyline(polylineOptions);

                            //Store a data object with the polyline, used here to indicate an arbitrary type.
                            polyline1.setTag("Route A");



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(directionsTag, error.toString());

                    }
                });


        // Set the tag on the request.
        directionsJSONRequest.setTag(directionsTag);

        // Add the request to the RequestQueue.
        queue.add(directionsJSONRequest);


    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}
