package com.favepc.reader.rfidreaderutility;

public class TempData {
    private String temperature;

    private String bandId;

    private String rfidNumber;

    private String date_time;

    public TempData(String temp, String epc, String rfidNumber, String date_time) {
        this.bandId = epc;
        this.temperature = temp;
        this.rfidNumber = rfidNumber;
        this.date_time = date_time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBandId() {
        return bandId;
    }

    public void setBandId(String bandId) {
        this.bandId = bandId;
    }

    public String getRfidNumber() {
        return rfidNumber;
    }

    public void setRfidNumber(String rfidNumber) {
        this.rfidNumber = rfidNumber;
    }

    public String getDatetime() {
        return date_time;
    }

    public void setDatetime(String datetime) {
        this.date_time = datetime;
    }

}
