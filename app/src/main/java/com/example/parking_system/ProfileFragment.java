
package com.example.parking_system;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvUsername, tvEmail, tvPhone, tvDOB, tvGender, tvAddressStreet, tvAddressPincode, tvAddressState, tvLicense, tvWalletBalance;
    private ImageView ivProfilePicture, ivEditEmail, ivEditPhone, ivEditDOB, ivEditGender, ivEditAddressStreet, ivEditAddressPincode, ivEditAddressState, ivEditDriverLicense;
    private Button btnChangePassword,  btnDeleteAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        }

        // Initialize Views
        tvName = view.findViewById(R.id.tvName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvDOB = view.findViewById(R.id.tvDOB);
        tvGender = view.findViewById(R.id.tvGender);
        tvAddressStreet = view.findViewById(R.id.tvAddressStreet);
        tvAddressPincode = view.findViewById(R.id.tvAddressPincode);
        tvAddressState = view.findViewById(R.id.tvAddressState);
        tvLicense = view.findViewById(R.id.tvLicense);
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        ivEditEmail = view.findViewById(R.id.ivEditEmail);
        ivEditPhone = view.findViewById(R.id.ivEditPhone);
        ivEditDOB = view.findViewById(R.id.ivEditDOB);
        ivEditGender = view.findViewById(R.id.ivEditGender);
        ivEditAddressStreet = view.findViewById(R.id.ivEditAddressStreet);
        ivEditAddressPincode = view.findViewById(R.id.ivEditAddressPincode);
        ivEditAddressState = view.findViewById(R.id.ivEditAddressState);
        ivEditDriverLicense = view.findViewById(R.id.ivEditDriverLicense);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);
