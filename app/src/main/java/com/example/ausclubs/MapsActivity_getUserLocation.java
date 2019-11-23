package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.ausclubs.Constants.ERROR_DIALOG_REQUEST;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MapsActivity_getUserLocation extends FragmentActivity implements OnMapReadyCallback {


    /*
    This is the Map Fragment Activity to be used for choosing event location, it also has an added feature that utilizes the
    google Geocoder API in order to allow the user to be able to search for a location and use that as the event location
     */

    public static final String TAG = "MapsActivity";

    public static final float DEFAULT_ZOOM = 15f;
    private EditText mSearchText;
    private ProgressDialog progressDialog;
    private boolean mLocationPermissionGranted = false;

    private FusedLocationProviderClient mFusedLocationProvider;
    private GoogleMap mMap;

    private ImageView chooseloc;
    private LatLng finalloc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_get_user_location);//Set Map Fragment Layout
        mSearchText = (EditText) findViewById(R.id.input_search);//Search EditText so that the user can search for places
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finding...");//Progress Dialog to show while the app is finding the searched locztion
        progressDialog.setCancelable(false);

        chooseloc = (ImageView) findViewById(R.id.ic_choosethisloc);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getLocationPermission();//Request permission if it has already not been asked

    }



    private void geoLocate(){//This is the function that will search for the location that has been typed into the search bar
        Log.d("Geolocate","Geolocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity_getUserLocation.this);//Instantiage Geocoder class that will retrieve location from search string
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,1);//Will return a maximum of locatino which has the highest hcance of matching with the users search input
        } catch (IOException e) {
            Log.e("geolocate",e.getMessage());
        }
        if(list.size()>0){
            Address address = list.get(0);//Get the first value sent (Max results are 1)
            Log.d(TAG, address.toString());
            mMap.clear();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));//Move Camera to retrived Lat Long from searched address

            finalloc = new LatLng(address.getLatitude(), address.getLongitude());//Set searched address as the location to be returned
        }
        else{
            Log.d(TAG, "Nothing found");
            Toast.makeText(this, "Could not find", Toast.LENGTH_SHORT).show();
        }

    }

    private void init() {
        Log.d("Init","Init:Initialising");
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {//Initialize search Text Listener for return key
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    //Execute geolocate
                    progressDialog.show();
                    geoLocate();//If return key pressed then use the GeoCoder to find the location searched
                    progressDialog.dismiss();
                }
                return false;
            }
        });

        chooseloc.setOnClickListener(new View.OnClickListener() {//This listener will return the set location that has been done by either searching, current user location or by pressing on the map
            @Override
            public void onClick(View v) {
                //return latlong to previous activity
                Intent intent = new Intent();
                double [] locArr = {finalloc.latitude, finalloc.longitude};
                intent.putExtra("Location", locArr);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume(){//Resume map if app was not in focus while the user was enabling location services
        super.onResume();
        if(isMapsEnabled()){
            if(mLocationPermissionGranted){
                initMap();
            }else{
                getLocationPermission();
            }
        }
    }

    private void getLocationPermission() {//Enable location permission if it has not already been done
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMap();
        }
    }

    public boolean isMapsEnabled(){//To check if location services are on, if not then proceed to the settings menu to enable them
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGPS();
            return false;
        }
        return true;

    }

    public boolean isMapsEnabledNoAlert(){//Similar to the above function, except no alerts are created if location services are not available
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //buildAlertMessageNoGPS();
            return false;
        }
        return true;

    }



    private void buildAlertMessageNoGPS(){//Alert to be shown if No GPS services are enabled,
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This App requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//Launch settings if location services are not enabled
                        startActivityForResult(enableGPSIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    public boolean isServicesOK(){ //Check to see if google play services are enabled, this has already been documented in addToFeedActivity, it has been added here again since it is possible that the user can disable location services before the maps is launched
        Log.d(TAG, "Checking google services");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity_getUserLocation.this);

        if(available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Services Running Fine");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "An Error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity_getUserLocation.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//Already documented in addToFeedActivity, placed here again since it is possible that the user can disable location services before the maps is launched
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;
                }
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {//Launch Permissions for location with expected result that gps services have been enabled
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                    initMap();
                }else{
                    getLocationPermission();
                }
        }
    }
    private void initMap() {//Initialize map once all services are enabled and permission are granted
        if(isMapsEnabledNoAlert() &&  ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void getDeviceLocation() {//Get Current location if the user wants to set current location as the event location
        Log.d(TAG, "getDeviceLocation: finding location");

        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);//Get Fused Location Provider

        try {
            if (mLocationPermissionGranted) {//IF permission has been granted then move camera to the current location and set that as the location for the event
                Task location = mFusedLocationProvider.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getDeviceLocation: Found location");
                            Location currentLocation = (Location) task.getResult();
                            LatLng loc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            finalloc = loc;
                            moveCamera(loc, DEFAULT_ZOOM, "Selected location");
                        } else {

                            Log.d(TAG, "getDeviceLocation: Could not find location");
                            Toast.makeText(MapsActivity_getUserLocation.this, "Cannot find device location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (java.lang.NullPointerException n) {//Null Exception can be caused for when the location services are disabled after maps activity has been launched
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            Intent backtoUserMainPage = new Intent(MapsActivity_getUserLocation.this, feedsActivity.class);
            backtoUserMainPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backtoUserMainPage);
        }
        catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: Exception occurred: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {//Function created that will move the map from whereever it is to the LatLng coordinated that are passed to the function
        Log.d(TAG, "moveCamera: Moving camera to lat:" + latLng.latitude + " long:" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {//If map is ready then set the current map camera to be at the users current location
        mMap = googleMap;
        Log.d(TAG, "onMapReady: Map ready");

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);

            init();
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {//This listener will place a marker for whever the user taps so that that location can be used as the event location
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions m = new MarkerOptions();
                m.position(latLng);
                mMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(m);
                finalloc = latLng;
            }
        });

    }


}
