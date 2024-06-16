package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proform.model.Test;
import com.example.proform.model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addTest extends AppCompatActivity {
    private Spinner transporterSpinner;
    private Button submitButton;
    private CardView cardDetails;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> spinnerAdapter;
    private ImageButton menuButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private TextView emailText, cinText, phoneText, emailTextView, cinTextView, phoneTextView, placeholderTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_test);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("tests");
        transporterSpinner = findViewById(R.id.transporterSpinner);
        cardDetails=findViewById(R.id.cardDetails);
        submitButton = findViewById(R.id.buttonSubmit);
        emailTextView = findViewById(R.id.EmailTextView);
        cinTextView = findViewById(R.id.CinlTextView);
        phoneTextView = findViewById(R.id.PhoneTextView);
        emailText = findViewById(R.id.EmailText);
        cinText = findViewById(R.id.CinText);
        phoneText = findViewById(R.id.PhoneText);
        placeholderTextView = findViewById(R.id.placeholderTextView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.id_menu);
        setupNavigationView();
        emailTextView.setVisibility(View.GONE);
        emailText.setVisibility(View.GONE);
        cinTextView.setVisibility(View.GONE);
        cinText.setVisibility(View.GONE);
        phoneTextView.setVisibility(View.GONE);
        phoneText.setVisibility(View.GONE);
        placeholderTextView.setVisibility(View.GONE);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transporterSpinner.setAdapter(spinnerAdapter);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        populateTransporterSpinner();
        submitButton.setOnClickListener(v -> addTestEntry());
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    gohome();
                    item.setChecked(true);
                    return true;
                } else if (itemId == R.id.nav_list_employers) {
                    openListEmployersActivity();
                    return true;
                } else if (itemId == R.id.nav_list_commands) {
                    openListCommandsActivity();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    openSetting();
                    return true;
                }else if (itemId == R.id.nav_list_tests) {
                    openListTestsActivity();
                    return true;
                } else if (itemId == R.id.nav_info) {
                    return true;
                } else if (itemId == R.id.nav_share) {
                    //   shareApp();
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    logout();
                    return true;
                } else {
                    drawerLayout.closeDrawers();
                    return true;
                }
            }
        });
    }

    private void openSetting() {
        Intent intent = new Intent(addTest.this, setting.class);
        startActivity(intent);
    }

    private void openListTestsActivity() {
        Intent intent = new Intent(addTest.this, listTests.class);
        startActivity(intent);
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(addTest.this, listcommand.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(addTest.this, listemp.class);
        startActivity(intent);
    }

    private void gohome() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUid);
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            String currentUserRole = currentUser.getPoste();
                            if ("Admin".equals(currentUserRole)) {
                                Intent intent = new Intent(addTest.this, home.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(addTest.this, HomeChef.class);
                                startActivity(intent);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(addTest.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(addTest.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        Intent intent = new Intent(addTest.this, MainActivity.class);
        startActivity(intent);
    }

    private void populateTransporterSpinner() {
        DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");
        testsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> transporterIdsWithTests = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String transporterId = snapshot.child("idTransporter").getValue(String.class);
                    if (transporterId != null) {
                        transporterIdsWithTests.add(transporterId);
                    }
                }
                fetchTransporters(transporterIdsWithTests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addTest.this, "Failed to retrieve tests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransporters(List<String> transporterIdsWithTests) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> transporterNames = new ArrayList<>();
                transporterNames.add("Select Transporter");

                Map<String, String> transporterMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("poste").getValue(String.class);

                    if (role != null && role.equals("Transporter") && !transporterIdsWithTests.contains(userId)) {
                        transporterNames.add(name);
                        transporterMap.put(name, userId);
                    }
                }
                spinnerAdapter.clear();
                spinnerAdapter.addAll(transporterNames);
                transporterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedName = parent.getItemAtPosition(position).toString();
                        if (!selectedName.equals("Select Transporter")) {
                            String selectedUid = transporterMap.get(selectedName);
                            fetchTransporterDetails(selectedUid);
                        } else {
                            placeholderTextView.setVisibility(View.GONE);
                            emailTextView.setVisibility(View.GONE);
                            emailText.setVisibility(View.GONE);
                            cinTextView.setVisibility(View.GONE);
                            cinText.setVisibility(View.GONE);
                            phoneTextView.setVisibility(View.GONE);
                            phoneText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addTest.this, "Failed to retrieve users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransporterDetails(String selectedUid) {
        DatabaseReference transporterRef = FirebaseDatabase.getInstance().getReference("users").child(selectedUid);
        transporterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String cin = dataSnapshot.child("cin").getValue(String.class);
                    String phone = dataSnapshot.child("phoneNumber").getValue(String.class);
                    emailText.setText(email);
                    cinText.setText(cin);
                    phoneText.setText(phone);

                    emailTextView.setVisibility(View.VISIBLE);
                    emailText.setVisibility(View.VISIBLE);
                    cinTextView.setVisibility(View.VISIBLE);
                    cinText.setVisibility(View.VISIBLE);
                    phoneTextView.setVisibility(View.VISIBLE);
                    phoneText.setVisibility(View.VISIBLE);

                    findViewById(R.id.cardDetails).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addTest.this, "Failed to retrieve transporter details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTestEntry() {
        String selectedTransporterName = transporterSpinner.getSelectedItem().toString();
        String email = emailText.getText().toString();
        String cin = cinText.getText().toString();
        String phone = phoneText.getText().toString();

        if (!email.isEmpty() && !cin.isEmpty() && !phone.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String testId = databaseReference.push().getKey();
                Test test = new Test();
                test.setIdTransporter(selectedTransporterName);
                test.setTestAlcool("In Progress");
                test.setTestReconnaissance("In Progress");
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String uid = snapshot.getKey();
                            if (name != null && name.equals(selectedTransporterName)) {
                                Map<String, Object> testDetails = new HashMap<>();
                                testDetails.put("idTransporter", uid);
                                testDetails.put("testAlcool", "In Progress");
                                testDetails.put("testReconnaissance", "In Progress");
                                databaseReference.child(testId).setValue(testDetails);
                                Toast.makeText(addTest.this, "Test added successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK, resultIntent);
                                finish();
                                notifyTransporter(uid);
                                return;
                            }
                        }
                        Toast.makeText(addTest.this, "Selected transporter not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(addTest.this, "Failed to retrieve transporter details", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(this, "Please select a transporter to add the test", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyTransporter(String transporterUid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(transporterUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User transporter = dataSnapshot.getValue(User.class);
                if (transporter != null) {
                    String transporterEmail = transporter.getEmail();
                    NotificationUtils.sendNotification(addTest.this, "New Test Assigned", "A new test has been assigned to you.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addTest.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
            }
        });
    }
}