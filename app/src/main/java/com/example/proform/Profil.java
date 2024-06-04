package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proform.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profil extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText;
    private Button edit, cancel;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private User originalUser;
    private String originalName, originalPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        nameEditText = findViewById(R.id.id_nameProfile);
        emailEditText = findViewById(R.id.id_emailsProfile);
        phoneEditText = findViewById(R.id.id_phoneProfile);
        edit = findViewById(R.id.btn_edit_profile);
        cancel = findViewById(R.id.btn_CancelProfile);

        // Get user information from Intent
        String userName = getIntent().getStringExtra("userName");

        // Get user data from Firebase
        DatabaseReference usersRef = firebaseDatabase.getReference().child("users");
        usersRef.orderByChild("name").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            populateUserProfile(user, snapshot.getRef());
                        } else {
                            showToast("User information is incomplete.");
                        }
                    }
                } else {
                    showToast("User information not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error retrieving user information.");
            }
        });

        // Set up TextWatcher for EditText fields
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                edit.setEnabled(hasUserChanged());
            }
        };

        nameEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);

        // Set up click listener for Cancel button
        cancel.setOnClickListener(v -> navigateBasedOnRole());
    }

    private boolean hasUserChanged() {
        // Check if originalUser is null or if any field has changed
        if (originalUser == null) {
            return false;
        }

        String currentName = nameEditText.getText().toString().trim();
        String currentEmail = emailEditText.getText().toString().trim();
        String currentPhoneNumber = phoneEditText.getText().toString().trim();

        return !currentName.equals(originalUser.getName()) ||
                !currentEmail.equals(originalUser.getEmail()) ||
                !currentPhoneNumber.equals(originalUser.getPhoneNumber());
    }

    private void populateUserProfile(User user, DatabaseReference userRef) {
        // Populate the EditText fields with user data
        nameEditText.setText(user.getName());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhoneNumber());

        // Store original user data for comparison
        originalUser = user;
        originalName = user.getName();
        originalPhoneNumber = user.getPhoneNumber();

        // Set up click listener for the Edit button
        edit.setOnClickListener(v -> updateProfile(userRef));
    }

    private void updateProfile(DatabaseReference userRef) {
        // Get the updated values from the EditText fields
        String newName = nameEditText.getText().toString().trim();
        String newPhoneNumber = phoneEditText.getText().toString().trim();

        // Validate input fields
        if (newName.isEmpty() || newPhoneNumber.isEmpty()) {
            showToast("Name and phone number cannot be empty.");
            return;
        }

        // Update the user data in Firebase
        userRef.child("name").setValue(newName);
        userRef.child("phoneNumber").setValue(newPhoneNumber);

        // Navigate to the appropriate home screen based on user role
        navigateBasedOnRole();
    }

    private void navigateBasedOnRole() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showToast("User not authenticated.");
            return;
        }

        String currentUserUid = currentUser.getUid();
        DatabaseReference currentUserRef = firebaseDatabase.getReference().child("users").child(currentUserUid);

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    navigateToHomeScreen(currentUser.getPoste());
                } else {
                    showToast("Error retrieving user role.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }

    private void navigateToHomeScreen(String role) {
        Intent intent;
        switch (role) {
            case "Admin":
                intent = new Intent(this, home.class);
                break;
            case "Chef":
                intent = new Intent(this, HomeChef.class);
                break;
            default:
                intent = new Intent(this, HomeTransporter.class);
                break;
        }
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(Profil.this, message, Toast.LENGTH_SHORT).show();
    }

    public void backHome(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            DatabaseReference currentUserRef = firebaseDatabase.getReference().child("users").child(currentUserUid);

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        navigateToHomeScreen(user.getPoste());
                    } else {
                        showToast("Error retrieving user role.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Error: " + databaseError.getMessage());
                }
            });
        } else {
            showToast("User not authenticated.");
        }
    }
}
