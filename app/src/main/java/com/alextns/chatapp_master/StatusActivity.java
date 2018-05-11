package com.alextns.chatapp_master;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mStatus;
    private Button mSaveButton;
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        setContentView(R.layout.activity_status);
        mToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus = findViewById(R.id.currentUserStatus);
        mStatus.setText(status_value);
        mSaveButton = findViewById(R.id.saveStatusBtn);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show progress
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes !");
                mProgressDialog.setMessage("Please wait while we save the changes.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String status = mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgressDialog.dismiss();
                            finish();
                        }

                        else{
                            Toast.makeText(StatusActivity.this, "There was some error in Saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mCurrentUser!=null)
        ((ThotChat)this.getApplication()).startActivityTransitionTimer(mCurrentUser.getUid());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCurrentUser!=null)
            ((ThotChat)this.getApplication()).stopActivityTransitionTimer(mCurrentUser.getUid());
    }
}
