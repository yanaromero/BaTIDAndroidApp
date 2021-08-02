package com.favepc.reader.rfidreaderutility;

public class TempData {
    private String temperature;

    private String rfidNumber;

    private String location;

//    private String datetime;

    public TempData(String temp, String epc, String location) {
        this.rfidNumber = epc;
        this.temperature = temp;
        this.location = location;
//        this.datetime = datetime;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRfidNumber() {
        return rfidNumber;
    }

    public void setRfidNumber(String rfidNumber) {
        this.rfidNumber = rfidNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
