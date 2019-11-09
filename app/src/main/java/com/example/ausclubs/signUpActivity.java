package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class signUpActivity extends AppCompatActivity {


    private EditText emailAddressEditText;
    private EditText fullNameEditText;
    private EditText passwordEditText;
    private Switch isAdminSwitch;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailAddressEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        fullNameEditText = (EditText) findViewById(R.id.fullNameEditText);
        isAdminSwitch = (Switch) findViewById(R.id.switch1);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fullNameEditText.getText().toString().trim().isEmpty() || emailAddressEditText.getText().toString().trim().isEmpty() || passwordEditText.getText().toString().trim().isEmpty()){
                    Toast.makeText(signUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(emailAddressEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = mDatabase.child(user_id);

                                current_user_db.child("Name").setValue(fullNameEditText.getText().toString().trim());
                                current_user_db.child("Email").setValue(emailAddressEditText.getText().toString().trim());
                                current_user_db.child("Admin").setValue(isAdminSwitch.isChecked());
                                Toast.makeText(signUpActivity.this, "Welcome to the AUS Clubs App", Toast.LENGTH_SHORT).show();
                                Intent toFeedsActivity = new Intent(signUpActivity.this, feedsActivity.class);
                                startActivity(toFeedsActivity);
                            }else{
                                Toast.makeText(signUpActivity.this, "Error creating user", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }
}
