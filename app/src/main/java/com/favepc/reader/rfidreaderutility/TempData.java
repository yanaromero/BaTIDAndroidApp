package com.favepc.reader.rfidreaderutility;

public class TempData {
    private String temp;

    private String epc;

    private String location;

    private String datetime;

    public TempData(String temp, String epc, String location, String datetime) {
        this.epc = epc;
        this.temp = temp;
        this.location = location;
        this.datetime = datetime;
    }

}
