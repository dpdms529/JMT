package org.techtown.jmt;

public class PersonalComment {
    String store_name;
    String comment;
//    String image_path;  // 이미지 관련은 다시 알아볼 것(파이어베이스 저장소에서 가져와야 함)

    public PersonalComment(String store_name, String comment) {
        this.store_name = store_name;
        this.comment = comment;
    }

    public String getStoreName() { return store_name; }

    public void setStoreName(String store_name) { this.store_name = store_name; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}
