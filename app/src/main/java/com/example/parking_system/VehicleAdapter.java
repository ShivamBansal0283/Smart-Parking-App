package com.example.parking_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;

    public VehicleAdapter(Context context, List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.tvVehicleNumber.setText("Number: " + vehicle.getVehicleNumber());
        holder.tvVehicleModel.setText("Model: " + vehicle.getVehicleModel());
        holder.tvVehicleType.setText("Type: " + vehicle.getVehicleType());
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleNumber, tvVehicleModel, tvVehicleType;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleNumber = itemView.findViewById(R.id.tvVehicleNumber);
            tvVehicleModel = itemView.findViewById(R.id.tvVehicleModel);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
        }
    }
}