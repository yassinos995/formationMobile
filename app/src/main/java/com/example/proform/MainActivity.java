package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText,passwordEditText;
    private static final String mail_regex="^[A-Za-z0-9+_.-]+@(.+)$";
    MaterialButton btn_login;
    private  CheckBox rememberMe;
    ImageView facebookLogo,googleLogo,githubLogo;
    boolean isPasswordVisible = false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEditText = findViewById(R.id.email_id);
        passwordEditText = findViewById(R.id.password_id);
        btn_login = findViewById(R.id.btn_login);
        firebaseAuth = FirebaseAuth.getInstance();
        rememberMe=findViewById(R.id.rememberMe);
        facebookLogo = findViewById(R.id.logof);
        googleLogo = findViewById(R.id.id_google);
        githubLogo = findViewById(R.id.id_git);

        SharedPreferences preferences=getSharedPreferences("checkBox",MODE_PRIVATE);
        boolean resCheckBox = preferences.getBoolean("Remember",false);

        if (resCheckBox){
            startActivity(new Intent(MainActivity.this, HomeTransporter.class));
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
            if (TextUtils.isEmpty(emails)) {
                emailEditText.setError("Email is required!");
            } else if (!isValidEmail(emails)) {
                emailEditText.setError("Email is invalid!");
            } else if (TextUtils.isEmpty(passwords)) {
                passwordEditText.setError("Password is required!");
            } else if (passwords.length() <= 7) {
                passwordEditText.setError("Password is too short!");
            } else {
                login(emails, passwords);
            }
    });

        googleLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String googleUrl="https://mail.google.com/mail/rabhiyassine995@gmail.com/0/#inbox";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(googleUrl));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    intent.setData(Uri.parse("https://mail.google.com/mail/rabhiyassine995@gmail.com/0/#inbox"));
                    startActivity(intent);
                }
            }
        });
        githubLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String githubUrl="https://github.com/yassinos995";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(githubUrl));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // If no app can handle the intent, open the GitHub website in a browser
                    intent.setData(Uri.parse("https://github.com/yassinos995"));
                    startActivity(intent);
                }
            }
        });

        facebookLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl ="https://www.facebook.com/yassin.rebhi.90";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(facebookUrl));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    intent.setData(Uri.parse("https://www.facebook.com/yassin.rebhi.90"));
                    startActivity(intent);
                }
            }
        });

passwordEditText.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                // Toggle password visibility
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    // Show password
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0);
                } else {
                    // Hide password
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0);
                }
                return true;
            }
        }
        return false;
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
                startActivity(new Intent(getApplicationContext(), HomeTransporter.class));
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

