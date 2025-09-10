

package com.example.parking_system;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Calendar;

public class ConfirmBookingActivity extends AppCompatActivity {

    private TextView tvOwner, tvVehicleNumber, tvParkingName, tvParkingId, tvLocation, tvMode, tvPricePerHour, tvEmail, tvAvailableSlots;
    private Button btnSelectTime, btnBook;
    private FirebaseAuth mAuth;
    private DatabaseReference bookingsRef, parkingRef, vehicleRef,userRef;
    private String userId, parkingId, vehicleNumber;
    private int availableSlots;
    private String selectedStartTime = ""; // Variable to store the selected start time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);


        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button
        getSupportActionBar().setTitle("Confirm Booking"); // Set Toolbar Title

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();  // Get current user's ID
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        tvOwner = findViewById(R.id.tvOwnerValue);
        tvVehicleNumber = findViewById(R.id.tvVehicleNumberValue);
        tvParkingName = findViewById(R.id.tvParkingNameValue);
        tvParkingId = findViewById(R.id.tvParkingIdValue);
        tvLocation = findViewById(R.id.tvLocationValue);
        tvMode = findViewById(R.id.tvModeValue);
        tvPricePerHour = findViewById(R.id.tvPricePerHourValue);
        tvEmail = findViewById(R.id.tvEmailValue);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlotsValue);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnBook = findViewById(R.id.btnBook);

        // Retrieve data from the Intent
        Intent intent = getIntent();
        parkingId = intent.getStringExtra("PARKING_ID");
        vehicleNumber = intent.getStringExtra("VEHICLE_NUMBER");

        if (TextUtils.isEmpty(parkingId) || TextUtils.isEmpty(vehicleNumber)) {
            Toast.makeText(this, "Error: Missing data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase references
        bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots").child(parkingId);
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles").child(vehicleNumber);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId); // Reference to fetch user data


        // Display the vehicle and parking details
        tvVehicleNumber.setText(vehicleNumber);
        tvParkingId.setText(parkingId);

        // Fetch parking details from Firebase
        fetchParkingDetails();

        fetchUserDetails();

        // Set Select Time button listener
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());

        // Set Book button listener
        btnBook.setOnClickListener(v -> confirmBooking());
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

    private void fetchUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class); // Get user name
                    String userEmail = snapshot.child("email").getValue(String.class); // Get user email

                    // Update UI with user details
                    tvOwner.setText(userName);
                    tvEmail.setText(userEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ConfirmBookingActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchParkingDetails() {
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
//                    String owner = snapshot.child("owner").getValue(String.class);
                    String parkingName = snapshot.child("name").getValue(String.class);
                    String location = snapshot.child("latitude").getValue(Double.class) + ", " +
                            snapshot.child("longitude").getValue(Double.class);
                    String mode = snapshot.child("mode").getValue(String.class);
                    double pricePerHour = snapshot.child("pricePerHour").getValue(Double.class);
                    availableSlots = snapshot.child("availableSlots").getValue(Integer.class);

                    // Update UI with parking details
//                    tvOwner.setText("Owner: " + owner);
                    tvParkingName.setText(parkingName);
                    tvLocation.setText(location);
                    tvMode.setText(mode);
                    tvPricePerHour.setText("₹" + pricePerHour);
                    tvAvailableSlots.setText(Integer.toString(availableSlots));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ConfirmBookingActivity.this, "Failed to load parking details", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private long selectedStartTimeMillis;

    private void showTimePickerDialog() {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog to select time
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            // Format the time and set it to the Select Time button
            selectedStartTime = String.format("%02d:%02d", hourOfDay, minute1);
            btnSelectTime.setText(selectedStartTime);

            // Convert the selected time to milliseconds for comparison
            selectedStartTimeMillis = getMillisecondsForTime(hourOfDay, minute1);

            // You can now use selectedStartTimeMillis to compare with System.currentTimeMillis()
            // For example:
            if (selectedStartTimeMillis < System.currentTimeMillis()) {
                // The selected start time is in the past
                Toast.makeText(this, "Selected time is in the past!", Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private long getMillisecondsForTime(int hourOfDay, int minute) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();

        // Set the time to the selected hour and minute, keeping the current date
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Return the time in milliseconds
        return calendar.getTimeInMillis();
    }

    private void confirmBooking() {
        // Check if a valid start time is selected
        if (TextUtils.isEmpty(selectedStartTime)) {
            Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if there are available slots
        if (availableSlots <= 0) {
            Toast.makeText(this, "No available slots!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique booking ID and create a booking object
        String bookingId = bookingsRef.push().getKey();
        Booking booking = new Booking(bookingId, userId, vehicleNumber, parkingId, selectedStartTime);

        // Save the booking to Firebase
        bookingsRef.child(bookingId).setValue(booking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Decrease available slots by 1
                parkingRef.child("availableSlots").setValue(availableSlots - 1);

                // Update parking status to "occupied" if no slots are available
                if (availableSlots - 1 == 0) {
                    parkingRef.child("status").setValue("occupied");
                }

                // Add the vehicle number to the parking spot's parked vehicles
                parkingRef.child("parkedVehicles").child(vehicleNumber).setValue(true);

                // Update vehicle's parking field with the parkingId
                vehicleRef.child("parking").setValue(parkingId);

                // Update vehicle's parking field with the parkingId
                vehicleRef.child("reserved").setValue(selectedStartTimeMillis);

//                // **Update the vehicle start time in the database**
//                vehicleRef.child("startTime").setValue(selectedStartTimeMillis); // Store the selected start time

                Toast.makeText(ConfirmBookingActivity.this, "Parking Booked Successfully!", Toast.LENGTH_SHORT).show();

                // Redirect to Dashboard
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("open_fragment", "ParkingFragment");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ConfirmBookingActivity.this, "Booking Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}