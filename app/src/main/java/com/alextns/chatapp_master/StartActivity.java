package com.alextns.chatapp_master;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class StartActivity extends AppCompatActivity {

    private Button mCreateAccountBtn;
    private Button mLoginAccountBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mCreateAccountBtn = findViewById(R.id.createAccountBtn);
        mLoginAccountBtn = findViewById(R.id.startLogin);

        mCreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAcc_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(createAcc_intent);

            }
        });

        mLoginAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginAcc_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginAcc_intent);
            }
        });
    }



}
