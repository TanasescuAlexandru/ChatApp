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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
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
    private FirebaseUser mCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);
        final String user_id = getIntent().getStringExtra("user_id");

        mMainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Thot Chat");
        //get DB user profile details
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        //link ui parts
        mUserProfileName = findViewById(R.id.userProfileName);
        mUserProfileStatus = findViewById(R.id.userProfileStatus);
        mUserImage = findViewById(R.id.userProfileImage);
        mSendRequestBtn = findViewById(R.id.sendRequestBtn);
        mDeclineFriendRequestBtn = findViewById(R.id.declineFriendRequestBtn);
        mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);

        current_state = 0;
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
                String displayImage = dataSnapshot.child("image").getValue().toString();
                mUserProfileName.setText(displayName);
                mUserProfileStatus.setText(displayStatus);
                Picasso.get().load(displayImage).fit().centerCrop().placeholder(R.drawable.default_avatar).into(mUserImage);

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
                                        mSendRequestBtn.setText("Unfriend");
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
                //this is visible only for user1 when he recived a friend request from user2. I will check it anyway
                if (current_state == 2) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mSendRequestBtn.setEnabled(true);
                                    current_state = 0;
                                    mSendRequestBtn.setText("Send Friend Request");
                                    mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);

                                }
                            });

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
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue(2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mSendRequestBtn.setEnabled(true);
                                        current_state = 1;
                                        mSendRequestBtn.setText("Cancel Friend Request");
                                    }
                                });
                            }


                        }
                    });

                }

                //------CANCEL FRIEND REQUEST BEHAVIOUR-----
                if (current_state == 1) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mSendRequestBtn.setEnabled(true);
                                    current_state = 0;
                                    mSendRequestBtn.setText("Send Friend Request");

                                }
                            });

                        }
                    });

                }

                //-----ACCEPT FRIEND REQUEST-----

                if (current_state == 2) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendsDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mSendRequestBtn.setEnabled(true);
                                                    current_state = 3;
                                                    mSendRequestBtn.setText("Unfriend");
                                                    mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);

                                                }
                                            });

                                        }
                                    });
                                }
                            });

                        }
                    });

                }

                if (current_state == 3) {
                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendsDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mSendRequestBtn.setEnabled(true);
                                    current_state = 0;
                                    mSendRequestBtn.setText("Send Friend Request");

                                }
                            });

                        }
                    });

                }

            }
        });

    }
}
