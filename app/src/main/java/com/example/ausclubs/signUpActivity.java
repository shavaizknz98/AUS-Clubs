package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class signUpActivity extends AppCompatActivity implements EditText.OnEditorActionListener{


    /*
    This is the sign up activity for if the user does not have an account, it will use Firebase Authentication to create the user
    and it will also allow Club Admins to declare which Clubs they are part of

     */


    private EditText emailAddressEditText;
    private EditText fullNameEditText;
    private EditText passwordEditText;
    private Switch isAdminSwitch;//Switch used to declare clubs
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private EditText clubName1;
    private EditText clubName2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailAddressEditText = (EditText) findViewById(R.id.emailEditText);
        emailAddressEditText.setOnEditorActionListener(this);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        passwordEditText.setOnEditorActionListener(this);
        fullNameEditText = (EditText) findViewById(R.id.fullNameEditText);
        fullNameEditText.setOnEditorActionListener(this);
        isAdminSwitch = (Switch) findViewById(R.id.switch1);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        clubName1 = (EditText) findViewById(R.id.firstClubEditText);
        clubName1.setOnEditorActionListener(this);
        clubName2 = (EditText) findViewById(R.id.secondClubEditText);
        clubName2.setOnEditorActionListener(this);
        clubName1.setVisibility(View.GONE);
        clubName2.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        isAdminSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {//IF the user selects that he is an admin, then show additional EditTexts to declare club names
                if (b) {
                    clubName1.setVisibility(View.VISIBLE);
                    clubName2.setVisibility(View.VISIBLE);
                }
                else {//Else hide the Edit text field
                    clubName1.setVisibility(View.GONE);
                    clubName2.setVisibility(View.GONE);
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");//Database reference to add User to the firebase database

        signUpButton.setOnClickListener(new View.OnClickListener() { //Listener for sing up button
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }


    public void signUp(){
        //First this will perform checks to see if the user has filled out all mandatory fields
        if(fullNameEditText.getText().toString().trim().isEmpty() || emailAddressEditText.getText().toString().trim().isEmpty() || passwordEditText.getText().toString().trim().isEmpty()){
            Toast.makeText(signUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            //This will create a user with the firebase Authenticator
            mAuth.createUserWithEmailAndPassword(emailAddressEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String user_id = mAuth.getCurrentUser().getUid();//Get User ID after creating user
                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        //Add user details to the Users Database
                        current_user_db.child("Name").setValue(fullNameEditText.getText().toString().trim());
                        current_user_db.child("Email").setValue(emailAddressEditText.getText().toString().trim());
                        current_user_db.child("Admin").setValue(isAdminSwitch.isChecked());
                        current_user_db.child("feedCount").setValue(0);//Sets the number of feeds the current user has uploaded, not used but can be for debug or analytical purposes
                        if (isAdminSwitch.isChecked()) {//AUS Regulations allow users to be Admins of a maximum of two clubs, so the addition of second club is optional, the field can be left blank
                            if (!clubName1.getText().toString().trim().isEmpty()) {
                                current_user_db.child("clubOne").setValue(clubName1.getText().toString().trim());
                                if (!clubName2.getText().toString().trim().isEmpty()) {
                                    current_user_db.child("clubTwo").setValue(clubName2.getText().toString().trim());
                                }
                            }

                        }
                        Toast.makeText(signUpActivity.this, "Please Sign In Again", Toast.LENGTH_SHORT).show();
                        Intent toFeedsActivity = new Intent(signUpActivity.this, MainActivity.class);
                        toFeedsActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(toFeedsActivity);//Proceeds to list of fields if the user is done signing up
                        finish();
                    }else{
                        Toast.makeText(signUpActivity.this, "Error creating user", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            signUp();//Proceed to sign up if the user presses the return key
        }
        return false;
    }
}
