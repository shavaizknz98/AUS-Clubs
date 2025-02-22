package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    /*

    This Class is the LogIn Page and also the first activity to be loaded

     */

    private Button signUpButton;
    private Button signInButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;//Set Progress Dialog for loading when confirming user credentials with Firebase Authentication
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpButton = (Button) findViewById(R.id.signUpButton);
        signInButton = (Button) findViewById(R.id.logInButton);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        final Intent toFeedsActivity = new Intent(MainActivity.this, feedsActivity.class); //Set intent to the feedsActivity
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging In...");
        progressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        emailEditText.setOnEditorActionListener(this);//Used so the the soft keyboard can be hidden as soon as log in button is clicked, as well ass log in if the user clicks on the enter key
        passwordEditText.setOnEditorActionListener(this);

        emailEditText.setText("shavaizknz98@gmail.com");
        passwordEditText.setText("Becooler98");

        signInButton.setOnClickListener(new View.OnClickListener() {// Call Sign in function if sign in button pressed
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {//Proceed to sing up page if user has no account
            @Override
            public void onClick(View view) {
                hideKeyboard(MainActivity.this);//Self explanatory
                Intent toSignUp = new Intent(MainActivity.this, signUpActivity.class);
                startActivity(toSignUp);
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
    || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            signIn();//match this behavior to your 'Send' (or Confirm) button
        }
        return false;
    }

    private void signIn(){
        progressDialog.show();
        hideKeyboard(this);
        if(emailEditText.getText().toString().trim().isEmpty() || passwordEditText.getText().toString().trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }else{//Use firebase Authenticator to sign in with email and password if the account exists, else then display a toast informing the user of the error
            mAuth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        final Intent toFeedsActivity = new Intent(MainActivity.this, feedsActivity.class);
                        startActivity(toFeedsActivity);
                        finish();
                        progressDialog.dismiss();
                    }else{
                        progressDialog.dismiss();//Display error Toast to string if no user has been made.
                        Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
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
