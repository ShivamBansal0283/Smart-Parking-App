package com.example.parking_system;


import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parking_system.R;
import com.example.parking_system.Transaction;

import java.util.Date;
import java.util.List;

// Adapter class for displaying transaction data
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.tvTransactionId.setText( transaction.getTransactionId());
        holder.tvforpayment.setText(transaction.getforpayment());
        holder.tvAmount.setText("₹" + transaction.getAmount());
        // Assuming transaction.getTimestamp() returns a timestamp in milliseconds
        long timestamp = transaction.getTimestamp();

        // Create a SimpleDateFormat object to format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Convert the timestamp into a Date object
        Date date = new Date(timestamp);

        // Format the Date object to a string
        String formattedDate = sdf.format(date);

        // Set the formatted date into the TextView
        holder.tvDate.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // ViewHolder class
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView tvTransactionId, tvAmount, tvforpayment, tvDate;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionIdValue);
            tvAmount = itemView.findViewById(R.id.tvAmountValue);
            tvforpayment = itemView.findViewById(R.id.tvforpaymentValue);
            tvDate = itemView.findViewById(R.id.tvDateValue);
        }
    }
}