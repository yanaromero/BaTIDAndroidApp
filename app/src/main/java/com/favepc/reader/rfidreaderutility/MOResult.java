package com.favepc.reader.rfidreaderutility;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MOResult {
    // 5003 is the key of the Manila Observatory location
    @SerializedName("5003")
    private MOValue weatherData;
    public MOValue getWeatherData() {
        return weatherData;
    }

}
