package com.alextns.chatapp_master;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatFriendID;
    private String mChatFriendName;
    private android.support.v7.widget.Toolbar mChatToolbar;
    private DatabaseReference mRootReference;
    private FirebaseAuth mAuth;
    private TextView mChatUserName;
    private TextView mChatLastSeen;
    private CircleImageView mFriendThumbImage;
    private String mCurrentUserID;
    private ImageButton sendMessageBtn;
    private ImageButton chatMenuBtn;
    private EditText inputMessage;
    private MessageAdapter mAdapter;
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatFriendID = getIntent().getStringExtra("user_id");
        mChatFriendName = getIntent().getStringExtra("user_name");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mRootReference = FirebaseDatabase.getInstance().getReference();
        //set toolbar
        mChatToolbar = findViewById(R.id.chatMainToolbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle(mChatFriendName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View actionBarView = inflater.inflate(R.layout.chat_toolbar, null);
        actionBar.setCustomView(actionBarView);

        //custom action bar items
        mChatUserName = findViewById(R.id.userChatName);
        mChatLastSeen = findViewById(R.id.userChatLastSeen);
        mFriendThumbImage = findViewById(R.id.userChatImage);

        chatMenuBtn = findViewById(R.id.chatMenuBtn);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        inputMessage = findViewById(R.id.inputMessage);


        //set adaptor and layout
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);


        mMessagesList.setAdapter(mAdapter);
        loadMessages();




        //get friends profile details
        mRootReference.child("Users").child(mChatFriendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    String mChatUserThumbImage =  dataSnapshot.child("thumb_image").getValue().toString();
                    Object online_status = dataSnapshot.child("online").getValue();

                    mChatUserName.setText(mChatFriendName);

                    if (!online_status.equals(true)){
                        mChatLastSeen.setText(TimeAgo.using(Long.parseLong(online_status.toString())));
                    }else{
                        mChatLastSeen.setText("Online");
                    }

                    Picasso.get().load(mChatUserThumbImage).placeholder(R.drawable.default_avatar).into(mFriendThumbImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //create message object in DB
        mRootReference.child("Users").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatFriendID)){
                    Map chatMap = new HashMap();
                    chatMap.put("seen", false);
                    chatMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatFriendID ,chatMap);
                    chatUserMap.put("Chat/" + mChatFriendID + "/" + mCurrentUserID ,chatMap);

                    mRootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("Chat Log", databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void loadMessages() {
        mRootReference.child("messages").child(mCurrentUserID).child(mChatFriendID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {
        String message = inputMessage.getText().toString();
        if (!TextUtils.isEmpty(message)){
            inputMessage.setText("");

            String currentUserChatRef = "messages/" + mCurrentUserID + "/" + mChatFriendID;
            String friendChatRef = "messages/" + mChatFriendID  + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootReference.child("messages").child(mCurrentUserID).child(mChatFriendID).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen" ,false);
            messageMap.put("type" ,"text");
            messageMap.put("time_stamp" ,ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserChatRef + "/" + push_id, messageMap);
            messageUserMap.put(friendChatRef + "/" + push_id, messageMap);

            mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("Chat Log", databaseError.getMessage());
                    }
                }
            });



        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null)
            ((ThotChat)this.getApplication()).startActivityTransitionTimer(mAuth.getCurrentUser().getUid());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser()!=null)
            ((ThotChat)this.getApplication()).stopActivityTransitionTimer(mAuth.getCurrentUser().getUid());
    }
}
