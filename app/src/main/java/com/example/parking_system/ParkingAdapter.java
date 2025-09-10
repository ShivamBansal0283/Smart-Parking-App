
package com.example.parking_system;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {
    private Context context;
    private List<ParkingSpot> parkingList;
    private LatLng userLocation;
    private String selectedVehicleRegNo;

    public ParkingAdapter(Context context, List<ParkingSpot> parkingList, LatLng userLocation, String selectedVehicleRegNo) {
        this.context = context;
        this.parkingList = parkingList;
        this.userLocation = userLocation;
        this.selectedVehicleRegNo = selectedVehicleRegNo;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_spot, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingSpot spot = parkingList.get(position);

        // Set parking spot details
        holder.tvParkingName.setText(spot.getName());
        holder.tvMode.setText("Mode: " + spot.getMode());
        holder.tvStatus.setText("Status: " + spot.getStatus());

        // Calculate and display distance to the user location
        if (userLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude, spot.getLatitude(), spot.getLongitude(), results);
            holder.tvDistance.setText("Distance: " + (int) results[0] + " m");
        } else {
            holder.tvDistance.setText("Distance: N/A");
        }

        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(context, ConfirmBookingActivity.class);
            intent.putExtra("PARKING_ID", spot.getParkingId());
            intent.putExtra("VEHICLE_NUMBER", selectedVehicleRegNo);
            context.startActivity(intent);
        });

        // Set the click listener for the parking spot card
        holder.itemView.setOnClickListener(v -> {
            // Get the LatLng of the selected parking spot
            LatLng parkingLocation = new LatLng(spot.getLatitude(), spot.getLongitude());

            // Focus the map on the selected parking spot
            ((SearchParkingActivity) context).focusOnParkingSpot(parkingLocation);
        });
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }

    public static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvParkingName, tvMode, tvStatus, tvDistance;
        Button btnBook;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParkingName = itemView.findViewById(R.id.tvParkingName);
            tvMode = itemView.findViewById(R.id.tvMode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}