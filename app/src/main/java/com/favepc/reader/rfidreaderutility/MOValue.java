package com.favepc.reader.rfidreaderutility;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MOValue {
    @SerializedName("pres")
    @Expose
    private String pres;
    @SerializedName("rr")
    @Expose
    private String rr;
    @SerializedName("rh")
    @Expose
    private String rh;
    @SerializedName("temp")
    @Expose
    private String temp;
    @SerializedName("td")
    @Expose
    private String td;
    @SerializedName("wdir")
    @Expose
    private String wdir;
    @SerializedName("wspd")
    @Expose
    private String wspd;
    @SerializedName("srad")
    @Expose
    private String srad;
    @SerializedName("hi")
    @Expose
    private String hi;
    @SerializedName("uvi")
    @Expose
    private String uvi;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("rainDay")
    @Expose
    private String rainDay;
    @SerializedName("rain24h")
    @Expose
    private String rain24h;
    @SerializedName("tx")
    @Expose
    private String tx;
    @SerializedName("tn")
    @Expose
    private String tn;

    public String getPres() {
        return pres;
    }

    public String getRr() {
        return rr;
    }

    public String getRh() {
        return rh;
    }

    public String getTemp() {
        return temp;
    }

    public String getTd() {
        return td;
    }

    public String getWdir() {
        return wdir;
    }

    public String getWspd() {
        return wspd;
    }

    public String getSrad() {
        return srad;
    }

    public String getHi() {
        return hi;
    }

    public String getUvi() {
        return uvi;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRainDay() {
        return rainDay;
    }

    public String getRain24h() {
        return rain24h;
    }

    public String getTx() {
        return tx;
    }

    public String getTn() {
        return tn;
    }
}
