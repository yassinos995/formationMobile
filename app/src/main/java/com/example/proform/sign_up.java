package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class sign_up extends AppCompatActivity {

    MaterialButton btnData;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText phoneNumberEditText;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference DatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.id_name);
        emailEditText = findViewById(R.id.id_emails);
        passwordEditText = findViewById(R.id.id_passwords);
        repeatPasswordEditText = findViewById(R.id.id_passwordr);
        phoneNumberEditText = findViewById(R.id.id_phone);
        btnData=findViewById(R.id.btn_s);
        DatabaseReference= FirebaseDatabase.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

        MaterialButton signUpButton = findViewById(R.id.btn_s);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }





    private void signUp() {
        boolean isValid = true;

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        if (!repeatPassword.equals(password)) {
            repeatPasswordEditText.setError("Passwords do not match");
            passwordEditText.setError("hell");
            isValid = false;
        }

        if (phoneNumber.isEmpty()) {
            phoneNumberEditText.setError("Phone number is required");
            isValid = false;
        } else if (phoneNumber.length() > 6) {
            phoneNumberEditText.setError("Enter a valid 3-digit phone number");
            isValid = false;
        }

        if (isValid) {
            goToLogins(null);
        }
    }
    public void goToLogins(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void backsign_up(View view){

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }





 /*   private void signUpUser(String name,String email,String password,String repeatPassword,String phoneNumber){
        FirebaseAuth auth= FirebaseAuth.getInstance();
    }*/

}