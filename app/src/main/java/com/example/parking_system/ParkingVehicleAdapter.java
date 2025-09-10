
package com.example.parking_system;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ParkingVehicleAdapter extends RecyclerView.Adapter<ParkingVehicleAdapter.VehicleViewHolder> {

    private Context context;
    private List<Vehicle> vehicleList;
    private OnVehicleActionListener searchListener, scanListener, finishListener, navigateListener;
    private Handler handler = new Handler();

    private OnVehicleActionListener cancelBookingListener;  // Add this
    private OnVehicleActionListener startParkingListener;  // Add this


    public ParkingVehicleAdapter(Context context, List<Vehicle> vehicleList,
                                 OnVehicleActionListener searchListener, OnVehicleActionListener scanListener,
                                 OnVehicleActionListener finishListener, OnVehicleActionListener navigateListener,
                                 OnVehicleActionListener cancelBookingListener,OnVehicleActionListener startParkingListener) {
        this.context = context;
        this.vehicleList = vehicleList;
        this.searchListener = searchListener;
        this.scanListener = scanListener;
        this.finishListener = finishListener;
        this.navigateListener = navigateListener;
        this.cancelBookingListener = cancelBookingListener;   // Set the listener
        this.startParkingListener = startParkingListener;   // Set the listener

    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.tvVehicleNumber.setText("Reg No: " + vehicle.getVehicleNumber());


        if (vehicle.getParking() == null) {
            // Vehicle is NOT parked
            holder.btnSearchParking.setVisibility(View.VISIBLE);
            holder.btnScanQR.setVisibility(View.VISIBLE);
            holder.tvTimer.setVisibility(View.GONE);
            holder.btnFinish.setVisibility(View.GONE);
            holder.btnDirections.setVisibility(View.GONE);
            holder.tvBooked.setVisibility(View.GONE);
            holder.btnCancelBooking.setVisibility(View.GONE);// Hide cancel button
            holder.btnStartParking.setVisibility(View.GONE);


            // Set listeners for search and scan
            holder.btnSearchParking.setOnClickListener(v -> searchListener.onAction(vehicle));
            holder.btnScanQR.setOnClickListener(v -> scanListener.onAction(vehicle));
        } else {
            // Vehicle is parked
            holder.btnSearchParking.setVisibility(View.GONE);
            holder.btnScanQR.setVisibility(View.GONE);
            holder.btnFinish.setVisibility(View.VISIBLE);
            holder.btnDirections.setVisibility(View.VISIBLE);
            holder.tvTimer.setVisibility(View.VISIBLE);
            holder.tvBooked.setVisibility(View.GONE);
            holder.btnStartParking.setVisibility(View.GONE);



            // Set listeners for finish and directions
            holder.btnFinish.setOnClickListener(v -> finishListener.onAction(vehicle));
            holder.btnDirections.setOnClickListener(v -> navigateListener.onAction(vehicle));

            // Start Timer
            startTimer(holder.tvTimer, vehicle.getStartTime());

            // Check if start time is in the future and handle the button state
            long remainingTime = vehicle.getReserved()-System.currentTimeMillis();
            if (remainingTime > 0) {
                // Format the startTime to a readable time string in the format "HH:mm a" (e.g., "10:00 PM")
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String formattedTime = sdf.format(new Date(vehicle.getReserved())); // Convert milliseconds to date and format

                holder.btnFinish.setVisibility(View.GONE);
                holder.tvBooked.setVisibility(View.VISIBLE);
                holder.tvTimer.setVisibility(View.GONE);
                holder.tvBooked.setText("Booked for " + formattedTime);

                // Show cancel booking button
                holder.btnCancelBooking.setVisibility(View.VISIBLE);

                // Set listener for cancel booking button
                holder.btnCancelBooking.setOnClickListener(v -> cancelBookingListener.onAction(vehicle));
            } else {
                holder.btnFinish.setVisibility(View.VISIBLE);
                holder.tvBooked.setVisibility(View.GONE);
                holder.tvTimer.setVisibility(View.VISIBLE);
                holder.btnCancelBooking.setVisibility(View.GONE);  // Hide cancel button when start time has passed
            }

            if(remainingTime < 0 && vehicle.getStartTime()==0){
                holder.btnStartParking.setVisibility(View.VISIBLE);
                holder.btnStartParking.setOnClickListener(v -> startParkingListener.onAction(vehicle));
                holder.tvTimer.setVisibility(View.GONE);
                holder.btnFinish.setVisibility(View.GONE);
                holder.btnCancelBooking.setVisibility(View.VISIBLE);
                holder.btnCancelBooking.setOnClickListener(v -> cancelBookingListener.onAction(vehicle));


            }
            else if(remainingTime <0 && vehicle.getStartTime()>0){
                holder.btnStartParking.setVisibility(View.GONE);
                holder.tvTimer.setVisibility(View.VISIBLE);
                holder.btnFinish.setVisibility(View.VISIBLE);
                holder.btnCancelBooking.setVisibility(View.GONE);


            }
        }
    }

    private void startTimer(TextView timerView, long startTime) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
                timerView.setText(String.format(Locale.getDefault(), "Time Parked: %02d:%02d:%02d", hours, minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleNumber, tvTimer,tvBooked;
        Button btnSearchParking, btnScanQR, btnFinish, btnDirections,btnCancelBooking,btnStartParking;


        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleNumber = itemView.findViewById(R.id.tvVehicleNumber);
            btnSearchParking = itemView.findViewById(R.id.btnSearchParking);
            btnScanQR = itemView.findViewById(R.id.btnScanQR);
            btnFinish = itemView.findViewById(R.id.btnFinish);
            btnDirections = itemView.findViewById(R.id.btnDirections);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            tvBooked=itemView.findViewById(R.id.tvBooked);
            btnCancelBooking=itemView.findViewById(R.id.btnCancelBooking);
            btnStartParking=itemView.findViewById(R.id.btnStartParking);
        }

    }

    public interface OnVehicleActionListener {
        void onAction(Vehicle vehicle);
    }
}