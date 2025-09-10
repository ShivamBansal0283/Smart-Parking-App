package com.example.parking_system;

public class Vehicle {
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleType;

    private long reserved;

    private String parking; // Stores the parking ID if the vehicle is parked, otherwise null

    private long startTime; // Stores the start time of parking, otherwise null

    private String status;  // New field to store the parking status ("Booked", "Timer", "Vacant")


    public Vehicle() {
        // Default constructor required for Firebase
    }

    public Vehicle(String vehicleNumber, String vehicleModel, String vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.reserved = 0;
        this.parking = null; // Default to not parked
        this.startTime = 0; // Default to no parking start time
        this.status = "Vacant"; // Default status when the vehicle is not parked



    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }
    public long getStartTime() {
        return startTime;
    }

    public long getReserved() {
        return reserved;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}