package com.favepc.reader.rfidreaderutility;

public class LocalTempModel {
    private int id;
    private int rfidNumber;
    private double temperature;
    private String location;
    private String datetime;

    public LocalTempModel(int id, int rfidNumber, double temperature, String location, String datetime) {
        this.id = id;
        this.rfidNumber = rfidNumber;
        this.temperature = temperature;
        this.location = location;
        this.datetime = datetime;
    }

    public LocalTempModel() {
    }

    @Override
    public String toString() {
        return "LocalTempModel{" +
                "id=" + id +
                ", rfidNumber=" + rfidNumber +
                ", temperature=" + temperature +
                ", location='" + location + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRfidNumber() {
        return rfidNumber;
    }

    public void setRfidNumber(int rfidNumber) {
        this.rfidNumber = rfidNumber;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
