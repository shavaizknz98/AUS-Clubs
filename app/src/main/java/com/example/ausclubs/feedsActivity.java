package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.example.ausclubs.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class feedsActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mFeed;
    public static Users user;
    private ListView eventsListView;
    private ProgressDialog progressDialog;
    private Feeds mainFeed;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogOut:
                mAuth.signOut();
                Intent toMainActivity = new Intent(this, MainActivity.class);
                startActivity(toMainActivity);
                finish();
            case R.id.menuRefreshBtn:
                mFeed = null;
                mFeed = FirebaseDatabase.getInstance().getReference().child("Users");
                mFeed.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mainFeed.emptyFeed();

                        Feed tempFeed = new Feed();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            long feedCount =0;
                            try{
                                feedCount = (long) ds.child("feedCount").getValue();
                            }catch (NullPointerException e){

                            }
                            for(int i=1; i <= feedCount; i++){
                                tempFeed = new Feed();
                                tempFeed.setTitle(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getTitle());
                                tempFeed.setDate(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getDate());
                                tempFeed.setClubName(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getClubName());
                                tempFeed.setEventDescription(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getEventDescription());
                                tempFeed.setLocationLatitude(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getLocationLatitude());
                                tempFeed.setLocationLongitude(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getLocationLongitude());


                                mainFeed.addToFeedList(tempFeed); //adding feed to mainfeed
                            }
                        }
                        progressDialog.dismiss();
                        updateDisplay();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        eventsListView = (ListView) findViewById(R.id.eventsListView);
        eventsListView.setOnItemClickListener(this);
        eventsListView.setOnItemLongClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving user data");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mainFeed = new Feeds();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFeed = FirebaseDatabase.getInstance().getReference().child("Users");

        mFeed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mainFeed.emptyFeed();

                Feed tempFeed = new Feed();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    long feedCount =0;
                    try{
                        feedCount = (long) ds.child("feedCount").getValue();
                    }catch (NullPointerException e){

                    }
                    for(int i=1; i <= feedCount; i++){
                        tempFeed = new Feed();
                        tempFeed.setTitle(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getTitle());
                        tempFeed.setDate(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getDate());
                        tempFeed.setClubName(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getClubName());
                        tempFeed.setSetBy(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getSetBy());
                        tempFeed.setEventDescription(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getEventDescription());
                        tempFeed.setLocationLatitude(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getLocationLatitude());
                        tempFeed.setLocationLongitude(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getLocationLongitude());

                        mainFeed.addToFeedList(tempFeed); //adding feed to mainfeed
                    }
                }
                progressDialog.dismiss();
                updateDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = new Users();

                Feed tempFeed = new Feed();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    user.setEmail(ds.child(mAuth.getUid()).getValue(Users.class).getEmail());
                    user.setName(ds.child(mAuth.getUid()).getValue(Users.class).getName());
                    user.setAdmin(ds.child(mAuth.getUid()).getValue(Users.class).isAdmin());
                    user.setClubOne(ds.child(mAuth.getUid()).getValue(Users.class).getClubOne());
                    try {
                        user.setClubTwo(ds.child(mAuth.getUid()).getValue(Users.class).getClubTwo());
                    }catch (Exception e) {

                    }
                    user.setFeedCount(ds.child(mAuth.getUid()).getValue(Users.class).getFeedCount());
                }
                if (!user.isAdmin()) {
                    fab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(feedsActivity.this, "Error Retrieving Details, Please Sign Out and Try Again" + user.isAdmin(), Toast.LENGTH_SHORT).show();


            }
        });

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This App requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGPSIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.isAdmin()) {
                    Intent toAddFeed = new Intent(feedsActivity.this, addToFeedActivity.class);
                    startActivity(toAddFeed);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Feed clickedOnFeed = mainFeed.getFeedList().get(position);

        Intent intentToEvent = new Intent(this, eventInfo.class);

        intentToEvent.putExtra("ClubName", clickedOnFeed.getClubName());
        intentToEvent.putExtra("Title", clickedOnFeed.getTitle());
        intentToEvent.putExtra("Date", clickedOnFeed.getDate());
        intentToEvent.putExtra("Description", clickedOnFeed.getEventDescription());
        intentToEvent.putExtra("Lat", clickedOnFeed.getLocationLatitude());
        intentToEvent.putExtra("Long", clickedOnFeed.getLocationLongitude());

        startActivity(intentToEvent);


    }



    public void updateDisplay() {
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Feed item : mainFeed.getFeedList()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("date", item.getDate());
            map.put("title", item.getTitle());
            map.put("clubName", item.getClubName());

            data.add(map);
        }

        // create the resource, from, and to variables
        int resource = R.layout.listview_event;
        String[] from = {"date", "title", "clubName"};
        int[] to = {R.id.eventDateTextView, R.id.eventTitleTextView, R.id.clubNameTextView};

        // create and set the adapter
        SimpleAdapter adapter =
                new SimpleAdapter(this, data, resource, from, to);
        eventsListView.setAdapter(adapter);


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        Feed clickedOnFeed = mainFeed.getFeedList().get(position);

        if(clickedOnFeed.getSetBy() == mAuth.getCurrentUser().getUid()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to delete this event?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            return;
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }



        return true;
    }
}
