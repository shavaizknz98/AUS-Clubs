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

    private TextView eventDescriptionTextView, clubNameTextView, eventDateTextView, eventTitleTextView;
    private Button showLocationButton;
    private Double Lat, Long;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        eventDescriptionTextView = (TextView) findViewById(R.id.eventDescriptionTextView);
        clubNameTextView = (TextView) findViewById(R.id.clubNameTextView);
        eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
        eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);
        showLocationButton = (Button) findViewById(R.id.showLocationButton);

        eventDescriptionTextView.setMovementMethod(new ScrollingMovementMethod());
        Intent intent = getIntent();
        String Description = intent.getStringExtra("Description");

        String eventDescription = intent.getStringExtra("Description");
        String eventTitle = intent.getStringExtra("Title");
        Lat = intent.getDoubleExtra("Lat",0.0);
        Long = intent.getDoubleExtra("Long",0.0);
        eventDescriptionTextView.setText(eventDescription);
        eventTitleTextView.setText(eventTitle);
        clubNameTextView.setText(intent.getStringExtra("ClubName"));
        eventDateTextView.setText(intent.getStringExtra("Date"));

        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:" + Lat + "," + Long);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }
}
