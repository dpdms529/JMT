package org.techtown.jmt;

public class StoreInfo {
    // category, best menu, location, comments 등 저장할 변수? 필요
    String store_name;
    long lover;

    public StoreInfo(String store_name, long lover) {
        this.store_name = store_name;
        this.lover = lover;
    }

    // 생성자 더 추가해서 맛집 상세 정보에서도 사용할 것

    public String getStoreName() { return store_name; }

    public void setStoreName(String store_name) { this.store_name = store_name; }

    public long getLover() { return lover; }

    public void setLover(long lover) { this.lover = lover; }
}
