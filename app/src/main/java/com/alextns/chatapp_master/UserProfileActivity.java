package com.alextns.chatapp_master;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/*

Users relationship @current_state
    current_state == 0 -> Not Friends
    current_state == 1 -> Request sent
    current_state == 2 -> Request recived
    current_state == 3 -> Friends
 */
public class UserProfileActivity extends AppCompatActivity {

    private DatabaseReference mUsersDatabase;
    private Button mSendRequestBtn;
    private Button mDeclineFriendRequestBtn;
    private Toolbar mMainToolbar;
    private TextView mUserProfileName , mUserProfileStatus;
    private ImageView mUserImage;
    private ProgressDialog mProgressDialog;
    private int current_state;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);
        final String user_id = getIntent().getStringExtra("user_id");
        int recivedRequestState = getIntent().getIntExtra("recivedRequestState", 0);



        mMainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Thot Chat");
        //get DB user profile details and keep data synced offline
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //link ui parts
        mUserProfileName = findViewById(R.id.userProfileName);
        mUserProfileStatus = findViewById(R.id.userProfileStatus);
        mUserImage = findViewById(R.id.userProfileImage);
        mSendRequestBtn = findViewById(R.id.sendRequestBtn);
        mDeclineFriendRequestBtn = findViewById(R.id.declineFriendRequestBtn);
        mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
        //default current state = 0 (NOT FRIENDS)
        current_state = recivedRequestState;
        mProgressDialog = new ProgressDialog(UserProfileActivity.this);
        mProgressDialog.setTitle("Loading User Data !");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        //set values from DB
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String displayStatus = dataSnapshot.child("status").getValue().toString();
                final String displayImage = dataSnapshot.child("image").getValue().toString();
                mUserProfileName.setText(displayName);
                mUserProfileStatus.setText(displayStatus);

                Picasso.get().load(displayImage).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop()
                        .placeholder(R.drawable.default_avatar).into(mUserImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(displayImage).fit().centerCrop().placeholder(R.drawable.default_avatar).into(mUserImage);
                            }
                        });




                //Friends request
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();


                            if (Integer.parseInt(req_type) == 1) {
                                current_state = 1;
                                mSendRequestBtn.setText("Cancel Friend Request");


                            } else if (Integer.parseInt(req_type) == 2) {
                                mDeclineFriendRequestBtn.setVisibility(View.VISIBLE);
                                current_state = 2;
                                mSendRequestBtn.setText("Accept Friend Request");
                            }
                            mProgressDialog.dismiss();


                        }
                        else {

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = 3;
                                        mSendRequestBtn.setText("Unfriend this person");
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                            mProgressDialog.dismiss();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressDialog.dismiss();

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();

            }
        });

        mDeclineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this is visible only for user1 when he received a friend request from user2. I will check it anyway
                if (current_state == 2) {
                    Map friendsRequestMap = new HashMap();
                    friendsRequestMap.put("Friends_request/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsRequestMap.put("Friends_request/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(friendsRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){
                                mSendRequestBtn.setEnabled(true);
                                current_state = 0;
                                mSendRequestBtn.setText("Send Friend Request");
                                mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                            }else {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });


                }
            }
        });

        mSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendRequestBtn.setEnabled(false);

                // ------NOT FRIENDS BEHAVIOUR------
                if (current_state == 0) {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationID = newNotificationRef.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type","1");

                    Map requestMap = new HashMap();
                    requestMap.put("Friends_request/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", 1);
                    requestMap.put("Friends_request/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", 2);
                    requestMap.put("notifications/" + user_id + "/"  + newNotificationID ,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mSendRequestBtn.setEnabled(true);
                                current_state = 1;
                                mSendRequestBtn.setText("Cancel Friend Request");
                            }else {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //------CANCEL FRIEND REQUEST BEHAVIOUR-----
                if (current_state == 1) {

                    Map friendsRequestMap = new HashMap();
                    friendsRequestMap.put("Friends_request/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsRequestMap.put("Friends_request/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(friendsRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mSendRequestBtn.setEnabled(true);
                                current_state = 0;
                                mSendRequestBtn.setText("Send Friend Request");
                            }else {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

                //-----ACCEPT FRIEND REQUEST-----

                if (current_state == 2) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friends_request/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsMap.put("Friends_request/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){
                                mSendRequestBtn.setEnabled(true);
                                current_state = 3;
                                mSendRequestBtn.setText("Unfriend this person");
                                mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);

                            }
                            else {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }
                    //Current state is Friends ----- Behaviour when user is clicking Unfriend
                if (current_state == 3) {

                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id , null);
                    unFriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null){
                                        mSendRequestBtn.setEnabled(true);
                                        current_state = 0;
                                        mSendRequestBtn.setText("Send Friend Request");

                                    }
                                    else {
                                        Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                }

            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        ((ThotChat)this.getApplication()).startActivityTransitionTimer(mCurrentUser.getUid());


    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ThotChat)this.getApplication()).stopActivityTransitionTimer(mCurrentUser.getUid());
    }
}
