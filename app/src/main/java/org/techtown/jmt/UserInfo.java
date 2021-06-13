package org.techtown.jmt;

public class UserInfo {
    // best menu, location, comments 등 저장할 변수? 필요
    String user_name;
    String userID;
    long num_of_comment;

    public UserInfo(String user_name, String userID, long num_of_comment) {   // 리스트에서 이용하므로 chip에 추가할 best menu도 필요함
        this.user_name = user_name;
        this.userID = userID;
        this.num_of_comment = num_of_comment;
    }

    // 생성자 더 추가해서 유저 리스트 정보에서도 사용할 것

    public String getUserName() { return user_name; }

    public void setUserName(String user_name) { this.user_name = user_name; }

    public long getNumOfComment() { return num_of_comment; }

    public void setNumOfComment(long num_of_comment) { this.num_of_comment = num_of_comment; }

    public String getUserID() { return userID; }

    public void setUserID(String userID) { this.userID = userID; }
}
