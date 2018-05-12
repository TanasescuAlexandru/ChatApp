package com.alextns.chatapp_master;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author alexandru.tanasescu on 06/05/2018.
 */
public class ThotChat extends Application{
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;




    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Picasso image offline

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUsersDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }





    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;



    //schedule a task witch is supposed to start in 2 seconds if the timer reach the limit(2 seconds)
    //task that will set wasInBackground value to true and user online value to Server timestamp
    //this timer limit should be reached only when the user leaves the app.
    //added in onPause activity methods
    public void startActivityTransitionTimer(final String mCurrentUserID) {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {

                ThotChat.this.wasInBackground = true;


                DatabaseReference mOnlineUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);
                mOnlineUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }
    //stops the time when an activity resume/starts(added in onResume activity method)
    public void stopActivityTransitionTimer(String mCurrentUserID) {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }
        DatabaseReference mOnlineUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);
        mOnlineUsersDatabase.child("online").setValue(true);
        this.wasInBackground = false;

    }




}