//        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        // Load User Data
        loadUserData();

        // Set Edit Profile Listeners
        setEditListeners();

        // Set Button Listeners
        setButtonListeners();

        return view;
    }

    private void loadUserData() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Fetch user data from Firebase
                    String name = task.getResult().child("name").getValue(String.class);
                    String email = task.getResult().child("email").getValue(String.class);
                    String phone = task.getResult().child("phone").getValue(String.class);
                    String gender = task.getResult().child("gender").getValue(String.class);
                    String profilePictureUrl = task.getResult().child("profilePictureUrl").getValue(String.class);
                    String walletBalance = String.valueOf(task.getResult().child("wallet").child("balance").getValue(Double.class));

                    // Set profile data into TextViews
                    tvName.setText(name != null ? name : "");
                    tvUsername.setText("@" + (name != null ? name.split(" ")[0] : ""));
                    tvEmail.setText(email != null ? email : "");
                    tvPhone.setText(phone != null ? phone : "");
                    tvGender.setText(gender != null ? gender : "");
                    tvWalletBalance.setText(walletBalance != null ? "$" + walletBalance : "$0.00");


                        // Optionally set a default placeholder if the URL is not available
                        ivProfilePicture.setImageResource(R.drawable.placeholder_profile);

                    // Load address and driver's license if available
                    loadAddressAndLicenseData();
                } else {
                    Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadAddressAndLicenseData() {
        // Load address if available
        userRef.child("address").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String street = task.getResult().child("street").getValue(String.class);
                String pincode = task.getResult().child("pincode").getValue(String.class);
                String state = task.getResult().child("state").getValue(String.class);

                // Set address data into TextViews
                tvAddressStreet.setText(street != null ? street : "");
                tvAddressPincode.setText(pincode != null ? pincode : "");
                tvAddressState.setText(state != null ? state : "");
            }
            else {
                tvAddressStreet.setText("");
                tvAddressPincode.setText("");
                tvAddressState.setText("");
            }
        });

        // Load driver's license if available
        userRef.child("driverLicense").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String license = task.getResult().getValue(String.class);
                tvLicense.setText(license != null ? license : "");
            }
            else {
                tvLicense.setText("");
            }
        });
    }

    private void setEditListeners() {
        // Edit email
        ivEditEmail.setOnClickListener(v -> editField("Email", tvEmail));

        // Edit phone
        ivEditPhone.setOnClickListener(v -> editField("Phone", tvPhone));

        // Edit date of birth
        ivEditDOB.setOnClickListener(v -> editField("DOB", tvDOB));

        // Edit gender
        ivEditGender.setOnClickListener(v -> editField("Gender", tvGender));

        // Edit address street
        ivEditAddressStreet.setOnClickListener(v -> editField("Street", tvAddressStreet));

        // Edit address pincode
        ivEditAddressPincode.setOnClickListener(v -> editField("Pincode", tvAddressPincode));

        // Edit address state
        ivEditAddressState.setOnClickListener(v -> editField("State", tvAddressState));

        // Edit driver license
        ivEditDriverLicense.setOnClickListener(v -> editField("Driver’s License", tvLicense));
    }

    private void editField(String field, TextView textView) {
        String currentValue = textView.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit " + field);

        // Create a layout to hold the input field or dropdown
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        switch (field) {

            case "Street":
            case "Driver’s License":
                // EditText for text-based fields
                EditText input = new EditText(getContext());
                input.setText(currentValue);
                layout.addView(input);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString();
                    if (newValue.isEmpty()) {
                        Toast.makeText(getContext(), field + " cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserData(field, newValue);
                    }
                });
                break;
            case "Phone":
                // EditText for phone number
                EditText phoneInput = new EditText(getContext());
                phoneInput.setInputType(InputType.TYPE_CLASS_PHONE); // restrict input to phone numbers
                phoneInput.setText(currentValue);
                layout.addView(phoneInput);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String phoneValue = phoneInput.getText().toString();
                    if (phoneValue.isEmpty() || phoneValue.length() < 10) {
                        Toast.makeText(getContext(), "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserData(field, phoneValue);
                    }
                });
                break;
            case "Email":
                // EditText for email field with validation
                EditText emailInput = new EditText(getContext());
                emailInput.setText(currentValue);
                emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); // Restrict input to email format
                layout.addView(emailInput);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String newEmail = emailInput.getText().toString();
                    if (newEmail.isEmpty()) {
                        Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        Toast.makeText(getContext(), "Enter a valid email", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserData(field, newEmail);
                    }
                });
                break;

            case "Pincode":
                // EditText for Pincode
                EditText pincodeInput = new EditText(getContext());
                pincodeInput.setInputType(InputType.TYPE_CLASS_NUMBER); // restrict input to numbers
                pincodeInput.setText(currentValue);
                layout.addView(pincodeInput);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String pincodeValue = pincodeInput.getText().toString();
                    if (pincodeValue.isEmpty() || pincodeValue.length() != 6) {
                        Toast.makeText(getContext(), "Enter a valid Pincode", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserData(field, pincodeValue);
                    }
                });
                break;


            case "Gender":
                // Spinner for Gender selection
                Spinner genderSpinner = new Spinner(getContext());
                ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Not Specified"});
                genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                genderSpinner.setAdapter(genderAdapter);
                int genderPosition = genderAdapter.getPosition(currentValue);
                genderSpinner.setSelection(genderPosition != -1 ? genderPosition : 2); // Default to "Not Specified" if not found
                layout.addView(genderSpinner);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String newGender = genderSpinner.getSelectedItem().toString();
                    updateUserData(field, newGender);
                });
                break;

            case "DOB":
                // DatePicker for DOB
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, monthOfYear, dayOfMonth) -> {
                    String newDOB = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                    updateUserData(field, newDOB);
                }, 1990, 0, 1);
                datePickerDialog.show();
                return; // Don't show a dialog, we are using DatePickerDialog for DOB

            case "State":
                // Spinner for State selection (using a predefined list of states in India)
                Spinner stateSpinner = new Spinner(getContext());
                ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getStatesList());
                stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(stateAdapter);
                layout.addView(stateSpinner);
                builder.setView(layout);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String newState = stateSpinner.getSelectedItem().toString();
                    updateUserData(field, newState);
                });
                break;
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Function to get a list of all Indian states
    private List<String> getStatesList() {
        List<String> states = new ArrayList<>();
        // Populate this list with all Indian states
        states.add("Andhra Pradesh");
        states.add("Arunachal Pradesh");
        states.add("Assam");
        states.add("Bihar");
        states.add("Chhattisgarh");
        states.add("Goa");
        states.add("Gujarat");
        states.add("Haryana");
        states.add("Himachal Pradesh");
        states.add("Jharkhand");
        states.add("Karnataka");
        states.add("Kerala");
        states.add("Madhya Pradesh");
        states.add("Maharashtra");
        states.add("Manipur");
        states.add("Meghalaya");
        states.add("Mizoram");
        states.add("Nagaland");
        states.add("Odisha");
        states.add("Punjab");
        states.add("Rajasthan");
        states.add("Sikkim");
        states.add("Tamil Nadu");
        states.add("Telangana");
        states.add("Tripura");
        states.add("Uttar Pradesh");
        states.add("Uttarakhand");
        states.add("West Bengal");
        states.add("Andaman and Nicobar Islands");
        states.add("Chandigarh");
        states.add("Dadra and Nagar Haveli and Daman and Diu");
        states.add("Lakshadweep");
        states.add("Delhi");
        states.add("Puducherry");

        return states;
    }

    private void updateUserData(String field, String newValue) {
        if (userRef != null) {
            switch (field) {
                case "Email":
                    userRef.child("email").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvEmail.setText(newValue);
                            Toast.makeText(getContext(), "Email updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Email", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "Phone":
                    userRef.child("phone").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvPhone.setText(newValue);
                            Toast.makeText(getContext(), "Phone number updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Phone", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "DOB":
                    userRef.child("dob").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvDOB.setText(newValue);
                            Toast.makeText(getContext(), "Date of Birth updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update DOB", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "Gender":
                    userRef.child("gender").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvGender.setText(newValue);
                            Toast.makeText(getContext(), "Gender updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Gender", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "Street":
                    userRef.child("address").child("street").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvAddressStreet.setText(newValue);
                            Toast.makeText(getContext(), "Street updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Street", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "Pincode":
                    userRef.child("address").child("pincode").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvAddressPincode.setText(newValue);
                            Toast.makeText(getContext(), "Pincode updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Pincode", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "State":
                    userRef.child("address").child("state").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvAddressState.setText(newValue);
                            Toast.makeText(getContext(), "State updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update State", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case "Driver’s License":
                    userRef.child("driverLicense").setValue(newValue).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tvLicense.setText(newValue);
                            Toast.makeText(getContext(), "Driver's License updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update Driver's License", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                // Additional cases can be added for other fields (if any)
            }
        }
    }
    private void setButtonListeners() {
        btnChangePassword.setOnClickListener(v -> {
            // Show dialog to get old password and new password from the user
            showChangePasswordDialog();
        });


        btnDeleteAccount.setOnClickListener(v -> {
            // Get the current user
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                String userId = user.getUid();

                // Reference to the user in the Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                // Delete user data from Firebase Realtime Database first
                userRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Data is deleted from the Realtime Database, now delete the user's authentication data
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                Toast.makeText(getContext(), "Account and data deleted successfully", Toast.LENGTH_SHORT).show();

                                // Optionally, sign out and redirect to the login page
                                mAuth.signOut();
                                startActivity(new Intent(getContext(), LoginActivity.class)); // Navigate to login page
                                getActivity().finish(); // Close the current activity
                            } else {
                                Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to delete user data from the database", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showChangePasswordDialog() {
        // Create an AlertDialog to get the old password, new password, and confirm new password from the user
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Password");

        // Create a layout for the dialog with EditTexts for old password, new password, and confirm new password
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText oldPasswordInput = new EditText(getContext());
        oldPasswordInput.setHint("Enter Old Password");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        final EditText newPasswordInput = new EditText(getContext());
        newPasswordInput.setHint("Enter New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(getContext());
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if(newPassword.length()<6)
            {
                Toast.makeText(getContext(), "password must be atleast 6 letters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reauthenticate with old password
            reauthenticateAndChangePassword(oldPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void reauthenticateAndChangePassword(String oldPassword, String newPassword) {
        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the user's credentials (email and old password)
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            // Reauthenticate the user
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Old password matches, now update to new password
                            updatePassword(newPassword);
                        } else {
                            // Old password is incorrect
                            Toast.makeText(getContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword(String newPassword) {
        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Update the user's password
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Password updated successfully
                            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                            // Log out the user
                            FirebaseAuth.getInstance().signOut();

                            // Redirect to login page after logging out
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();  // Close the current fragment/activity
                        } else {
                            // Failed to update the password
                            Toast.makeText(getContext(), "Failed to change password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}