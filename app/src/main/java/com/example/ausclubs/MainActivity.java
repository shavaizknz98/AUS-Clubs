package com.example.ausclubs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private Button signUpButton;
    private Button signInButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpButton = (Button) findViewById(R.id.signUpButton);
        signInButton = (Button) findViewById(R.id.logInButton);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        final Intent toFeedsActivity = new Intent(MainActivity.this, feedsActivity.class);
        mAuth = FirebaseAuth.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(toFeedsActivity);
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
signIn();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toSignUp = new Intent(MainActivity.this, signUpActivity.class);
                startActivity(toSignUp);
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            signIn();//match this behavior to your 'Send' (or Confirm) button
        }
        return false;
    }

    private void signIn(){
        if(emailEditText.getText().toString().trim().isEmpty() || passwordEditText.getText().toString().trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        final Intent toFeedsActivity = new Intent(MainActivity.this, feedsActivity.class);
                        startActivity(toFeedsActivity);
                    }else{
                        Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
}
