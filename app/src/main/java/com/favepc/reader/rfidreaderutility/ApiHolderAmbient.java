package com.favepc.reader.rfidreaderutility;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiHolderAmbient {
    // contains current data displayed on panahon.observatory.ph
    @GET("stn_mo_obs.json")
    Call<MOResult> getAmbientTemperature();

}
