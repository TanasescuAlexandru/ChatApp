package com.alextns.chatapp_master;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = null;
    private EditText mLoginEmail;
    private EditText mLoginPass;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressBar;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar mToolbar;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressBar = new ProgressDialog(this);

        mLoginEmail = findViewById(R.id.loginEmail);
        mLoginPass = findViewById(R.id.loginPassword);
        mLoginButton = findViewById(R.id.loginButton);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mLoginEmail.getText().toString();
                String pass = mLoginPass.getText().toString();
                if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass))){
                    mProgressBar.setTitle("Login User!");
                    mProgressBar.setMessage("Please wait while are verifying your credentials!");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();
                    logInUser(email,pass);
                }
            }
        });

    }

    private void logInUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mProgressBar.dismiss();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    mUsersDatabase.child(currentUserID).child("device_token").setValue(deviceToken);

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
                else{
                    mProgressBar.hide();
                    Toast.makeText(LoginActivity.this, "Cannot sign in. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }


}
