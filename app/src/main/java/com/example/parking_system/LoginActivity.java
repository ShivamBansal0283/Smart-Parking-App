
package com.example.parking_system;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvCreateAccount;
    private LottieAnimationView carLoadingAnimation;  // Lottie animation view for car loading

    private FirebaseAuth mAuth;


    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Initialize SharedPreferences for session management
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // If user is already logged in, redirect to DashboardActivity
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return; // Stop further execution
        }

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        carLoadingAnimation = findViewById(R.id.carLoadingAnimation);  // Initialize Lottie animation

        // Hide the loading animation initially
        carLoadingAnimation.setVisibility(View.GONE);




        //Handle Password Visibility Toggle (using the eye icon)
        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableStart = etPassword.getCompoundDrawablesRelative()[2] != null ? etPassword.getCompoundDrawablesRelative()[2].getBounds().width() : 0;

                // Check if the touch was on the eye icon
                if (event.getRawX() >= (etPassword.getRight() - drawableStart)) {
                    if (etPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                        // Show password
                        etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        etPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_closed, 0);
                    } else {
                        // Hide password
                        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        etPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_open, 0);
                    }
                    return true;
                }
            }
            return false;
        });

        // Login Button Click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                carLoadingAnimation.setVisibility(View.VISIBLE);  // Show the car loading animation
                carLoadingAnimation.playAnimation();  // Show the car loading animation
                loginUser(email, password);
            }
        });

        // Inside onCreate or onClickListener for Forgot Password button
        tvForgotPassword.setOnClickListener(v -> {
            // Show dialog to enter email
            showForgotPasswordDialog();
        });

        // Redirect to Register Page
        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

    }
    private void showForgotPasswordDialog() {
        // Create a new dialog to get the email address
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Forgot Password");

        // Create a layout with a single EditText to enter the email address
        LinearLayout layout = new LinearLayout(LoginActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText emailInput = new EditText(LoginActivity.this);
        emailInput.setHint("Enter your email");
        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailInput);

        builder.setView(layout);

        // Positive button: Send reset email
        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter an email address", Toast.LENGTH_SHORT).show();
            } else {
                // Validate email format
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    checkEmailAndSendResetLink(email); // Check email and send reset link
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void checkEmailAndSendResetLink(String email) {
        // Check if email exists in Firebase Database under the Users node
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email exists in the database, send password reset link
                    sendPasswordResetEmail(email);
                } else {
                    // Email not found
                    Toast.makeText(LoginActivity.this, "Email not registered in our system", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Error checking email: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        // Use Firebase Authentication to send the password reset email
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Validate Email and Password Fields
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email!");
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters!");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    // Login User with Firebase Authentication
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    carLoadingAnimation.setVisibility(View.GONE);  // Hide loading animation
                    carLoadingAnimation.cancelAnimation();  // Stop the animation
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Save login session
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Redirect to Dashboard
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } else {
                            Toast.makeText(LoginActivity.this, "User does not exist!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}