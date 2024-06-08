package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proform.model.User;
import com.google.firebase.auth.AuthCredential;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class setting extends AppCompatActivity {
    private EditText oldPassEditText, newPassEditText, repeatPassEditText;
    private MaterialButton changePassButton;
    private FirebaseAuth mAuth;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();

        oldPassEditText = findViewById(R.id.oldpass_id);
        newPassEditText = findViewById(R.id.password_id);
        repeatPassEditText = findViewById(R.id.passwordr_id);
        changePassButton = findViewById(R.id.btn_login);

        changePassButton.setOnClickListener(view -> changePassword());
        newPassEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (newPassEditText.getRight() - newPassEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        newPassEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        newPassEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    return true;
                }
            }
            return false;
        });
        repeatPassEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (repeatPassEditText.getRight() - repeatPassEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        repeatPassEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        repeatPassEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    return true;
                }
            }
            return false;
        });

    }

    private void changePassword() {
        String oldPassword = oldPassEditText.getText().toString();
        String newPassword = newPassEditText.getText().toString();
        String repeatPassword = repeatPassEditText.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(repeatPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.length() < 8) {
            oldPassEditText.setError("Password must be at least 8 characters");
            return;
        }

        if (newPassword.length() < 8) {
            newPassEditText.setError("Password must be at least 8 characters");
            return;
        }

        if (repeatPassword.length() < 8) {
            repeatPassEditText.setError("Password must be at least 8 characters");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!newPassword.equals(oldPassword)) {
                                if (newPassword.equals(repeatPassword)) {
                                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            updateUserPasswordInDatabase(user.getUid(), newPassword);
                                        } else {
                                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    repeatPassEditText.setError("New password and repeat password do not match");
                                }
                            } else {
                                newPassEditText.setError("New password must be different from the old password");
                            }
                        } else {
                            oldPassEditText.setError("Incorrect old password");
                        }
                    });
        }
    }

    private void updateUserPasswordInDatabase(String userId, String newPassword) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("password").setValue(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password changed successfully in Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to change password in Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    public void backhome(View view) {
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
                                Intent intent = new Intent(setting.this, home.class);
                                startActivity(intent);
                            } else if ("Chef".equals(currentUserRole)){
                                Intent intent = new Intent(setting.this, HomeChef.class);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(setting.this, HomeTransporter.class);
                                startActivity(intent);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(setting.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(setting.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
