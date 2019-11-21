package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.ausclubs.Constants.ERROR_DIALOG_REQUEST;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class addToFeedActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = "addToFeedActivity";
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView dateTextView;

    private EditText eventDescriptionEditText, eventTitleEditText;
    private Spinner clubNameSpinner;
    private Button uploadButton;
    private int mDay = -1, mMonth = -1, mYear = -1;
    private String datestr = "";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    public static final int REQUEST_LOCATION_CODE = 987;
    private LatLng userloc;
    private boolean mLocationPermissionGranted = false;

    private Button addLocationBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_feed);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        eventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);
        clubNameSpinner = (Spinner) findViewById(R.id.clubNameSpinner);
        eventTitleEditText = (EditText) findViewById(R.id.eventTitleEditText);
        addLocationBtn = (Button) findViewById(R.id.addLocationBtn);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add(Users.C1);
        if(Users.C2 != ""){
            spinnerArray.add(Users.C2);
        }
        getLocationPermission();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.clubNameSpinner);
        sItems.setAdapter(adapter);


        setTitle("Add A Club Event");

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isServicesOK()) {
                    //need to return coordinates
                    Intent gotoMapActivity = new Intent(addToFeedActivity.this, MapsActivity_getUserLocation.class);
                    //gotoMapActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(gotoMapActivity);     //this will change
                    startActivityForResult(gotoMapActivity, REQUEST_LOCATION_CODE);
                }
            }
        });
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(addToFeedActivity.this);
                if(!isValidEdiText(eventDescriptionEditText)  || !isValidEdiText(eventTitleEditText)){
                    Toast.makeText(addToFeedActivity.this, "Please input all fields", Toast.LENGTH_SHORT).show();
                }else if(!isValidDate()){
                    Toast.makeText(addToFeedActivity.this, "Please select an event date", Toast.LENGTH_SHORT).show();
                    dateTextView.setTextColor(Color.RED);
                }else if(userloc == null){
                    Toast.makeText(addToFeedActivity.this, "Please set a location", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(addToFeedActivity.this, "Location Set is " + userloc.latitude + " " + userloc.longitude, Toast.LENGTH_SHORT).show();
                    String user_id = mAuth.getCurrentUser().getUid();
                    int feedCount = feedsActivity.user.getFeedCount();
                    feedCount++;
                    DatabaseReference current_user_db = mDatabase.child(user_id);
                    DatabaseReference current_user_db_feed = current_user_db.child("Feeds").child("Feed" + feedCount);

                    current_user_db_feed.child("Title").setValue(eventTitleEditText.getText().toString().trim());
                    current_user_db_feed.child("eventDescription").setValue(eventDescriptionEditText.getText().toString().trim());
                    current_user_db_feed.child("clubName").setValue(clubNameSpinner.getSelectedItem().toString().trim());
                    current_user_db_feed.child("Date").setValue(dateTextView.getText().toString().trim());
                    current_user_db_feed.child("locationLatitude").setValue(userloc.latitude);
                    current_user_db_feed.child("locationLongitude").setValue(userloc.longitude);
                    current_user_db_feed.child("setBy").setValue(mAuth.getUid());
                    current_user_db.child("feedCount").setValue(feedCount);
                    Toast.makeText(addToFeedActivity.this, "Event Created", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day_of_month) {
        month+=1;
        mDay = day_of_month;
        mMonth = month;
        mYear = year;
        datestr = year+"-"+ month+"-"+ day_of_month;
        dateTextView.setText(day_of_month+" / " + month + " / " + year);
        dateTextView.setTextColor(Color.BLACK);

    }
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean isValidEdiText(EditText editText){
        if(editText.getText().toString().trim().isEmpty() ){
            return false;
        }
        return true;
    }

    private boolean isValidDate(){
        if(mDay == -1 || mMonth == -1 || mYear == -1){
            return false;
        }
        return true;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: Checking if services can be used");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(addToFeedActivity.this);
        if(available == ConnectionResult.SUCCESS) {
            //can make requests
            Log.d(TAG, "isServicesOK: Can make requests");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: fixable error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(addToFeedActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Log.d(TAG, "isServicesOK: can't make requests");
            Toast.makeText(this, "You can't make location requests, please try again later", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                }else{
                    getLocationPermission();
                }
        }
        switch(requestCode) {
            case REQUEST_LOCATION_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    double [] locdata = data.getDoubleArrayExtra("Location");
                    userloc = new LatLng(locdata[0], locdata[1]);

                    Log.d("HELLOHELLO", "Lat:" + userloc.latitude + " Long:" + userloc.longitude);
                } else {
                    Log.d("onActivityResult", "Activity cancelled");
                }
                break;
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions( addToFeedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;
                }
            }
        }
    }

}
