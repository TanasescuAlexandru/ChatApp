package com.alextns.chatapp_master;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author alexandru.tanasescu on 05/05/2018.
 */
public class UsersViewHolder extends RecyclerView.ViewHolder {

    View mView;
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
        CircleImageView mDisplayImage = mView.findViewById(R.id.userSingleImage);
        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
    }

    public void setUserOnline(boolean online_status) {

        ImageView userOnlineView = mView.findViewById(R.id.user_online_status);

        if(online_status){

            userOnlineView.setVisibility(View.VISIBLE);

        } else {

            userOnlineView.setVisibility(View.INVISIBLE);

        }

    }
}
