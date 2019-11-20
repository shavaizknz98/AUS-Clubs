package com.example.ausclubs;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class addToFeedActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView dateTextView;

    private EditText eventDescriptionEditText, eventClubNameEditText, eventTitleEditText;
    private Button uploadButton;
    private int mDay = -1, mMonth = -1, mYear = -1;
    private String datestr = "";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_feed);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        eventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);
        eventClubNameEditText = (EditText) findViewById(R.id.eventClubNameEditText);
        eventTitleEditText = (EditText) findViewById(R.id.eventTitleEditText);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        setTitle("Add A Club Event");
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
                if(!isValidEdiText(eventDescriptionEditText) || !isValidEdiText(eventClubNameEditText) || !isValidEdiText(eventTitleEditText)){
                    Toast.makeText(addToFeedActivity.this, "Please input all fields", Toast.LENGTH_SHORT).show();
                }else if(!isValidDate()){
                    Toast.makeText(addToFeedActivity.this, "Please select an event date", Toast.LENGTH_SHORT).show();
                    dateTextView.setTextColor(Color.RED);
                }else{
                    String user_id = mAuth.getCurrentUser().getUid();
                    int feedCount = feedsActivity.user.getFeedCount();
                    feedCount++;
                    DatabaseReference current_user_db = mDatabase.child(user_id);
                    DatabaseReference current_user_db_feed = current_user_db.child("Feeds").child("Feed" + feedCount);

                    current_user_db_feed.child("Title").setValue(eventTitleEditText.getText().toString().trim());
                    current_user_db_feed.child("eventDescription").setValue(eventDescriptionEditText.getText().toString().trim());
                    current_user_db_feed.child("clubName").setValue(eventClubNameEditText.getText().toString().trim());
                    current_user_db_feed.child("Date").setValue(dateTextView.getText().toString().trim());
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
}
