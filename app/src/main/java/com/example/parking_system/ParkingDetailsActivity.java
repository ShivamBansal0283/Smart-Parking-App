package com.example.parking_system;//package com.example.parking_system;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

public class ParkingDetailsActivity extends AppCompatActivity {

    private TextView tvParkingName, tvParkingStatus, tvAvailableSlots, tvPricePerHour;
    private TextView tvVehicleNumber, tvOwnerName, tvOwnerEmail;
    private Button btnBook;
    private String parkingId, vehicleNumber, userId;
    private int availableSlots;
    private DatabaseReference parkingRef, vehicleRef, ownerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_details);


        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button

        // Get the Parking ID and Vehicle Number from the intent
        parkingId = getIntent().getStringExtra("PARKING_ID");
        vehicleNumber = getIntent().getStringExtra("VEHICLE_NUMBER");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the user ID

        // Initialize UI elements
        tvParkingName = findViewById(R.id.tvParkingNameValue);
        tvParkingStatus = findViewById(R.id.tvParkingStatusValue);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlotsValue);
        tvPricePerHour = findViewById(R.id.tvPricePerHourValue);

        tvVehicleNumber = findViewById(R.id.tvVehicleNumberValue);
        tvOwnerName = findViewById(R.id.tvOwnerNameValue);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmailValue);
        tvVehicleNumber = findViewById(R.id.tvVehicleNumberValue);

        btnBook = findViewById(R.id.btnBook);

        // Initialize Firebase references
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots").child(parkingId);
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles").child(vehicleNumber);
        ownerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);  // Reference to get vehicle owner's info

        // Fetch parking details
        fetchParkingDetails();

        // Set up the Book Button Click Listener
        btnBook.setOnClickListener(v -> bookParkingSpot());
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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String parkingName = snapshot.child("name").getValue(String.class);
                    String parkingStatus = snapshot.child("status").getValue(String.class);
                    availableSlots = snapshot.child("availableSlots").getValue(Integer.class);
                    double pricePerHour = snapshot.child("pricePerHour").getValue(Double.class);

                    // Update the UI with the fetched parking details
                    tvParkingName.setText(parkingName);
                    tvParkingStatus.setText(parkingStatus);
                    tvAvailableSlots.setText(Integer.toString(availableSlots));
                    tvPricePerHour.setText(" ₹" + pricePerHour);
                    tvVehicleNumber.setText(vehicleNumber);

                    // Fetch the vehicle owner details
                    fetchOwnerDetails();
                } else {
                    Toast.makeText(ParkingDetailsActivity.this, "Parking ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParkingDetailsActivity.this, "Failed to fetch parking details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOwnerDetails() {
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ownerName = snapshot.child("name").getValue(String.class);
                    String ownerEmail = snapshot.child("email").getValue(String.class);

                    // Update the UI with the owner's details
                    tvOwnerName.setText(ownerName);
                    tvOwnerEmail.setText( ownerEmail);
                } else {
                    Toast.makeText(ParkingDetailsActivity.this, "Failed to fetch owner details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParkingDetailsActivity.this, "Failed to fetch owner details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookParkingSpot() {
        // Check if parking spot is available
        if (availableSlots <= 0) {
            Toast.makeText(this, "No available slots!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the available slots and mark the parking as occupied
        parkingRef.child("availableSlots").setValue(availableSlots - 1);
        if (availableSlots - 1 == 0) {
            parkingRef.child("status").setValue("Full");
        }

        // Add the vehicle's parking information
        parkingRef.child("parkedVehicles").child(vehicleNumber).setValue(true);
        vehicleRef.child("parking").setValue(parkingId);  // Update the vehicle with the parking ID
        vehicleRef.child("startTime").setValue(System.currentTimeMillis());  // Set the start time for the vehicle

        // Show success message
        Toast.makeText(this, "Parking booked successfully!", Toast.LENGTH_SHORT).show();

        // Redirect to the Parking Fragment
        Intent intent = new Intent(ParkingDetailsActivity.this, DashboardActivity.class);
        intent.putExtra("open_fragment", "ParkingFragment");
        startActivity(intent);
        finish();
    }
}