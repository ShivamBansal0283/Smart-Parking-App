package com.example.parking_system;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private TextView tvUserName, tvUserEmail;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🛠 Access Header Elements
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

        // 🛠 Update Header with Firebase User Data
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        tvUserName.setText("Welcome, " + name);
                    } else {
                        tvUserName.setText("Welcome, User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DashboardActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // 🛠 Update Header with Firebase User Data
        if (user != null) {
            tvUserEmail.setText(user.getEmail());  // ✅ Display email from FirebaseAuth

            userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        tvUserName.setText("Welcome, " + name);
                    } else {
                        tvUserName.setText("Welcome, User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DashboardActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tvUserEmail.setText("No Email Found");
        }
        // **🛠 Handle Fragment Redirection**
        if (getIntent().hasExtra("open_fragment")) {
            String fragmentToOpen = getIntent().getStringExtra("open_fragment");
            if ("ParkingFragment".equals(fragmentToOpen)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ParkingFragment()).commit();
            }
        }
        else {
            // Open HomeFragment by default if no fragment specified
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("open_fragment")) {
            String fragmentToOpen = getIntent().getStringExtra("open_fragment");
            if ("ParkingFragment".equals(fragmentToOpen)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ParkingFragment()).commit();
                getIntent().removeExtra("open_fragment"); // Prevent reloading on subsequent resumes
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();  // Store item ID in a variable

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (id == R.id.nav_vehicles) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyVehiclesFragment()).commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else if (id == R.id.nav_parking) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ParkingFragment()).commit();
        } else if (id == R.id.nav_addparkingspot) {
            Intent intent = new Intent(this, AddParkingActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_history) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment()).commit();
        }else if (id == R.id.nav_wallet) { // Add this case
            // Open the Wallet Activity or Fragment
            Intent intent = new Intent(DashboardActivity.this, WalletActivity.class); // New activity for wallet
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {

        if (sharedPreferences != null) { // Ensure sharedPreferences is initialized
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
        }

        // 🔹 Sign out from Firebase
        mAuth.signOut();

        // 🔹 Ensure the user is logged out before proceeding
        if (mAuth.getCurrentUser() == null) {
            // Redirect to LoginActivity and clear all activities in the stack
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close DashboardActivity
        }
    }
}











