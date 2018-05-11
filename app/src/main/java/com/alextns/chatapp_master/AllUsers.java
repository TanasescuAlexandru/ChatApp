package com.alextns.chatapp_master;

/**
 * @author alexandru.tanasescu on 04/05/2018.
 */

public class AllUsers {

    public String name;
    public String status;
    public String thumb_image;
    public boolean online;

    public boolean getOnline() {
        return online;
    }



    public AllUsers(){

    }

    public AllUsers(String name, String status, String thumb_image, boolean online) {
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }


    public String getThumbImage() {
        return thumb_image;
    }



}
