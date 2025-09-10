package com.example.parking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddVehicleActivity extends AppCompatActivity {

    private EditText etVehicleNumber, etVehicleModel, etVehicleType;
    private Button btnAddVehicle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference vehicleRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button
        getSupportActionBar().setTitle("Add Vehicle"); // Set Toolbar Title

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etVehicleType = findViewById(R.id.etVehicleType);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        progressBar = findViewById(R.id.progressBar);

        // Add Vehicle Button Click
        btnAddVehicle.setOnClickListener(v -> addVehicle());
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


    private void addVehicle() {
        String vehicleNumber = etVehicleNumber.getText().toString().trim();
        String vehicleModel = etVehicleModel.getText().toString().trim();
        String vehicleType = etVehicleType.getText().toString().trim();

        if (TextUtils.isEmpty(vehicleNumber)) {
            etVehicleNumber.setError("Enter vehicle number");
            return;
        }

        if (TextUtils.isEmpty(vehicleModel)) {
            etVehicleModel.setError("Enter vehicle model");
            return;
        }

        if (TextUtils.isEmpty(vehicleType)) {
            etVehicleType.setError("Enter vehicle type");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Get Current User ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // Initialize Firebase Database Reference
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles");

        // Create Vehicle Object
        Vehicle vehicle = new Vehicle(vehicleNumber, vehicleModel, vehicleType);

        // Store in Firebase under UserID
        vehicleRef.child(vehicleNumber).setValue(vehicle).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(AddVehicleActivity.this, "Vehicle Added Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddVehicleActivity.this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(AddVehicleActivity.this, "Failed to add vehicle!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}