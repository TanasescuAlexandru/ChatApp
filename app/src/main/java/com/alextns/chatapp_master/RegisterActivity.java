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

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText mDisplayName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegisterAccountBtn;
    private FirebaseAuth mAuth;
    private String TAG=null;
    private Toolbar mToolbar;
    private ProgressDialog mProgressBar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Auth
        mAuth = FirebaseAuth.getInstance();

        //Android Fields from xlm file
        mDisplayName = findViewById(R.id.registerName);
        mEmail = findViewById(R.id.registerEmail);
        mPassword = findViewById(R.id.registerPass);
        mRegisterAccountBtn = findViewById(R.id.registerButton);
        mToolbar = findViewById(R.id.mainToolbar);

        //Toolbar set
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressBar = new ProgressDialog(this);

       //get click event from ui and get account details
        mRegisterAccountBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getText().toString() ;
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(!(TextUtils.isEmpty(displayName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))){
                    mProgressBar.setTitle("Registering User!");
                    mProgressBar.setMessage("Please wait while we create your account!");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();
                    registerUser(displayName,email,password);
                }
            }
        });
    }
    //register into Google Firebase with email and pass
    private void registerUser(final String displayName, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = Objects.requireNonNull(current_user).getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", displayName);
                            userMap.put("device_token", deviceToken);
                            userMap.put("status", "Hi there !");
                            userMap.put("image", "default_image");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mProgressBar.dismiss();
                                        TAG = "RegisterActivity success";
                                        // Sign in success, update UI with the signed-in user's information.
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent reg_intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        reg_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(reg_intent);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            mProgressBar.hide();
                            // If sign in fails, display a message to the user.
                            TAG = "RegisterActivity Fail";
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}
