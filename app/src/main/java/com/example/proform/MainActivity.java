package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    private EditText emailEditText, passwordEditText;
    private static final String mail_regex = "^[A-Za-z0-9+_.-]+@(.+)$";
    MaterialButton btn_login;
    private CheckBox rememberMe;
    ImageView facebookLogo, googleLogo;
    boolean isPasswordVisible = false;
    private ProgressDialog progressDialog;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEditText = findViewById(R.id.email_id);
        passwordEditText = findViewById(R.id.password_id);
        btn_login = findViewById(R.id.btn_login);
        firebaseAuth = FirebaseAuth.getInstance();
        rememberMe = findViewById(R.id.rememberMe);
        facebookLogo = findViewById(R.id.logof);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        //googleLogo = findViewById(R.id.id_google);


       SharedPreferences preferences = getSharedPreferences("checkBox", MODE_PRIVATE);
        boolean resCheckBox = preferences.getBoolean("Remember", false);

        if (resCheckBox) {
            startActivity(new Intent(MainActivity.this, home.class));
        } else {
            Toast.makeText(this, "Please sign in !", Toast.LENGTH_SHORT).show();
        }

        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("Remember", isChecked);
            editor.apply();
        });
        btn_login.setOnClickListener(v -> {
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

       /* googleLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String googleUrl = "https://mail.google.com/mail/rabhiyassine995@gmail.com/0/#inbox";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(googleUrl));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    intent.setData(Uri.parse("https://mail.google.com/mail/rabhiyassine995@gmail.com/0/#inbox"));
                    startActivity(intent);
                }
            }
        });*/


        facebookLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl = "https://www.facebook.com/yassin.rebhi.90";

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

    private void login(String email, String password) {
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Log.d("Login", "User logged in successfully");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user);
                            CheckEmailVerification();

                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        Log.e("Login", "Authentication failed: " + task.getException().getMessage());
                        task.getException().printStackTrace();
                    }
                });
    }
    private void checkUserRole(FirebaseUser user) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User userData = dataSnapshot.getValue(User.class);
                    if (userData != null) {
                        String userPoste = userData.getPoste();
                        Log.d("UserRole", "User role: " + userPoste);
                        if (userPoste != null) {
                            switch (userPoste) {
                                case "Transporter":
                                    Log.d("UserRole", "Navigating to Transporter Dashboard");
                                    goToTransporterDashboard();
                                    break;
                                case "Chef":
                                    Log.d("UserRole", "Navigating to Chef Dashboard");
                                    goToChefDashboard();
                                    break;
                                case "Admin":
                                    Log.d("UserRole", "Navigating to Admin Dashboard");
                                    goToAdminDashboard(); // Redirect admin to home activity
                                    break;
                                default:
                                    Log.d("UserRole", "Redirecting to Regular User Dashboard");
                                    redirectToRegularUserDashboard();
                                    break;
                            }
                        } else {
                            Log.e("UserRole", "User role is null");
                            redirectToRegularUserDashboard();
                        }
                    }
                } else {
                    Log.e("UserRole", "DataSnapshot does not exist");
                    redirectToRegularUserDashboard();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error retrieving data: " + error.getMessage());
            }
        });
    }


    private void goToAdminDashboard() {
        Intent intent = new Intent(this,home.class);
        startActivity(intent);
        finish();
    }


    private void redirectToRegularUserDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void CheckEmailVerification() {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(loggedUser.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("poste").getValue(String.class);
                        if (role != null) {
                            if (role.equals("Transporter")) {
                                goToTransporterDashboard();
                            } else if (role.equals("Chef")) {
                                goToChefDashboard();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Failed to fetch user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(mail_regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void forgetap(View view) {
        Intent intent = new Intent(this, forgetPassword.class);
        startActivity(intent);
    }
    private void goToTransporterDashboard() {
        Intent intent = new Intent(MainActivity.this, HomeTransporter.class);
        startActivity(intent);
        finish();
    }

    private void goToChefDashboard() {
        Intent intent = new Intent(MainActivity.this, HomeChef.class);
        startActivity(intent);
        finish();
    }
    public void navigateToAnotherPage(View view) {
        Intent intent = new Intent(this, sign_up.class);
        startActivity(intent);
    }

}