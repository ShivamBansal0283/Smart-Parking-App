package com.example.parking_system;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhoneNumber, etPassword, etConfirmPassword;
    private Button btnRegister, btnVerifyOTP, btnUploadProfilePic;
    private TextView tvLogin;
    private RadioGroup radioGroupGender;
    private CheckBox checkboxTerms;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String selectedGender = "Not Specified"; // Default gender value
    private String profilePictureUrl = ""; // Default empty value for profile picture

    private String verificationId;  // Define verificationId at the class level
    private PhoneAuthProvider.ForceResendingToken resendToken;  // Define resendToken at the class level


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users"); // Reference to Firebase Realtime Database

        // Initialize UI elements
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnUploadProfilePic = findViewById(R.id.btnUploadProfilePic);
        tvLogin = findViewById(R.id.tvLogin);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        checkboxTerms = findViewById(R.id.checkboxTerms);

        // Gender Selection (Radio Buttons)
        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                selectedGender = "Male";
            } else if (checkedId == R.id.rbFemale) {
                selectedGender = "Female";
            } else if (checkedId == R.id.rbOther) {
                selectedGender = "Other";
            }
        });

        // Register Button Click
        btnRegister.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(name, email, phoneNumber, password, confirmPassword)) {
                if (verifyPhoneNumber(phoneNumber)) {
                    registerUser(email, password, name, phoneNumber); // Add phone number to registration
                } else {
                    Toast.makeText(RegisterActivity.this, "Phone number not verified!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigate to Login Page
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Profile Picture Button (Profile picture functionality to be added later)
        btnUploadProfilePic.setOnClickListener(v -> {
            // Handle profile picture upload (Use an image picker library later to implement this)
            Toast.makeText(RegisterActivity.this, "Upload Profile Picture clicked", Toast.LENGTH_SHORT).show();
        });

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                btnVerifyOTP.setVisibility(View.VISIBLE); // Re-enable OTP button when phone number changes
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnVerifyOTP.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
                Toast.makeText(RegisterActivity.this, "Enter a valid phone number!", Toast.LENGTH_SHORT).show();
                return;
            }

            sendOTP(phoneNumber); // Function to send OTP to the phone number
        });
    }
    private void sendOTP(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber("+91" + phoneNumber)       // Phone number to send OTP
                        .setTimeout(60L, TimeUnit.SECONDS)          // Timeout duration for OTP
                        .setActivity(RegisterActivity.this)        // Activity context
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                // Auto verification complete (you can automatically sign in)
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(RegisterActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                // OTP sent successfully
                                RegisterActivity.this.verificationId = verificationId;
                                RegisterActivity.this.resendToken = token;
                                showOTPDialog(); // Show OTP input dialog
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Enter OTP");

        // Create input fields
        final EditText otpInput = new EditText(RegisterActivity.this);
        otpInput.setHint("Enter OTP");
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(otpInput);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String otp = otpInput.getText().toString().trim();
            if (!otp.isEmpty()) {
                verifyOTP(otp);  // Function to verify OTP entered by the user
            } else {
                Toast.makeText(RegisterActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void verifyOTP(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // OTP verification successful, proceed with registration
                        btnVerifyOTP.setVisibility(View.GONE);  // Hide the verify button once OTP is validated
                        Toast.makeText(RegisterActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();

                        // You can now proceed with the rest of the registration process
                    } else {
                        Toast.makeText(RegisterActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Validate User Input
    private boolean validateInput(String name, String email, String phoneNumber, String password, String confirmPassword) {
        if (name.isEmpty()) {
            etFullName.setError("Enter your name!");
            etFullName.requestFocus();
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email!");
            etEmail.requestFocus();
            return false;
        }
        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            etPhoneNumber.setError("Enter a valid phone number!");
            etPhoneNumber.requestFocus();
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters!");
            etPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match!");
            etConfirmPassword.requestFocus();
            return false;
        }
        if (!checkboxTerms.isChecked()) {
            Toast.makeText(RegisterActivity.this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Verify Phone Number (Functionality to be added later)
    private boolean verifyPhoneNumber(String phoneNumber) {
        // This is a placeholder for OTP functionality
        // For now, we'll just return true to simulate successful OTP verification
        return true; // Simulate that the phone number is verified successfully
    }

    // Register User with Firebase and Save Name, Gender, Profile Picture, and Wallet Details
    private void registerUser(String email, String password, String name, String phoneNumber) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Store user details in Firebase Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                            userRef.child("name").setValue(name);
                            userRef.child("email").setValue(email);
                            userRef.child("phone").setValue(phoneNumber); // Save phone number
                            userRef.child("gender").setValue(selectedGender); // Save gender
                            userRef.child("profilePictureUrl").setValue(profilePictureUrl); // Save profile picture URL

                            // Add wallet details (with initial balance as 0.0)
                            userRef.child("wallet").child("balance").setValue(0.0);

                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


















