package org.techtown.jmt;

public class PersonalComment {
    private String store_name;
    private String comment;
    private String image_url;
    private String location;

    public PersonalComment(String store_name, String comment, String image_url, String location) {
        this.store_name = store_name;
        this.comment = comment;
        this.image_url = image_url;
        this.location = location;
    }

    public String getStoreName() {
        return store_name;
    }

    public void setStoreName(String store_name) {
        this.store_name = store_name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
