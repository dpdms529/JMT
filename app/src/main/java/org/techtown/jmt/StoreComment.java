package org.techtown.jmt;

public class StoreComment {
    private String user_name;
    private String comment;
    //private String image_url;

    public StoreComment(String user_name, String comment) {
        this.user_name = user_name;
        this.comment = comment;
        //this.image_url = image_url;
    }

    public String getUserName() { return user_name; }

    public void setUserName(String user_name) { this.user_name = user_name; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    //public String getImageUrl() { return image_url; }

    //public void setImageUrl(String image_url) { this.image_url = image_url; }
}
