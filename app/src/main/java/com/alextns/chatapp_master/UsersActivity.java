package com.alextns.chatapp_master;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersRecyclerView;
    private DatabaseReference mUsersDatabase;
    private FirebaseRecyclerAdapter<AllUsers, UsersViewHolder> mUsersAdaptor;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar mMainToolbar;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        setUpRecyclerView();
        mMainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mMainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("All Users");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<AllUsers>().setQuery(mUsersDatabase, AllUsers.class).build();

        mUsersAdaptor = new FirebaseRecyclerAdapter<AllUsers, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);



                return new UsersViewHolder(mView);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull AllUsers model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setThumbImage(model.getThumbImage());
                holder.setUserOnline(model.getOnline());

                //get user_id from DB in order to get user details in UserProfileActivity that is clicked
                final String user_id = getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userProfileIntent = new Intent(UsersActivity.this, UserProfileActivity.class);
                        userProfileIntent.putExtra("user_id", user_id);
                        startActivity(userProfileIntent);

                    }
                });


            }
        };

        mUsersRecyclerView.setAdapter(mUsersAdaptor);
}


    @Override
    protected void onStart() {
        super.onStart();
        mUsersAdaptor.startListening();


    }
    @Override
    public void onStop() {
        super.onStop();
        mUsersAdaptor.stopListening();

    }
    private void setUpRecyclerView(){
        mUsersRecyclerView = findViewById(R.id.users_list);
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((ThotChat)this.getApplication()).startActivityTransitionTimer();


    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ThotChat)this.getApplication()).stopActivityTransitionTimer();
    }

}
