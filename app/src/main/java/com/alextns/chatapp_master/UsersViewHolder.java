package com.alextns.chatapp_master;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author alexandru.tanasescu on 05/05/2018.
 */
public class UsersViewHolder extends RecyclerView.ViewHolder {

    View mView;
    TextView userLastSeen;
    public UsersViewHolder(View itemView){
        super(itemView);
        mView = itemView;
    }
    public void setName(String name){
        TextView mDisplayName = mView.findViewById(R.id.userSingleName);
        mDisplayName.setText(name);

    }
    public void setStatus(String status){
        TextView mDisplayStatus = mView.findViewById(R.id.userSingleStatus);
        mDisplayStatus.setText(status);

    }
    public void setDate(String date){

        TextView userStatusView = mView.findViewById(R.id.userSingleStatus);
        userStatusView.setText(date);

    }

    public void setThumbImage(String thumb_image){
        CircleImageView mDisplayImage = mView.findViewById(R.id.userChatImage);
        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
    }

    public void setUserOnline(Object online_status) {

        ImageView userOnlineView = mView.findViewById(R.id.user_online_status);
         userLastSeen = mView.findViewById(R.id.lastSeenTextView);

        if(online_status.equals(true)){

            userOnlineView.setVisibility(View.VISIBLE);
            userLastSeen.setText("Online");

        } else {

            userOnlineView.setVisibility(View.INVISIBLE);
            //long timeInMillis = Long.valueOf(online_status);
            userLastSeen.setText(TimeAgo.using(Long.parseLong(online_status.toString())));

        }

    }

}
