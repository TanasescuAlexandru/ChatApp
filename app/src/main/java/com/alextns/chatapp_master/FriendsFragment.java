package com.alextns.chatapp_master;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

                String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot!=null) {
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();
                            String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                            Object userOnline = dataSnapshot.child("online").getValue();
                            holder.setName(userName);
                            holder.setStatus(userStatus);
                            holder.setUserOnline(userOnline);
                            holder.setThumbImage(thumbImage);
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
