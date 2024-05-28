package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class forgetPassword extends AppCompatActivity {
    private EditText emailf;
    private Button reset_btn;
    private Button back_btn;
    private FirebaseAuth firebaseAuth;
    private  String emailS;
    private static final String mail_regex="^[A-Za-z0-9+_.-]+@(.+)$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        reset_btn=findViewById(R.id.reset_btn);
        emailf = findViewById(R.id.email_forget);
        back_btn=findViewById(R.id.back_btn);
        firebaseAuth=FirebaseAuth.getInstance();

        reset_btn.setOnClickListener( v -> {
            emailS=emailf.getText().toString().trim();
            if(!isValidEmail(emailS)){
                emailf.setError("email not valid");
            }else{
                firebaseAuth.sendPasswordResetEmail(emailS).addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        Toast.makeText(this, "Password sent ", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(forgetPassword.this,MainActivity.class));
                    }else {
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                } );

            }

        });


    }
    public void backlog(View view){
        Intent intent= new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailf.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailf.setError("Invalid email address");
            return false;
        }
        return true;
    }
    private boolean isValidEmail(String email) {
        Pattern pattern=Pattern.compile(mail_regex);
        Matcher matcher= pattern.matcher(email);
        return matcher.matches();
    }
}