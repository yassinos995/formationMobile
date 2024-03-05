package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profil extends AppCompatActivity {
    private EditText name, email, phone;
    Button edit, cancel;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser user;
    private DatabaseReference rf;
    private String originalName;
    private String originalPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        name = findViewById(R.id.id_nameProfile);
        email = findViewById(R.id.id_emailsProfile);
        phone = findViewById(R.id.id_phoneProfile);
        edit = findViewById(R.id.btn_edit_profile);
        cancel = findViewById(R.id.btn_CancelProfile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user = firebaseAuth.getCurrentUser();
        rf = firebaseDatabase.getReference().child("users").child(user.getUid());

        rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String namep = snapshot.child("name").getValue().toString();
                String emailp = snapshot.child("email").getValue().toString();
                String phoneNumberp = snapshot.child("phoneNumber").getValue().toString();
                name.setText(namep);
                email.setText(emailp);
                phone.setText(phoneNumberp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profil.this, " Error !", Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("checkBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("remember", false);
            editor.apply();
            firebaseAuth.signOut();
            startActivity(new Intent(Profil.this, MainActivity.class));
            Toast.makeText(this, "you Log Out", Toast.LENGTH_SHORT).show();
            finish();
        });
        edit.setOnClickListener(v -> {
            String newName = name.getText().toString().trim();
            String newPhoneNumber = phone.getText().toString().trim();

            rf.child("name").setValue(newName);
            rf.child("phoneNumber").setValue(newPhoneNumber);

            originalName = newName;
            originalPhoneNumber = newPhoneNumber;

            Toast.makeText(Profil.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();


        });
        edit.setEnabled(false);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!name.getText().toString().equals(originalName) ||
                    !phone.getText().toString().equals(originalPhoneNumber)) {
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        }

    };
}