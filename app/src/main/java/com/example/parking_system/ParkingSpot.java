package com.example.parking_system;

public class ParkingSpot {
    private String parkingId;
    private String name;
    private double latitude;
    private double longitude;
    private String status; // "vacant" or "occupied"
    private double pricePerHour;
    private double pricePerMin;
    private String mode; // "2-wheeler" or "4-wheeler"
    private int capacity;
    private int availableSlots;
    private String type; // "public" or "private"
    private String hours; // Operational hours (e.g., "24/7")
    private int rating; // Average rating (out of 5)
    private String[] reviews; // Array of user reviews
    private String owner; // Owner info (if private parking)
    private boolean isVerified; // Whether the parking is verified

    // Default constructor (Required for Firebase)
    public ParkingSpot() {
    }

    // Constructor
    public ParkingSpot(String parkingId, String name, double latitude, double longitude, String status,
                       double pricePerHour, double pricePerMin, String mode, int capacity, int availableSlots,
                       String type, String hours, int rating, String[] reviews, String owner, boolean isVerified) {
        this.parkingId = parkingId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.pricePerHour = pricePerHour;
        this.pricePerMin = pricePerMin;
        this.mode = mode;
        this.capacity = capacity;
        this.availableSlots = availableSlots;
        this.type = type;
        this.hours = hours;
        this.rating = rating;
        this.reviews = reviews;
        this.owner = owner;
        this.isVerified = isVerified;
    }

    // Getters and Setters
    public String getParkingId() { return parkingId; }
    public void setParkingId(String parkingId) { this.parkingId = parkingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public double getPricePerMin() { return pricePerMin; }
    public void setPricePerMin(double pricePerMin) { this.pricePerMin = pricePerMin; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String[] getReviews() { return reviews; }
    public void setReviews(String[] reviews) { this.reviews = reviews; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}