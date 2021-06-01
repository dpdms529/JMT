package org.techtown.jmt;

public class StoreInfo {
    // category, best menu, location, comments 등 저장할 변수? 필요
    String store_name;

    public StoreInfo(String store_name) {   // 리스트에서 이용하므로 chip에 추가할 best menu도 필요함
        this.store_name = store_name;
    }

    // 생성자 더 추가해서 맛집 상세 정보에서도 사용할 것

    public String getStoreName() { return store_name; }

    public void setStoreName(String store_name) { this.store_name = store_name; }
}
