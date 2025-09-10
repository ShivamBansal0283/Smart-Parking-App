
package com.example.parking_system;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

public class WalletActivity extends AppCompatActivity implements PaymentResultListener {

    private TextView tvWalletBalance;
    private EditText etAmount;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable Back Button
        getSupportActionBar().setTitle("Wallet"); // Set Toolbar Title

        // Initialize Razorpay Checkout
        Checkout.preload(getApplicationContext());

        // Initialize Firebase User and Database Reference
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        }

        // Initialize UI Elements
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        etAmount = findViewById(R.id.etAmount);

        // Fetch Wallet Details
        fetchWalletDetails();

        // Add Money Button Click
        findViewById(R.id.btnAddMoney).setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                double amountToAdd = Double.parseDouble(amountStr);
                if (amountToAdd > 0) {
                    startRazorpayPayment(amountToAdd);
                } else {
                    Toast.makeText(WalletActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WalletActivity.this, "Amount cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void fetchWalletDetails() {
        userRef.child("wallet").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double balance = snapshot.child("balance").getValue(Double.class);
                    tvWalletBalance.setText(String.format("Balance: ₹ %.2f", balance));
                } else {
                    tvWalletBalance.setText("Balance: ₹0.00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WalletActivity.this, "Failed to fetch wallet data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRazorpayPayment(double amountToAdd) {
        // Prepare Razorpay payment details
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Parking System");
            options.put("description", "Add money to wallet");
            options.put("currency", "INR");
            options.put("amount", amountToAdd * 100);  // Razorpay accepts amount in paise (1 INR = 100 paise)
            options.put("prefill.email", currentUser.getEmail());
            options.put("prefill.contact", "1234567890"); // Use user's phone number

            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_B52gKGUK2XVJrF"); // Replace with your Razorpay Key ID

            checkout.open(WalletActivity.this, options);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in starting Razorpay payment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        // Payment successful, update wallet
        Toast.makeText(this, "Payment Successful: " + paymentId, Toast.LENGTH_SHORT).show();
        double amountToAdd = Double.parseDouble(etAmount.getText().toString());
        addMoneyToWallet(amountToAdd, paymentId);
    }

    @Override
    public void onPaymentError(int code, String message) {
        // Payment failed, show error
        Toast.makeText(this, "Payment Failed: " + message, Toast.LENGTH_SHORT).show();
    }

    private void addMoneyToWallet(double amountToAdd, String paymentId) {
        userRef.child("wallet").child("balance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Double currentBalance = task.getResult().getValue(Double.class);
                if (currentBalance != null) {
                    double newBalance = currentBalance + amountToAdd;

                    // Update the wallet balance
                    userRef.child("wallet").child("balance").setValue(newBalance)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(WalletActivity.this, "Money added successfully!", Toast.LENGTH_SHORT).show();
                                    tvWalletBalance.setText("Balance: ₹" + newBalance);

                                    // Now store the transaction details
                                    storeTransactionDetails(paymentId, amountToAdd, "Wallet");
                                } else {
                                    Toast.makeText(WalletActivity.this, "Failed to add money", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Toast.makeText(WalletActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeTransactionDetails(String paymentId, double amount, String newBalance) {
        // Create a new transaction record
        String transactionId = userRef.child("transactions").push().getKey();
        if (transactionId != null) {
            DatabaseReference transactionRef = userRef.child("transactions").child(transactionId);

            // Create a transaction object
            transactionRef.setValue(new Transaction(transactionId, amount, newBalance, paymentId, System.currentTimeMillis()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(WalletActivity.this, "Transaction saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WalletActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Transaction Model Class

}