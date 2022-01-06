package com.favepc.reader.rfidreaderutility;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MOResult {
    @SerializedName("5003")
//    private List<MOValue> weatherData;
//    public List<MOValue> getWeatherData() {
//        return weatherData;
//    }
    private MOValue weatherData;
    public MOValue getWeatherData() {
        return weatherData;
    }

}
