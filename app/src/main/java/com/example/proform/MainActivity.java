package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proform.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText,passwordEditText;
    private static final String mail_regex="^[A-Za-z0-9+_.-]+@(.+)$";
    MaterialButton btn_login;
    private  CheckBox rememberMe;



    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEditText = findViewById(R.id.email_id);
        passwordEditText = findViewById(R.id.password_id);
        btn_login = findViewById(R.id.btn_login);
        firebaseAuth = FirebaseAuth.getInstance();
        rememberMe=findViewById(R.id.rememberMe);

        SharedPreferences preferences=getSharedPreferences("checkBox",MODE_PRIVATE);
        boolean resCheckBox = preferences.getBoolean("Remember",false);

        if (resCheckBox){
            startActivity(new Intent(MainActivity.this, home.class));
        }else {
            Toast.makeText(this, "Please sign in !", Toast.LENGTH_SHORT).show();
        }
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    SharedPreferences preferences=getSharedPreferences("checkBox",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putBoolean("remember",true);
                    editor.apply();
                } else if (!buttonView.isChecked()) {
                    SharedPreferences preferences=getSharedPreferences("checkBox",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putBoolean("remember",false);
                    editor.apply();
                }
            }
        });

        btn_login.setOnClickListener( v -> {
        String emails = emailEditText.getText().toString().trim();
        String passwords = passwordEditText.getText().toString().trim();
            if (! isValidEmail(emails)){
                emailEditText.setError("Email is invalid!");
            } else if (passwords.length()<=7) {
                passwordEditText.setError("Password is invalid!");
            }else {
                login(emails,passwords);
            }
    });
    }
    private void login(String emails, String passwords) {
        firebaseAuth.signInWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        CheckEmailVerification();
                    } else {
                        Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void CheckEmailVerification() {
        FirebaseUser loggedUser=firebaseAuth.getCurrentUser();
        if (loggedUser!=null){
            if (loggedUser.isEmailVerified()){
                finish();
                startActivity(new Intent(getApplicationContext(), home.class));
                Toast.makeText(this, "welcome to home", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Please verify your Email", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
            }
        }
    }

    private boolean isValidEmail(String email) {
        Pattern pattern=Pattern.compile(mail_regex);
        Matcher matcher= pattern.matcher(email);
        return matcher.matches();
    }

    public void goToSignUp(View view) {
        Intent intent = new Intent(this, sign_up.class);
        startActivity(intent);
    }

    public void forgetap(View view) {
        Intent intent = new Intent(this, forgetPassword.class);
        startActivity(intent);
    }

    public void goToHome() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }



}

