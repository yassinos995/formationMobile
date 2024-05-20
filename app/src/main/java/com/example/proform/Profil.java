package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.proform.model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profil extends AppCompatActivity {
    private EditText nameEditText, emailEditText, phoneEditText;
    Button edit, cancel;
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
            if (!nameEditText.getText().toString().equals(originalName) ||
                    !phoneEditText.getText().toString().equals(originalPhoneNumber)) {
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
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
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                        if (name != null && email != null && phoneNumber != null) {
                            nameEditText.setText(name);
                            emailEditText.setText(email);
                            phoneEditText.setText(phoneNumber);
                            originalName = name;
                            originalPhoneNumber = phoneNumber;
                            DatabaseReference rf = snapshot.getRef();
                            edit.setOnClickListener(v -> updateProfile(rf));
                        } else {
                            Toast.makeText(Profil.this, "User information is incomplete.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(Profil.this, "User information not found.", Toast.LENGTH_SHORT).show();
                }}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profil.this, "Error retrieving user information.", Toast.LENGTH_SHORT).show();
            }
        });
        nameEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);
        cancel.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("checkBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("remember", false);
            editor.apply();
            firebaseAuth.signOut();
            Intent intent = new Intent(Profil.this, home.class);
            intent.putExtra("from_profil", true);
            startActivity(intent);
            finish();
        });
    }
    public void backHome(View view) {
        finish();
    }
    private void updateProfile(DatabaseReference rf) {
        String newName = nameEditText.getText().toString().trim();
        String newPhoneNumber = phoneEditText.getText().toString().trim();
        if (!newName.isEmpty() && !newPhoneNumber.isEmpty()) {
            rf.child("name").setValue(newName);
            rf.child("phoneNumber").setValue(newPhoneNumber);
            Intent intent = new Intent();
            intent.putExtra("updatedName", newName);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(Profil.this, "Name and phone number cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

}
