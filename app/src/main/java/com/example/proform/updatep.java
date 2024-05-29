package com.example.proform;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proform.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class updatep extends AppCompatActivity {
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button updateButton;
    private Button cancelButton;
    private DatabaseReference userRef;
    private User originalUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatep);

        nameEditText = findViewById(R.id.id_nameProfileU);
        emailEditText = findViewById(R.id.id_emailsProfileU);
        phoneEditText = findViewById(R.id.id_phoneProfileU);
        updateButton = findViewById(R.id.btn_edit_profileU);
        cancelButton = findViewById(R.id.btn_CancelProfileU);

        originalUser = (User) getIntent().getSerializableExtra("user");
        if (originalUser != null) {
            nameEditText.setText(originalUser.getName());
            emailEditText.setText(originalUser.getEmail());
            phoneEditText.setText(originalUser.getPhoneNumber());
        }

        updateButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed while text is being changed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Enable the update button if any changes are detected
                updateButton.setEnabled(hasUserChanged());
            }
        };

        nameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);

        updateButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newPhoneNumber = phoneEditText.getText().toString().trim();

            User userToUpdate = (User) getIntent().getSerializableExtra("user");
            if (userToUpdate != null) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                Query userQuery = usersRef.orderByChild("cin").equalTo(userToUpdate.getCin()).limitToFirst(1);
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userUid = userSnapshot.getKey(); // Retrieve the UID from the snapshot
                            userRef = usersRef.child(userUid);
                            userRef.child("name").setValue(newName);
                            userRef.child("email").setValue(newEmail);
                            userRef.child("phoneNumber").setValue(newPhoneNumber)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("UpdateDebug", "User profile updated successfully");
                                            Toast.makeText(updatep.this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Log.e("UpdateDebug", "Failed to update user profile: " + task.getException());
                                            Toast.makeText(updatep.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(updatep.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(updatep.this, listemp.class);
            startActivity(intent);
        });
    }

    private boolean hasUserChanged() {
        String currentName = nameEditText.getText().toString().trim();
        String currentEmail = emailEditText.getText().toString().trim();
        String currentPhoneNumber = phoneEditText.getText().toString().trim();

        return !currentName.equals(originalUser.getName()) ||
                !currentEmail.equals(originalUser.getEmail()) ||
                !currentPhoneNumber.equals(originalUser.getPhoneNumber());
    }
}