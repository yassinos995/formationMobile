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
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser user;
    private String originalName;
    private String originalPhoneNumber;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            boolean isNameChanged = !nameEditText.getText().toString().equals(originalName);
            boolean isPhoneChanged = !phoneEditText.getText().toString().equals(originalPhoneNumber);
            edit.setEnabled(isNameChanged || isPhoneChanged);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        nameEditText = findViewById(R.id.id_nameProfile);
        emailEditText = findViewById(R.id.id_emailsProfile);
        phoneEditText = findViewById(R.id.id_phoneProfile);
        edit = findViewById(R.id.btn_edit_profile);
        cancel = findViewById(R.id.btn_CancelProfile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user = firebaseAuth.getCurrentUser();

        String userName = getIntent().getStringExtra("userName");
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

        nameEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);

        cancel.setOnClickListener(v -> navigateBasedOnRole());
    }

    private void populateUserProfile(User user, DatabaseReference userRef) {
        nameEditText.setText(user.getName());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhoneNumber());

        originalName = user.getName();
        originalPhoneNumber = user.getPhoneNumber();

        edit.setOnClickListener(v -> updateProfile(userRef));
    }

    private void updateProfile(DatabaseReference userRef) {
        String newName = nameEditText.getText().toString().trim();
        String newPhoneNumber = phoneEditText.getText().toString().trim();

        if (newName.isEmpty() || newPhoneNumber.isEmpty()) {
            showToast("Name and phone number cannot be empty.");
            return;
        }

        userRef.child("name").setValue(newName);
        userRef.child("phoneNumber").setValue(newPhoneNumber);

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
            case "Chef personnelle":
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
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUid);
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            String currentUserRole = currentUser.getPoste();
                            if ("Admin".equals(currentUserRole)) {
                                Intent intent = new Intent(Profil.this, home.class);

                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(Profil.this, HomeChef.class);
                                startActivity(intent);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Profil.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not authenticated
            Toast.makeText(Profil.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
