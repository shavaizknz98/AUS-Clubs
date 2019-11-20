package com.example.ausclubs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class eventInfo extends AppCompatActivity {

    private TextView eventDescriptionTextView, clubNameTextView, eventDateTextView, eventTitleTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        eventDescriptionTextView = (TextView) findViewById(R.id.eventDescriptionTextView);
        clubNameTextView = (TextView) findViewById(R.id.clubNameTextView);
        eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
        eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);

        eventDescriptionTextView.setMovementMethod(new ScrollingMovementMethod());
        Intent intent = getIntent();
        String Description = intent.getStringExtra("Description");

        eventDescriptionTextView.setText(intent.getStringExtra("Description"));
        eventTitleTextView.setText(intent.getStringExtra("Title"));
        clubNameTextView.setText(intent.getStringExtra("ClubName"));
        eventDateTextView.setText(intent.getStringExtra("Date"));

    }
}
