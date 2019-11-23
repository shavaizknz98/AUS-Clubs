package com.example.ausclubs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class eventInfo extends AppCompatActivity {
    /*
    This Activity is used to show the information about an event when the user clicks on it from the List View in the feedsActivity
     */


    private TextView eventDescriptionTextView, clubNameTextView, eventDateTextView, eventTitleTextView;//Text Views to hold the event information
    private Button showLocationButton;//TO open the event location
    private Double Lat, Long;//Holds Lat Long for the event
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        eventDescriptionTextView = (TextView) findViewById(R.id.eventDescriptionTextView);
        clubNameTextView = (TextView) findViewById(R.id.clubNameTextView);
        eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
        eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);
        showLocationButton = (Button) findViewById(R.id.showLocationButton);

        eventDescriptionTextView.setMovementMethod(new ScrollingMovementMethod()); //Make the event information scrollable since it can span many pages

        Intent intent = getIntent();

        //Retrieve event information from the intent
        String eventDescription = intent.getStringExtra("Description");
        String eventTitle = intent.getStringExtra("Title");
        Lat = intent.getDoubleExtra("Lat",0.0);
        Long = intent.getDoubleExtra("Long",0.0);
        eventDescriptionTextView.setText(eventDescription);
        eventTitleTextView.setText(eventTitle);
        clubNameTextView.setText(intent.getStringExtra("ClubName"));
        eventDateTextView.setText(intent.getStringExtra("Date"));

        //Add listener to the button to launch goolge maps with the map camera hovering over the event location
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:" + Lat + "," + Long); //Add Geo:Lat,Long to the URL for maps to make it hover over the set location
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);//Launch google maps
            }
        });

    }
}
