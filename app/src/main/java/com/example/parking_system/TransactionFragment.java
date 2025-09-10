package com.example.parking_system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class TransactionFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private DatabaseReference transactionsRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        // Initialize Firebase User and Database Reference
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            transactionsRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentUser.getUid()).child("transactions");
        }

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

//         Fetch Transaction Details
        fetchTransactions();

        return view;
    }

    private void fetchTransactions() {
    // Get a reference to the 'transactions' node for the current user
        if (transactionsRef != null) {
            transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    transactionList.clear();  // Clear any existing data in the list
                    for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                        // Deserialize the transaction object
                        Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            // Add the transaction to the list
                            transactionList.add(transaction);
                        }
                    }
                    // Notify the adapter that the data has changed
                    transactionAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch transactions", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}