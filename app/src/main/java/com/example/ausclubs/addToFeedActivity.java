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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



//The Below are constants used to request permissions to access the Users fine location using the GPS as well googles location services
import static com.example.ausclubs.Constants.ERROR_DIALOG_REQUEST;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class addToFeedActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = "addToFeedActivity";//For Debug
    private TextView dateTextView; //TextView will display the event date
    private EditText eventDescriptionEditText, eventTitleEditText; //For the event description and the event title
    private Spinner clubNameSpinner; //Spinner through which user can choose between clubs he/she is registered in
    private Button uploadButton;//Upload to database button
    private int mDay = -1, mMonth = -1, mYear = -1; //Initial values choosen from datePicker
    private FirebaseAuth mAuth; //This will be used to set the SetBy property of Feeds, the value assigned is the Firebase Authentication automatically assigned user ID
    private DatabaseReference mDatabase;//Refernce to FIrebase Database to Store new feed
    private DatabaseReference mFeedCount;//Reference to Firebase Database to Retrieve FeedCount and Set Feed ID
    public static final int REQUEST_LOCATION_CODE = 987; //Location Request Code
    private LatLng userloc; //Stores user location in a LatLng Class
    private boolean mLocationPermissionGranted = false; //Request Permission if not granted
    private Feed tempFeed; //tempFeed to store the feedCount
    private Button addLocationBtn;//Button that opens up the GoogleMaps Fragment to get the location the event is at


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

        //Initialization of firebase references to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFeedCount = FirebaseDatabase.getInstance().getReference();
        tempFeed = new Feed(); //Initialize tempFeed to store feedCount as ID
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<>();//Create a spinner and using an ArrayList add the two clubs the user has registered with (AUS Policy says maximum of two clubs per student)
        spinnerArray.add(Users.C1);
        if(Users.C2 != null){//Second club is optional, so its value may be set to null
            spinnerArray.add(Users.C2);
        }
        getLocationPermission(); //Request permission before mapbutton is clicked to avoid crashes on newer versions of android

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray); //Assign List to arrayadapter for use with spinner view

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.clubNameSpinner);
        sItems.setAdapter(adapter);//Set adapter


        setTitle("Add A Club Event");

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isServicesOK()) {
                    //need to return coordinates
                    Intent gotoMapActivity = new Intent(addToFeedActivity.this, MapsActivity_getUserLocation.class);//Start Intent to the maps fragment, to choose event location
                    startActivityForResult(gotoMapActivity, REQUEST_LOCATION_CODE);//Since the activity will return a LatLng, it needs to be launched with StartActivityForResult
                }
            }
        });
        dateTextView.setOnClickListener(new View.OnClickListener() {//This listener will launch a date picker view to pick the date the event is on
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hide Keyboard when upload clicked
                hideKeyboard(addToFeedActivity.this);

                //Checks for valid strings
                if(!isValidEdiText(eventDescriptionEditText)  || !isValidEdiText(eventTitleEditText)){
                    Toast.makeText(addToFeedActivity.this, "Please input all fields", Toast.LENGTH_SHORT).show();
                }else if(!isValidDate()){
                    Toast.makeText(addToFeedActivity.this, "Please select an event date", Toast.LENGTH_SHORT).show();
                    dateTextView.setTextColor(Color.RED);
                }else if(userloc == null){//Check to see if location has been set for the event
                    Toast.makeText(addToFeedActivity.this, "Please set a location", Toast.LENGTH_SHORT).show();
                }else{
                    final Feed tempFeed = new Feed();//Tempt feed to store feedCount to be used to update feedCount once feed has been added
                    mFeedCount.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            tempFeed.setFeedCount((int) (long) dataSnapshot.child("feedCount").getValue());  //Retrieve feedcount
                            /*

                            The lines of code below will add in properties of the event under in the firebase database,
                            The properties added are:
                            * Title
                            * Hidden (TO be used to delete events)
                            * Event Description
                            * Event ID
                            * Name of the Club Setting the Event
                            * Date of Event
                            * Latitude and Longitude coordinates of the event


                             */
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("Title").setValue(eventTitleEditText.getText().toString().trim());
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("Hidden").setValue(false);
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("ID").setValue(tempFeed.getFeedCount());

                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("eventDescription").setValue(eventDescriptionEditText.getText().toString().trim());
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("clubName").setValue(clubNameSpinner.getSelectedItem().toString().trim());
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("Date").setValue(dateTextView.getText().toString().trim());
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("locationLatitude").setValue(userloc.latitude);
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("locationLongitude").setValue(userloc.longitude);
                            mDatabase.child("Feed").child("Feed"+(tempFeed.getFeedCount())).child("setBy").setValue(mAuth.getUid());
                            tempFeed.getFeedCount();
                            //Once the Feed has been added, update the count of the feed
                            mFeedCount.child("feedCount").setValue((tempFeed.getFeedCount() + 1));
                            Toast.makeText(addToFeedActivity.this, "Event Created", Toast.LENGTH_SHORT).show();
                            finish();//Exit the activity when done
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }
        });

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day_of_month) {//Date picker interface implementation for when the user is done choosing the date
        month+=1;
        //Assign month date and year to the member variables
        mDay = day_of_month;
        mMonth = month;
        mYear = year;
        dateTextView.setText(day_of_month+" / " + month + " / " + year); //Format is Day/Month/year as dd/mm/yyyy
        dateTextView.setTextColor(Color.BLACK);

    }
    private void showDatePickerDialog() { //This will launch the date picker dialog with the current year month and date pre selected
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); //This is to make sure the user cannot choose a location in the past
        datePickerDialog.show();
    }

    private boolean isValidEdiText(EditText editText){//Used to check is an editText View contains a valid string for upload
        if(editText.getText().toString().trim().isEmpty() ){
            return false;
        }
        return true;
    }

    private boolean isValidDate(){//Check if date selected is valid, by checking if the user has selected a date
        if(mDay == -1 || mMonth == -1 || mYear == -1){
            return false;
        }
        return true;
    }

    public static void hideKeyboard(Activity activity) {//To be used to hide the keyboard
        //This code has been retrieved from our software engineering project
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean isServicesOK(){
        //Making sure if google play services are available to be used
        // for when the maps api is being used for Places Search API (explain in map fragment activity)
        Log.d(TAG, "isServicesOK: Checking if services can be used");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(addToFeedActivity.this);
        if(available == ConnectionResult.SUCCESS) {
            //can make requests
            Log.d(TAG, "isServicesOK: Can make requests");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {//Show a dialog if the user does not have google play services available
            Log.d(TAG, "isServicesOK: fixable error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(addToFeedActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Log.d(TAG, "isServicesOK: can't make requests");
            //If nothing works, no location can be set
            Toast.makeText(this, "You can't make location requests, please try again later", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //TO be used for when the app will come back to this activity
        // after retrieving location results from maps fragment
        super.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                }else{
                    getLocationPermission();//If coming for location permission, get user location
                }
                break;
            case REQUEST_LOCATION_CODE://Coming back with user selected location
                if(resultCode == Activity.RESULT_OK) {
                    double [] locdata = data.getDoubleArrayExtra("Location"); //Information is retrieved as an array with Lat at index 0 and Long and index 1
                    userloc = new LatLng(locdata[0], locdata[1]); //Assign Lat Lng

                    Log.d("HELLOHELLO", "Lat:" + userloc.latitude + " Long:" + userloc.longitude);
                } else {
                    Log.d("onActivityResult", "Activity cancelled");
                }
                break;
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {//If permission has been granted through manifest, then permission checks are valid
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions( addToFeedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);//Request permission by asking the OS to request with the code for fine location
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //For when the location request has been granted and this activity is now in focus again.
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;//IF the user selects Yes or Grants FIne Loation for services, we can set Location Permission Granted to true
                }
            }
        }
    }

}
