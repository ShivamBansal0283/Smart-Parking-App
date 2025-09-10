
package com.example.parking_system;

public class Transaction {
    private String transactionId;
    private double amount;
    private String forpayment;
    private String paymentId;
    private long timestamp;

    // Default constructor required for Firebase
    public Transaction() {}

    // Constructor
    public Transaction(String transactionId, double amount, String forpayment, String paymentId, long timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.forpayment = forpayment;
        this.paymentId = paymentId;
        this.timestamp = timestamp;
    }

    // Getters and setters for all fields
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getforpayment() {
        return forpayment;
    }

    public void setforpayment(String forpayment) {
        this.forpayment = forpayment;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}