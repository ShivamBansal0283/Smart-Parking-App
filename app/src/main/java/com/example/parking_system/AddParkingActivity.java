package com.example.parking_system;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddParkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText etParkingName, etPricePerHour, etPricePerMin, etCapacity, etAvailableSlots;
    private Spinner spinnerMode;
    private Button btnAddParking;
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Marker currentMarker;

    private DatabaseReference parkingRef;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking);


        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button
        getSupportActionBar().setTitle("Add Vehicle"); // Set Toolbar Title

        // Initialize UI elements
        etParkingName = findViewById(R.id.etParkingName);
        etPricePerHour = findViewById(R.id.etPricePerHour);
        etPricePerMin = findViewById(R.id.etPricePerMin);
        etCapacity = findViewById(R.id.etCapacity);
        etAvailableSlots = findViewById(R.id.etAvailableSlots);
        spinnerMode = findViewById(R.id.spinnerMode);
        btnAddParking = findViewById(R.id.btnAddParking);

        // Firebase Database Reference
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots");

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient for current location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Add Parking Button Click
        btnAddParking.setOnClickListener(v -> addParkingSpot());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable location tracking if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Allow users to select a location by tapping on the map
        mMap.setOnMapClickListener(latLng -> {
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLocation = latLng;
            Toast.makeText(this, "Location Selected: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void addParkingSpot() {
        String name = etParkingName.getText().toString().trim();
        String pricePerHourStr = etPricePerHour.getText().toString().trim();
        String pricePerMinStr = etPricePerMin.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String availableSlotsStr = etAvailableSlots.getText().toString().trim();
        String mode = spinnerMode.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pricePerHourStr) || TextUtils.isEmpty(pricePerMinStr) ||
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(availableSlotsStr) || selectedLocation == null) {
            Toast.makeText(this, "Please fill all fields and select a location!", Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerHour = Double.parseDouble(pricePerHourStr);
        double pricePerMin = Double.parseDouble(pricePerMinStr);
        int capacity = Integer.parseInt(capacityStr);
        int availableSlots = Integer.parseInt(availableSlotsStr);

        // Generate unique ID for parking spot
        String parkingId = parkingRef.push().getKey();

        // Create ParkingSpot Object
        ParkingSpot spot = new ParkingSpot(parkingId, name, selectedLocation.latitude, selectedLocation.longitude,
                "vacant", pricePerHour, pricePerMin, mode, capacity, availableSlots, "public", "24/7", 0, null, "", false);

        // Save to Firebase
        parkingRef.child(parkingId).setValue(spot).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Parking Spot Added Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add parking spot!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}