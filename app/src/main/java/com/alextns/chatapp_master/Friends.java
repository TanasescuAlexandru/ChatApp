package com.alextns.chatapp_master;

/**
 * @author alexandru.tanasescu on 09/05/2018.
 */
public class Friends {

    private String online;
    private String userName;
    private String userStatus;
    private String thumb_image;

    public String getName() {
        return userName;
    }

    public String getStatus() {
        return userStatus;
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public String date;

    public Friends(){

    }

    public String getOnline() {
        return online;
    }

    public String getDate() {
        return date;
    }


    public Friends(String date){
        this.date=date;
    }

}
