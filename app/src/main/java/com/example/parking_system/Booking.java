package com.example.parking_system;

public class Booking {
    private String bookingId;
    private String userId;
    private String vehicleNumber;
    private String parkingId;
    private String startTime;

    // Required Default Constructor for Firebase
    public Booking() {
    }

    public Booking(String bookingId, String userId, String vehicleNumber, String parkingId, String startTime) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.parkingId = parkingId;
        this.startTime = startTime;
    }

    // Getters
    public String getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getParkingId() {
        return parkingId;
    }

    public String getStartTime() {
        return startTime;
    }

    // Setters (if needed)
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}