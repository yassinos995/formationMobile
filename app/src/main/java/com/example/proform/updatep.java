package com.example.proform;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proform.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class updatep extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button updateButton;
    private Button cancelButton;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatep);

        nameEditText = findViewById(R.id.id_nameProfileU);
        emailEditText = findViewById(R.id.id_emailsProfileU);
        phoneEditText = findViewById(R.id.id_phoneProfileU);
        updateButton = findViewById(R.id.btn_edit_profileU);
        cancelButton = findViewById(R.id.btn_CancelProfileU);

        User user = (User) getIntent().getSerializableExtra("user");

        // Populate EditText fields with user data
        if (user != null) {
            nameEditText.setText(user.getName());
            emailEditText.setText(user.getEmail());
            phoneEditText.setText(user.getPhoneNumber());
        }
        updateButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newPhoneNumber = phoneEditText.getText().toString().trim();

            if (user != null) {
                userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUserId());
                userRef.child("name").setValue(newName);
                userRef.child("email").setValue(newEmail);
                userRef.child("phoneNumber").setValue(newPhoneNumber);
                finish();
            }
        });
        cancelButton.setOnClickListener(v -> finish());
    }
}