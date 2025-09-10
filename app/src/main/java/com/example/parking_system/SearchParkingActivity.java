
package com.example.parking_system;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchParkingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView parkingRecyclerView;
    private ParkingAdapter parkingAdapter;
    private List<ParkingSpot> parkingSpotList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentLocation;
    private GoogleMap mMap;
    private DatabaseReference parkingRef;
    private String selectedVehicleRegNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_parking);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button
        getSupportActionBar().setTitle("Select Parking"); // Set Toolbar Title

        // Retrieve the selected vehicle registration number
        selectedVehicleRegNo = getIntent().getStringExtra("vehicleNumber");

        if (selectedVehicleRegNo != null) {
            Toast.makeText(this, "Searching parking for: " + selectedVehicleRegNo, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No vehicle selected!", Toast.LENGTH_SHORT).show();
        }


        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots");

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        getCurrentLocation();
    }

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

        // Enable the user's location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Move camera to user's location

                    // Fetch and show parking spots
                    fetchParkingSpots();
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void fetchParkingSpots() {
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkingSpotList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ParkingSpot spot = data.getValue(ParkingSpot.class);
                    if (spot != null && "vacant".equals(spot.getStatus())) {
                        parkingSpotList.add(spot);

                        // Add parking spot markers to the map
                        LatLng parkingLocation = new LatLng(spot.getLatitude(), spot.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(parkingLocation).title(spot.getName()));
                    }
                }

                // Set adapter only after fetching locations
                parkingAdapter = new ParkingAdapter(SearchParkingActivity.this, parkingSpotList, currentLocation, selectedVehicleRegNo);
                parkingRecyclerView.setAdapter(parkingAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchParkingActivity.this, "Failed to load parking spots", Toast.LENGTH_SHORT).show();
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



    // Add this method to focus the camera on the selected parking spot
    public void focusOnParkingSpot(LatLng parkingLocation) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(parkingLocation, 15)); // Zoom in on the parking spot
    }
}

