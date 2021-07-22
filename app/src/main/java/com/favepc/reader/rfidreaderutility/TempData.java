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

}
