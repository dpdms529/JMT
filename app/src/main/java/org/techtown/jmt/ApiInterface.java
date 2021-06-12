package org.techtown.jmt;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("v2/local/search/keyword.json")
    Call<SearchResult> getSearchLocation(
            @Header("Authorization") String token,
            @Query("query") String query,
            @Query("size") int size,
            @Query("category_group_code") String category
    );
}
