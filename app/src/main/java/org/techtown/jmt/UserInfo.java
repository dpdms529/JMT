package org.techtown.jmt;

public class UserInfo {
    String user_name;
    String userID;
    long num_of_comment;

    public UserInfo(String user_name, String userID, long num_of_comment) {
        this.user_name = user_name;
        this.userID = userID;
        this.num_of_comment = num_of_comment;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public long getNumOfComment() {
        return num_of_comment;
    }

    public void setNumOfComment(long num_of_comment) {
        this.num_of_comment = num_of_comment;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
