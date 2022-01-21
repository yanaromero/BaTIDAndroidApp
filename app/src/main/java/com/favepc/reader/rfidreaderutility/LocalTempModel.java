package com.favepc.reader.rfidreaderutility;

public class LocalTempModel {
    private int id;
    private int bandId;
    private double temperature;
    private String rfidNumber;
    private String datetime;

    public LocalTempModel(int id, int bandId, double temperature, String rfidNumber, String datetime) {
        this.id = id;
        this.bandId = bandId;
        this.temperature = temperature;
        this.rfidNumber = rfidNumber;
        this.datetime = datetime;
    }

    public LocalTempModel() {
    }

    @Override
    public String toString() {
        return "LocalTempModel{" +
                "id=" + id +
                ", bandId=" + bandId +
                ", temperature=" + temperature +
                ", rfidNumber='" + rfidNumber + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBandId() {
        return bandId;
    }

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getRfidNumber() {
        return rfidNumber;
    }

    public void setRfidNumber(String rfidNumber) {
        this.rfidNumber = rfidNumber;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
