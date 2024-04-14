package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private EditText nameEditText, emailEditText, passwordEditText, repeatPasswordEditText, phoneNumberEditText, posteEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference DatabaseReference;
    private static final String mail_regex = "^[A-Za-z0-9+_.-]+@(.+)$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.id_name);
        emailEditText = findViewById(R.id.id_emails);
        passwordEditText = findViewById(R.id.id_passwords);
        repeatPasswordEditText = findViewById(R.id.id_passwordr);
        phoneNumberEditText = findViewById(R.id.id_phone);
        btn_s = findViewById(R.id.btn_s);

        DatabaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        btn_s.setOnClickListener(v -> {
            signUpUser();
        });
    }

    private void signUpUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        RadioGroup posteRadioGroup = findViewById(R.id.posteRadioGroup);
        int selectedRadioButtonId = posteRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);

        if (selectedRadioButton != null) {
            String poste = selectedRadioButton.getText().toString().trim();
            if (validate(name, email, password, repeatPassword, phoneNumber, poste)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendEmailVerification(name, email, phoneNumber, poste);
                    } else {
                        Log.e("SignUp", "Sign-up failed: " + task.getException());
                        Toast.makeText(sign_up.this, "Sign-up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Log.e("sign_up", "Selected RadioButton is null");
        }
    }

    private void sendEmailVerification(String name, String email, String phoneNumber, String poste) {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            loggedUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserData(name, email, phoneNumber, poste, passwordEditText.getText().toString().trim());
                    Toast.makeText(this, "Registration done! Please check your email address.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(sign_up.this, home.class));
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendUserData(String name, String email, String phoneNumber, String poste, String password) {
        DatabaseReference usersRef = DatabaseReference.child("users");
        String userId = firebaseAuth.getUid();
        boolean isAdmin = poste.equals("Admin");
        User user = new User(name, email, phoneNumber, poste, password, isAdmin);
        user.setUserId(userId);
        user.setAdmin(isAdmin);
        usersRef.child("" + firebaseAuth.getUid()).setValue(user);
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(mail_regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validate(String name, String email, String password, String repeatPassword, String phoneNumber, String poste) {
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
        } else if (poste.isEmpty()) {
            posteEditText.setError("Poste is required");
            return false;
        } else {
            return true;
        }
    }
}