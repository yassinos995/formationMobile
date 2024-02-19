package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

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


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMe;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email_id);
        passwordEditText = findViewById(R.id.password_id);
        rememberMe = findViewById(R.id.rememberMe);

        MaterialButton LoginButton = findViewById(R.id.btn_login);
     LoginButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             login1();
         }
     });
    }

    SharedPreferences preferences = getSharedPreferences("checkBox",MODE_PRIVATE);

    CompoundButton.OnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
        @Override
                public void onCheckedChange(CompoundButton)
    });
    public void goToSignUp(View view) {
        Intent intent = new Intent(this, sign_up.class);
        startActivity(intent);
    }

  /*  public void goTofb(View view) throws PackageManager.NameNotFoundException {
        Intent facebookIntent;
        this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
        facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/{https://www.facebook.com/smart.ways.5836}"));
        startActivity(facebookIntent);
    }

    public void goToTwitter(View view) {


    }

    public void goToGit(View view) {

    }*/

    public void forgetap(View view) {
        Intent intent = new Intent(this, forgetPassword.class);
        startActivity(intent);

    }


    private void login1() {
        boolean isValid = true;

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

        if (isValid) {
            goToHome(null);
        }
    }

    public void goToHome(View view) {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }




}



