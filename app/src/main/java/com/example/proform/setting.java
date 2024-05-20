package com.example.proform;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class setting extends AppCompatActivity {
    private EditText oldPassEditText, newPassEditText, repeatPassEditText;
    private MaterialButton changePassButton;
    private FirebaseAuth mAuth;

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
    }

    private void changePassword() {
        String oldPassword = oldPassEditText.getText().toString();
        String newPassword = newPassEditText.getText().toString();
        String repeatPassword = repeatPassEditText.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(repeatPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
                                            // Clear fields or redirect to another screen
                                        } else {
                                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(this, "New password and repeat password do not match", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "New password must be different from the old password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Incorrect old password", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    }
