package com.example.parking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import java.util.Calendar;

public class FinishParkingActivity extends AppCompatActivity implements PaymentResultListener {

    private TextView tvParkingId, tvVehicleNumber, tvOwnerName, tvOwnerEmail, tvStartTime, tvEndTime, tvTotalPrice, tvPricePerHour;
    private Button btnPayThroughWallet, btnPayNow, btnCancel;
    private DatabaseReference parkingRef, vehicleRef, userRef;
    private String parkingId, vehicleNumber;
    private long startTime, endTime;
    private double pricePerHour, totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_parking);


        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button

        // Initialize Razorpay Checkout
        Checkout.preload(getApplicationContext());

        // Initialize Firebase references
        parkingId = getIntent().getStringExtra("PARKING_ID");
        vehicleNumber = getIntent().getStringExtra("VEHICLE_NUMBER");

        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots").child(parkingId);
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Vehicles").child(vehicleNumber);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()); // Referring to the user owning the vehicle

        // Initialize UI elements
        tvParkingId = findViewById(R.id.tvParkingIdValue);
        tvVehicleNumber = findViewById(R.id.tvVehicleNumberValue);
        tvOwnerName = findViewById(R.id.tvOwnerNameValue);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmailValue);
        tvStartTime = findViewById(R.id.tvStartTimeValue);
        tvEndTime = findViewById(R.id.tvEndTimeValue);
        tvTotalPrice = findViewById(R.id.tvTotalPriceValue);
        tvPricePerHour = findViewById(R.id.tvPricePerHourValue);

        btnPayThroughWallet = findViewById(R.id.btnPayThroughWallet);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnCancel = findViewById(R.id.btnCancel);

        // Fetch Parking details
        fetchParkingDetails();
        // Fetch Vehicle details
        fetchVehicleDetails();
        // Fetch User details for owner name and email
        fetchUserDetails();



        // Handle payment option selection
        btnPayThroughWallet.setOnClickListener(v -> handlePayment("wallet","phone"));
        btnPayNow.setOnClickListener(v -> fetchPhoneNumber());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // If cancelled, return no result
            finish(); // Close the activity and return to ParkingFragment
        });
    }
    // Handle Back Button Press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close Activity and Go Back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchParkingDetails() {
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    pricePerHour = snapshot.child("pricePerHour").getValue(Double.class);

                    // Set the parking details in the UI
                    tvParkingId.setText(parkingId);
                    tvPricePerHour.setText("₹" + pricePerHour);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FinishParkingActivity.this, "Failed to load parking details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVehicleDetails() {
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    startTime = snapshot.child("startTime").getValue(Long.class);

                    // Calculate end time as current time
                    endTime = System.currentTimeMillis();

                    // Display start time and end time in the UI
                    tvStartTime.setText(formatDate(startTime));
                    tvEndTime.setText(formatDate(endTime));

                    // Calculate the total time in hours
                    long durationMillis = endTime - startTime;
                    double durationHours = durationMillis / (1000.0 * 60 * 60);  // Convert to hours
                    totalPrice = durationHours * pricePerHour;

                    // Display the total price
                    tvTotalPrice.setText("₹" + totalPrice);
                    tvVehicleNumber.setText(vehicleNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FinishParkingActivity.this, "Failed to load vehicle details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ownerName = snapshot.child("name").getValue(String.class);
                    String ownerEmail = snapshot.child("email").getValue(String.class);

                    // Set the user (owner) details in the UI
                    tvOwnerName.setText( ownerName);
                    tvOwnerEmail.setText(ownerEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FinishParkingActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.getTime().toString();
    }

    private void handlePayment(String paymentMethod,String phoneNumber) {
        // Depending on payment method, either use Wallet or Razorpay
        if ("wallet".equals(paymentMethod)) {
            // Handle Wallet Payment
            userRef.child("wallet").child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        double walletBalance = snapshot.getValue(Double.class);

                        // Check if the wallet balance is sufficient
                        if (walletBalance >= totalPrice) {
                            // Deduct the total price from the wallet balance
                            double newBalance = walletBalance - totalPrice;

                            // Update wallet balance in the database
                            userRef.child("wallet").child("balance").setValue(newBalance)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FinishParkingActivity.this, "Payment made through Wallet", Toast.LENGTH_SHORT).show();

                                            // Store transaction details
                                            storeTransactionDetails("Wallet Payment", totalPrice);

                                            // Send success result back to ParkingFragment
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("paymentStatus", "SUCCESS");
                                            resultIntent.putExtra("PARKING_ID", parkingId);
                                            resultIntent.putExtra("VEHICLE_NUMBER", vehicleNumber);
                                            setResult(RESULT_OK, resultIntent); // Send back the result to ParkingFragment
                                            finish(); // Close the activity and return to ParkingFragment
                                        } else {
                                            Toast.makeText(FinishParkingActivity.this, "Failed to update wallet balance", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(FinishParkingActivity.this, "Insufficient wallet balance!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FinishParkingActivity.this, "Wallet balance not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FinishParkingActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("razorpay".equals(paymentMethod)) {
            // Handle Razorpay Payment
            try {
                JSONObject options = new JSONObject();
                options.put("name", "Parking System");
                options.put("description", "Parking fee");
                options.put("currency", "INR");
                options.put("amount", (int) (totalPrice * 100)); // Amount in paise
                options.put("prefill.email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                options.put("prefill.contact", phoneNumber); // Replace with user's phone number

                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_B52gKGUK2XVJrF"); // Replace with your Razorpay Key ID
                checkout.open(FinishParkingActivity.this, options);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error starting payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        // Payment success callback
        Toast.makeText(this, "Payment Successful: " + paymentId, Toast.LENGTH_SHORT).show();

        // Store transaction details
        storeTransactionDetails(paymentId, totalPrice); // Assuming new balance is the same as the total price, adjust as needed.

        // Send the success result back to ParkingFragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "SUCCESS");
        resultIntent.putExtra("PARKING_ID", parkingId);
        resultIntent.putExtra("VEHICLE_NUMBER", vehicleNumber);  // Add vehicle number to the result intent

        setResult(RESULT_OK, resultIntent); // Send back the result to ParkingFragment
        finish(); // Close FinishParkingActivity and return to ParkingFragment
    }

    @Override
    public void onPaymentError(int code, String response) {
        // Payment failure callback
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_SHORT).show();

        // Send the failure result back to ParkingFragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "failure");
        setResult(RESULT_CANCELED, resultIntent); // Send failure result to ParkingFragment
        finish(); // Stay on the same page or go back to ParkingFragment
    }

    private void fetchPhoneNumber() {
        userRef.child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phoneNumber = snapshot.getValue(String.class);
                    handlePayment("razorpay", phoneNumber);  // Pass the phone number to the handlePayment method
                } else {
                    Toast.makeText(FinishParkingActivity.this, "Phone number not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FinishParkingActivity.this, "Failed to fetch phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void storeTransactionDetails(String paymentId, double amount) {
        // Create a new transaction record
        String transactionId = userRef.child("transactions").push().getKey();
        if (transactionId != null) {
            DatabaseReference transactionRef = userRef.child("transactions").child(transactionId);

            // Create a transaction object
            transactionRef.setValue(new Transaction(transactionId, amount, "Parking", paymentId, System.currentTimeMillis()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(FinishParkingActivity.this, "Transaction saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FinishParkingActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}