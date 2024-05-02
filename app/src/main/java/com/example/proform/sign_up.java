package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proform.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sign_up extends AppCompatActivity {
    MaterialButton btn_s;
    private EditText nameEditText, emailEditText, passwordEditText, repeatPasswordEditText, phoneNumberEditText, posteEditText,cinEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference DatabaseReference;
    private static final String mail_regex = "^[A-Za-z0-9+_.-]+@(.+)$";
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        nameEditText = findViewById(R.id.id_name);
        emailEditText = findViewById(R.id.id_emails);
        passwordEditText = findViewById(R.id.id_passwords);
        repeatPasswordEditText = findViewById(R.id.id_passwordr);
        phoneNumberEditText = findViewById(R.id.id_phone);
        cinEditText = findViewById(R.id.id_cin);
        btn_s = findViewById(R.id.btn_s);
        DatabaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        btn_s.setOnClickListener(v -> signUpUser());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User userData = snapshot.getValue(User.class);
                        if (userData != null) {
                            String userRole = userData.getPoste();
                            if (userRole != null && userRole.equals("Chef personnelle")) {
                                TextView poste1 = findViewById(R.id.poste1);
                                RadioGroup posteRadioGroup = findViewById(R.id.posteRadioGroup);
                                poste1.setVisibility(View.GONE);
                                posteRadioGroup.setVisibility(View.GONE);

                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("setupRadioButtons", "Failed to retrieve user data: " + error.getMessage());
                }
            });
        }
    }

    private void signUpUser() {
        String name = nameEditText.getText().toString().trim();
        String Cin=cinEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        RadioGroup posteRadioGroup = findViewById(R.id.posteRadioGroup);
        int selectedRadioButtonId = posteRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        if (selectedRadioButton != null) {
            String poste = selectedRadioButton.getText().toString().trim();
            if (validate(name,Cin, email, phoneNumber, password, repeatPassword, poste)) {
                progressDialog.show();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendEmailVerification(name,Cin, email, phoneNumber, poste);
                    } else {
                        progressDialog.dismiss();
                        Log.e("SignUp", "Sign-up failed: " + task.getException());
                        Toast.makeText(sign_up.this, "Sign-up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            progressDialog.dismiss();
        }
    }
    private void sendEmailVerification(String name,String Cin, String email, String phoneNumber, String poste) {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            loggedUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    sendUserData(name, Cin,email, phoneNumber, poste, passwordEditText.getText().toString().trim());

                    Toast.makeText(this, "Registration done! Please check your email address.", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Log.e("EmailVerification", "Email verification failed: " + task.getException().getMessage());

                    Toast.makeText(this, "Email verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }}
    private void sendUserData(String Cin,String name,String email, String phoneNumber ,String poste,String password) {
        DatabaseReference usersRef = DatabaseReference.child("users");
       firebaseAuth.getUid();
        User user = new User(Cin,name,email, phoneNumber, password, poste);
        usersRef.child("" + firebaseAuth.getUid()).setValue(user);
    }
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(mail_regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private boolean validate(String name,String Cin, String email, String phoneNumber, String password, String repeatPassword, String poste) {
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
        }  else if (Cin.isEmpty()) {
            cinEditText.setError("Cin is required");
            return false;
        }else {
            return true;
        }
    }

}