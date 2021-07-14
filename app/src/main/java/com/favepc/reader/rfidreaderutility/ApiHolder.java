package com.favepc.reader.rfidreaderutility;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiHolder {

    @FormUrlEncoded
    @POST("create.php")
    Call<TempData> createPost(
            @Field("epc") String epc,
            @Field("temp") String temp
    );

    @POST("create.php")
    Call<TempData> createPost(@Body TempData body);
    
}
