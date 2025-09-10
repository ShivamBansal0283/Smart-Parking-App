package com.example.parking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class MyVehiclesFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList;
    private DatabaseReference vehicleRef;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button btnAddVehicle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_vehicles, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        btnAddVehicle = view.findViewById(R.id.btnAddVehicle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return view;
        }

        String userId = currentUser.getUid();
        vehicleRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles");

        vehicleList = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(getContext(), vehicleList);
        recyclerView.setAdapter(vehicleAdapter);

        loadVehicles();

        btnAddVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddVehicleActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadVehicles() {
        progressBar.setVisibility(View.VISIBLE);

        vehicleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vehicleList.clear();
                for (DataSnapshot vehicleSnapshot : snapshot.getChildren()) {
                    Vehicle vehicle = vehicleSnapshot.getValue(Vehicle.class);
                    vehicleList.add(vehicle);
                }
                vehicleAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load vehicles", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}