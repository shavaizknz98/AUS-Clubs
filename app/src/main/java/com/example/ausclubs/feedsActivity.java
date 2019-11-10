package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class feedsActivity extends AppCompatActivity {


    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    public static Users user;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving user data");
        progressDialog.setCancelable(false);
        progressDialog.show();

         mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = new Users();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    user.setEmail(ds.child(mAuth.getUid()).getValue(Users.class).getEmail());
                    user.setName(ds.child(mAuth.getUid()).getValue(Users.class).getName());
                    user.setAdmin(ds.child(mAuth.getUid()).getValue(Users.class).isAdmin());
                    user.setFeedCount(ds.child(mAuth.getUid()).getValue(Users.class).getFeedCount());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(feedsActivity.this, "Error Retrieving Details, Please Sign Out and Try Again" + user.isAdmin(), Toast.LENGTH_SHORT).show();


            }
        });



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.isAdmin()){
                    Intent toAddFeed = new Intent(feedsActivity.this, addToFeedActivity.class);
                    startActivity(toAddFeed);
                }
            }
        });
    }





}
