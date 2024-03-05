package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proform.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sign_up extends AppCompatActivity {
    MaterialButton btn_s;
    private EditText nameEditText,emailEditText,passwordEditText,repeatPasswordEditText,phoneNumberEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference DatabaseReference;
    private static final String mail_regex="^[A-Za-z0-9+_.-]+@(.+)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.id_name); // Change to the appropriate ID
        emailEditText = findViewById(R.id.id_emails); // Change to the appropriate ID
        passwordEditText = findViewById(R.id.id_passwords); // Change to the appropriate ID
        repeatPasswordEditText = findViewById(R.id.id_passwordr); // Change to the appropriate ID
        phoneNumberEditText = findViewById(R.id.id_phone); // Change to the appropriate ID
        btn_s=findViewById(R.id.btn_s); // Change to the appropriate ID

        DatabaseReference= FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        btn_s.setOnClickListener(v -> {
            signUpUser();
        } );
    }

    public void goToLogins() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void signUpUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (validate(name, email, password, repeatPassword, phoneNumber)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendEmailVerification(name, email, phoneNumber);
                } else {
                    // Handle sign-up failure
                    Toast.makeText(sign_up.this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    // Log the exception message for debugging
                    Log.e("SignUpError", "Error: " + task.getException().getMessage());
                }
            });
        }
    }


    private void sendEmailVerification(String name, String email, String phoneNumber) {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            loggedUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserData(name, email, phoneNumber);
                    Toast.makeText(this, "Registration done! Please check your email address.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(sign_up.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendUserData(String name, String email, String phoneNumber) {
        DatabaseReference usersRef = DatabaseReference.child("users");
        User user = new User(name, email, phoneNumber);
        usersRef.child(""+firebaseAuth.getUid()).setValue(user);
    }

    private boolean isValidEmail(String email) {
        Pattern pattern=Pattern.compile(mail_regex);
        Matcher matcher= pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validate(String name, String email, String password, String repeatPassword, String phoneNumber) {
        if (name.isEmpty() || name.length() < 4) {
            nameEditText.setError(" name is invalid");
            return false;
        } else if (!isValidEmail(email)) {
            emailEditText.setError("Email is invalid");
            return false;
        } else if (phoneNumber.length() != 8) {
            phoneNumberEditText.setError("Phone is invalid");
            return false;
        } else if (password.length() <= 7) {
            passwordEditText.setError("Password is invalid");
            return false;
        } else if (!repeatPassword.equals(password)) {
            repeatPasswordEditText.setError("Confirm password is invalid");
            return false;
        } else {
            return true;
        }
    }

}