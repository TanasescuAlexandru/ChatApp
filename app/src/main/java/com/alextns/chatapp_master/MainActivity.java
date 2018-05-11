package com.alextns.chatapp_master;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //local declaration
        Toolbar mMainToolbar;
        ViewPager mViewPager;
        SectionsPagerAdapter mSectionsPagerAdapter;
        TabLayout mTabLayout;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
        mMainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Thot Chat");
        //tabs
        mViewPager = findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);



    }

    @Override
    protected void onStart() {
        super.onStart();

         mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser == null) {
            sendToStart();
            }else{
            mCurrentUserID = mCurrentUser.getUid();
        }
    }


    private void sendToStart(){
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();        }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCurrentUserID!=null)
        ((ThotChat)this.getApplication()).startActivityTransitionTimer(mCurrentUserID);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCurrentUserID!=null)
        ((ThotChat)this.getApplication()).stopActivityTransitionTimer(mCurrentUserID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mainLogOutButton) {
          FirebaseAuth.getInstance().signOut();
          sendToStart();

        }
        if (item.getItemId() == R.id.accountSettingsBtn) {
            Intent settingsIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (item.getItemId() == R.id.allUsersMenuBtn) {
            Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);


    }



}
