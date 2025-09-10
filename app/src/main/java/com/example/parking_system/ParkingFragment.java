

package com.example.parking_system;

import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ParkingVehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList;
    private DatabaseReference vehicleRef, parkingRef;
    private FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return view;
        }

        String userId = currentUser.getUid();
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles");
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingSpots");

        vehicleList = new ArrayList<>();
        vehicleAdapter = new ParkingVehicleAdapter(getContext(), vehicleList, this::onSearchParking, this::onScanQR, this::onFinishParking, this::onNavigateToParking,this::cancelBooking,this::startParking);
        recyclerView.setAdapter(vehicleAdapter);

        loadVehicles();

        return view;
    }

    private void loadVehicles() {
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vehicleList.clear();
                for (DataSnapshot vehicleSnapshot : snapshot.getChildren()) {
                    Vehicle vehicle = vehicleSnapshot.getValue(Vehicle.class);
                    vehicleList.add(vehicle);
                }
                vehicleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load vehicles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSearchParking(Vehicle vehicle) {
        Intent intent = new Intent(getActivity(), SearchParkingActivity.class);
        intent.putExtra("vehicleNumber", vehicle.getVehicleNumber());
        startActivity(intent);
    }

    private void onScanQR(Vehicle vehicle) {
        Intent intent = new Intent(getActivity(), ScanQRCodeActivity.class);
        intent.putExtra("vehicleNumber", vehicle.getVehicleNumber());
        startActivityForResult(intent, 1001); // Request Code 1001
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            // Retrieve the QR code result (assuming it's parking ID or any other data you need)
            String scannedParkingId = data.getStringExtra("SCAN_RESULT");
            String selectedVehicleRegno = data.getStringExtra("vehicleNumber");
            if (scannedParkingId != null) {
                // Check if this Parking ID exists in the database
                parkingRef.child(scannedParkingId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If Parking ID exists, navigate to the parking details page
                            Intent intent = new Intent(getActivity(), ParkingDetailsActivity.class);
                            intent.putExtra("PARKING_ID", scannedParkingId); // Pass the Parking ID
                            intent.putExtra("VEHICLE_NUMBER", selectedVehicleRegno); // Pass the Parking ID
                            startActivity(intent);
                        } else {
                            // Show Invalid QR message and return to Parking Fragment
                            Toast.makeText(getContext(), "Invalid QR - Parking ID not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch parking details", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // If no valid Parking ID found in the QR, treat it as invalid
                Toast.makeText(getContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
            }
        }

        // Use Activity.RESULT_OK for handling the result
        if (requestCode == 1002) {
            if (resultCode == Activity.RESULT_OK) {  // Ensure you're using Activity.RESULT_OK
                // If the payment was successful, proceed with the database updates
                String paymentStatus = data.getStringExtra("paymentStatus");

                if ("SUCCESS".equals(paymentStatus)) {
                    String parkingId = data.getStringExtra("PARKING_ID");
                    String vehicleNumber = data.getStringExtra("VEHICLE_NUMBER");

                    // Now perform the database updates: increasing available slots, changing status, etc.
                    parkingRef.child(parkingId).child("availableSlots").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int availableSlots = snapshot.getValue(Integer.class);
                            parkingRef.child(parkingId).child("availableSlots").setValue(availableSlots + 1);

                            // If slots become available, mark the parking spot as "vacant"
                            if (availableSlots + 1 > 0) {
                                parkingRef.child(parkingId).child("status").setValue("vacant");
                            }

//                            parkingRef.child(parkingId).child("parkedVehicles").child(vehicleNumber).removeValue();
                            parkingRef.child(parkingId).child("parkedVehicles").child(vehicleNumber).removeValue();
                            vehicleRef.child(vehicleNumber).child("parking").setValue(null);
                            vehicleRef.child(vehicleNumber).child("endTime").removeValue();  // Remove the endTime
                            vehicleRef.child(vehicleNumber).child("startTime").setValue(0);  // Set startTime to 0
//                            vehicleRef.child(vehicleNumber).child("reserved").removeValue();  // remove reserved time for booking

                            Toast.makeText(getContext(), "Parking session ended successfully!", Toast.LENGTH_SHORT).show();
                            loadVehicles(); // Refresh vehicle list
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to finish parking", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Payment failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Payment process was cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void onFinishParking(Vehicle vehicle) {
        Toast.makeText(getContext(), "Parking ID for vehicle " + vehicle.getVehicleNumber() + ": " + vehicle.getParking(), Toast.LENGTH_LONG).show();

        // Check if the vehicle has a parking ID and if the start time has passed
        if (vehicle.getParking() != null) {

                String parkingId = vehicle.getParking();

                // Proceed to finish parking
                Intent intent = new Intent(getContext(), FinishParkingActivity.class);
                intent.putExtra("PARKING_ID", parkingId);
                intent.putExtra("VEHICLE_NUMBER", vehicle.getVehicleNumber());

                // Start FinishParkingActivity and expect a result back
                startActivityForResult(intent, 1002);  // Using request code 1002
//
        } else {
            // If parking ID is null, show that the vehicle is not parked
            Toast.makeText(getContext(), "No active parking found!", Toast.LENGTH_SHORT).show();
        }
    }


    private void onNavigateToParking(Vehicle vehicle) {
        if (vehicle.getParking() == null) {
            Toast.makeText(getContext(), "No active parking found!", Toast.LENGTH_SHORT).show();
            return;
        }

        parkingRef.child(vehicle.getParking()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double latitude = snapshot.child("latitude").getValue(Double.class);
                double longitude = snapshot.child("longitude").getValue(Double.class);

                String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", latitude, longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load parking location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelBooking(Vehicle vehicle) {
        String vehicleNumber = vehicle.getVehicleNumber();
        String parkingId = vehicle.getParking();

        // Set the start time to 0 and remove parking from the vehicle
        vehicleRef.child(vehicleNumber).child("startTime").setValue(0);
        vehicleRef.child(vehicleNumber).child("reserved").removeValue();
        vehicleRef.child(vehicleNumber).child("parking").removeValue();

        // Increase available slots by 1 in the parking spot
        parkingRef.child(parkingId).child("availableSlots").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availableSlots = snapshot.getValue(Integer.class);
                parkingRef.child(parkingId).child("availableSlots").setValue(availableSlots + 1);

                // If slots are available, mark parking as "vacant"
                if (availableSlots + 1 > 0) {
                    parkingRef.child(parkingId).child("status").setValue("vacant");
                }

                // Remove the vehicle from parkedVehicles
                parkingRef.child(parkingId).child("parkedVehicles").child(vehicleNumber).removeValue();

                Toast.makeText(getContext(), "Booking cancelled successfully!", Toast.LENGTH_SHORT).show();
                loadVehicles();  // Refresh the vehicle list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startParking(Vehicle vehicle) {
        String vehicleNumber = vehicle.getVehicleNumber();
        long currentTime = System.currentTimeMillis();

        // Update startTime for the vehicle in Firebase
        vehicleRef.child(vehicleNumber).child("startTime").setValue(currentTime);

        // Update reserved time to null since parking has started
        vehicleRef.child(vehicleNumber).child("reserved").removeValue();

        // Update the vehicle object in the local list
        vehicle.setStartTime(currentTime);  // Update the startTime in the local vehicle object

        // Notify the adapter that this item has changed
        int position = vehicleList.indexOf(vehicle);
        if (position != -1) {
            vehicleAdapter.notifyItemChanged(position);  // Notify the adapter to refresh this specific item
        }

        // You can also update the UI for the parking state (start button gone, timer shown)
        Toast.makeText(getContext(), "Parking started for vehicle " + vehicleNumber, Toast.LENGTH_SHORT).show();
    }
}