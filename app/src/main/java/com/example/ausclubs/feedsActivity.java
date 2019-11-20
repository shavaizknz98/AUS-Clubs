package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class feedsActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener {


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
                    long feedCount = (long) ds.child("feedCount").getValue();
                    for(int i=1; i <= feedCount; i++){
                        tempFeed = new Feed();
                        tempFeed.setTitle(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getTitle());
                        tempFeed.setDate(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getDate());
                        tempFeed.setClubName(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getClubName());
                        tempFeed.setEventDescription(ds.child("Feeds").child("Feed"+i).getValue(Feed.class).getEventDescription());
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
}
