package com.alextns.chatapp_master;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.marlonlom.utilities.timeago.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseRecyclerAdapter<Friends, UsersViewHolder> mFriendsAdaptor;
    private String mCurrentUser;
    private String mChatFriendName;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsList = view.findViewById(R.id.friends_list);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(mLayoutManager);


    return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendsDatabase, Friends.class).build();

        mFriendsAdaptor = new FirebaseRecyclerAdapter<Friends, UsersViewHolder>(options) {


            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);


                return new UsersViewHolder(mView);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final Friends model) {

                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot!=null) {
                            mChatFriendName = dataSnapshot.child("name").getValue().toString();
                            String mChatFriendStatus = dataSnapshot.child("status").getValue().toString();
                            String mChatFriendThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            Object mChatFriendOnline = dataSnapshot.child("online").getValue();
                            holder.setName(mChatFriendName);
                            holder.setStatus(mChatFriendStatus);
                            holder.setUserOnline(mChatFriendOnline);
                            holder.setThumbImage(mChatFriendThumb);
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options [] = new CharSequence[] {"Open Profile", "Send message"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Click event for each item
                                if (which == 0){
                                    Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                                    userProfileIntent.putExtra("user_id", list_user_id);
                                    startActivity(userProfileIntent);
                                }else if (which ==1) {
                                    //go to chat
                                    Intent chatProfileIntent = new Intent(getContext(), ChatActivity.class);
                                    chatProfileIntent.putExtra("user_id", list_user_id);
                                    chatProfileIntent.putExtra("user_name", mChatFriendName);
                                    startActivity(chatProfileIntent);
                                }

                            }
                        });
                        builder.show();
                    }
                });


            }
        };
        mFriendsList.setAdapter(mFriendsAdaptor);
        mFriendsAdaptor.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        mFriendsAdaptor.stopListening();


    }
}
