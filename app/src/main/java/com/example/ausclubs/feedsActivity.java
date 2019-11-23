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
import android.widget.EditText;
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

public class feedsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    /*

    The purpose of this activity is to show the user a list of feeds retrieved from the database, the user can click on each item and show
    the event information in detail.
    If the user is also an Admin (CLub Admin) he/she can also add a new feed otherwise the user can just view events and search events

     */

    private FloatingActionButton fab; //Floating Button which is only enabled for club admins, used to add to a feed
    private FirebaseAuth mAuth; // Firebase Authentication Reference
    private DatabaseReference mDatabase; //Database Reference
    private DatabaseReference mFeed; // Feed Reference in Database
    public static Users user; //Model User class
    private ListView eventsListView; //ListView to be populated with events
    private ProgressDialog progressDialog; //Progress dialog to show loading while list is being populated
    private Feeds mainFeed; // Feed containing list of events to be used to populate
    private DatabaseReference mFeedCount; //Reference to the overall feed count in the database
    private int FeedCount = 0; //Value holding the feed count


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//Instantiate the options menu
        getMenuInflater().inflate(R.menu.menu, menu);//Inflate menu from res.menu xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//This will perform actions for each of the buttons on the menu
        switch (item.getItemId()) {
            case R.id.menuLogOut://Log out button
                mAuth.signOut();//Sign out user from firebase Authentication
                Intent toMainActivity = new Intent(this, MainActivity.class);//Proceed back to the MainActivity which is the login page
                startActivity(toMainActivity);
                finish();//Close Activity once the activity has launched, so that the user cannot press the back button to return to this
                break;
            case R.id.menuRefreshBtn://Refresh button to reload the list
                mFeed = null;//This is done to remove all the listeners attached to the database reference
                mFeed = FirebaseDatabase.getInstance().getReference().child("Feed"); //Re-instantiate the database reference
                mFeedCount = null; //This is done to remove all the listeners attached to the database feedCount reference
                mFeedCount = FirebaseDatabase.getInstance().getReference();//Re-instantiate the database reference
                mFeedCount.addValueEventListener(new ValueEventListener() {//Listener for feedCount
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        FeedCount = ((int) (long) dataSnapshot.child("feedCount").getValue()); //retrieve the feed count
                        mFeed.addValueEventListener(new ValueEventListener() {//Listener for the feeds themselves
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                mainFeed.emptyFeed();

                                Feed tempFeed;
                                for (int i = 0; i < FeedCount; i++) {
                                    tempFeed = new Feed();
                                    tempFeed.setHidden((boolean) dataSnapshot2.child("Feed" + i).child("Hidden").getValue());
                                    if (!tempFeed.isHidden()) {//Add Feed to list from dataabase only if it is not hidden (equivalent of deleted for the user)
                                        tempFeed.setID((int) (long) dataSnapshot2.child("Feed" + i).child("ID").getValue());//Feed ID to be used for onClickItem and onLongClickItem
                                        tempFeed.setTitle((String) dataSnapshot2.child("Feed" + i).child("Title").getValue());//The rest are the event information
                                        tempFeed.setDate((String) dataSnapshot2.child("Feed" + i).child("Date").getValue());
                                        tempFeed.setClubName((String) dataSnapshot2.child("Feed" + i).child("clubName").getValue());
                                        tempFeed.setSetBy((String) dataSnapshot2.child("Feed" + i).child("setBy").getValue());
                                        tempFeed.setEventDescription((String) dataSnapshot2.child("Feed" + i).child("eventDescription").getValue());
                                        tempFeed.setLocationLatitude((Double) dataSnapshot2.child("Feed" + i).child("locationLatitude").getValue());
                                        tempFeed.setLocationLongitude((Double) dataSnapshot2.child("Feed" + i).child("locationLongitude").getValue());

                                        mainFeed.addToFeedList(tempFeed); //adding feed to mainfeed
                                    }
                                }

                                progressDialog.dismiss(); //Remove loading dialog
                                updateDisplay();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { //Implementation for if database error occurred, usually for if Firebase servers are down

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.menuSearch:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);//Show an alert dialog displaying an EditText for users to search through events

                alert.setTitle("Events Search");
                alert.setMessage("Type in the box below to search for events");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonID) {
                        if (!input.getText().toString().toLowerCase().trim().equals("")) {
                            ArrayList<Feed> tempFeedList = new ArrayList<>();
                            for (Feed item : mainFeed.getFeedList()) {
                                if (item.getTitle().toLowerCase().contains(input.getText().toString().toLowerCase().trim())) {
                                    tempFeedList.add(item);//Only include feedList Feeds that contain events with matching content from the search box
                                }
                            }
                            mainFeed.setFeedList(tempFeedList);
                            updateDisplay();//Update the list adapter at the end of the search
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing on cancel
                    }
                });

                alert.show(); //Show alert when button clicked
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        //Instantiate Views from resources
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        eventsListView = (ListView) findViewById(R.id.eventsListView);

        eventsListView.setOnItemClickListener(this);//Set item click listener
        eventsListView.setOnItemLongClickListener(this); //For long item click


        progressDialog = new ProgressDialog(this); //Instantiate progressDialog
        progressDialog.setMessage("Retrieving user data..."); //Message for progressDialog
        progressDialog.setCancelable(false);//Make sure the user cannot cancel the loading dialog
        progressDialog.show();//Show dialog until the all data has been loaded
        mainFeed = new Feeds(); //Instiate main list of feeds
        mAuth = FirebaseAuth.getInstance();//Instantiate Firebase Authentication
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users"); //Instantiate database reference for users
        mFeed = FirebaseDatabase.getInstance().getReference().child("Feed"); //Instantie database reference for feeds
        mFeedCount = FirebaseDatabase.getInstance().getReference();// Instantiate databse reference for feedCount
        mFeedCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FeedCount = ((int) (long) dataSnapshot.child("feedCount").getValue());
                mFeed.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                        mainFeed.emptyFeed();//Empty out feeds since this is an async listener and can be called anytime the database is updated
                        Feed tempFeed;
                        for (int i = 0; i < FeedCount; i++) {
                            tempFeed = new Feed();//New Feed Reference
                            tempFeed.setHidden((boolean) dataSnapshot2.child("Feed" + i).child("Hidden").getValue()); //If feed has been deleted (internally seen as hidden) do not display the list
                            //The reason there is no actual delete since my feeds are accesed using keys such as "feed1", "feed2" etc. if We remove one then the order is incorrect
                            //and this will lead to crashes, therefore it is faster to hide it and the user will not see the program as any different
                            if (!tempFeed.isHidden()) {
                                //Retrieve feed information when loading from database
                                tempFeed.setID((int) (long) dataSnapshot2.child("Feed" + i).child("ID").getValue());
                                tempFeed.setTitle((String) dataSnapshot2.child("Feed" + i).child("Title").getValue());
                                tempFeed.setDate((String) dataSnapshot2.child("Feed" + i).child("Date").getValue());
                                tempFeed.setClubName((String) dataSnapshot2.child("Feed" + i).child("clubName").getValue());
                                tempFeed.setSetBy((String) dataSnapshot2.child("Feed" + i).child("setBy").getValue());
                                tempFeed.setEventDescription((String) dataSnapshot2.child("Feed" + i).child("eventDescription").getValue());
                                tempFeed.setLocationLatitude((Double) dataSnapshot2.child("Feed" + i).child("locationLatitude").getValue());
                                tempFeed.setLocationLongitude((Double) dataSnapshot2.child("Feed" + i).child("locationLongitude").getValue());

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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        String user_id = mAuth.getCurrentUser().getUid();//user ID from firebase Authentication
        DatabaseReference current_user_db = mDatabase.child(user_id);
        current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {//Listener to retrieve user information
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = new Users();
                //Load user data and set it
                user.setEmail((String) dataSnapshot.child("Email").getValue());
                user.setName((String) dataSnapshot.child("Name").getValue());
                user.setAdmin((boolean) dataSnapshot.child("Admin").getValue());
                user.setClubOne((String) dataSnapshot.child("clubOne").getValue());
                try {
                    user.setClubTwo((String) dataSnapshot.child("clubTwo").getValue());//Second club is optional, if null found then do not add
                } catch (Exception e) {

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

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //Location manager to see if location services enabled

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This App requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//Intent to settings menu on the location services page
                            startActivityForResult(enableGPSIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        fab.setOnClickListener(new View.OnClickListener() {//Set floating action button listener
            @Override
            public void onClick(View view) {
                if (user.isAdmin()) {//Button is only enabled for those who are admins
                    Intent toAddFeed = new Intent(feedsActivity.this, addToFeedActivity.class);//Proceed to Add to Feed List Activity
                    startActivity(toAddFeed);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {//Listener for if the user chooses open an event in full detail
        Feed clickedOnFeed = mainFeed.getFeedList().get(position);

        Intent intentToEvent = new Intent(this, eventInfo.class);
        //Event details are passed through the intent
        intentToEvent.putExtra("ClubName", clickedOnFeed.getClubName());
        intentToEvent.putExtra("Title", clickedOnFeed.getTitle());
        intentToEvent.putExtra("Date", clickedOnFeed.getDate());
        intentToEvent.putExtra("Description", clickedOnFeed.getEventDescription());
        intentToEvent.putExtra("Lat", clickedOnFeed.getLocationLatitude());
        intentToEvent.putExtra("Long", clickedOnFeed.getLocationLongitude());

        startActivity(intentToEvent);


    }


    public void updateDisplay() {//Update Display will reload the list
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Feed item : mainFeed.getFeedList()) {
            if (!item.isHidden()) {//Only add item (Feed) if it is not deleted
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("date", item.getDate());
                map.put("title", item.getTitle());
                map.put("clubName", item.getClubName());
                data.add(map);
            }

        }

        // create the resource, from, and to variables
        int resource = R.layout.listview_event;
        String[] from = {"date", "title", "clubName"}; //From to to populate cell views with the data from the hashmap
        int[] to = {R.id.eventDateTextView, R.id.eventTitleTextView, R.id.clubNameTextView};


        // create and set the adapter
        SimpleAdapter adapter =
                new SimpleAdapter(this, data, resource, from, to);

        eventsListView.setAdapter(adapter);//Set adapter


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
        Feed clickedOnFeed = mainFeed.getFeedList().get(position);
        final DatabaseReference mReferenceToDelete = FirebaseDatabase.getInstance().getReference();

        for (final Feed item : mainFeed.getFeedList()) {
            if (item.getID() == clickedOnFeed.getID()) {//On long click will only open the deletion menu if the item is added by the current user
                if (clickedOnFeed.getSetBy().toString().trim().equals(mAuth.getCurrentUser().getUid().toString().trim())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);//Show alert for deletion confirmation
                    builder.setMessage("Do you want to delete this event?")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mReferenceToDelete.child("Feed").child("Feed" + item.getID()).child("Hidden").setValue(true);//Set Hidden boolen which is the equivalent of delete
                                    return;
                                }
                            })
                            .setNegativeButton("No", null);
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }


        return true;
    }
}
