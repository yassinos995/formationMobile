package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.proform.model.User;
import com.example.proform.model.commande;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class addcmd extends AppCompatActivity {

    private EditText dateEdit, descEdit, destEdit;
    private Spinner transporterSpinner;
    Calendar myCalendar;
    private Button submitButton;
    private ImageButton menuButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> spinnerAdapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcmd);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("commands");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        dateEdit = findViewById(R.id.dateEdit);
        descEdit = findViewById(R.id.descEdit);
        destEdit = findViewById(R.id.destEdit);
        transporterSpinner = findViewById(R.id.transporterSpinner);
        submitButton = findViewById(R.id.buttonSubmit);
        menuButton = findViewById(R.id.id_menu);
        myCalendar = Calendar.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transporterSpinner.setAdapter(spinnerAdapter);
        setupNavigationView();

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        populateTransporterSpinner();
        final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        };
        dateEdit.setOnClickListener(v -> new DatePickerDialog(addcmd.this, dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        submitButton.setOnClickListener(v -> addCommand());
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
                    return true;
                } else if (itemId == R.id.nav_list_tests) {
                     openListTestsActivity();
                    return true;
                } else if (itemId == R.id.nav_info) {
                    return true;
                } else if (itemId == R.id.nav_share) {
                    // shareApp();
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

    private void openListTestsActivity() {
        Intent intent = new Intent(addcmd.this,listTests.class);
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
                                Intent intent = new Intent(addcmd.this, home.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(addcmd.this, HomeChef.class);
                                startActivity(intent);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(addcmd.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(addcmd.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(addcmd.this, listemp.class);
        startActivity(intent);
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(addcmd.this, listcommand.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(addcmd.this, MainActivity.class);
        startActivity(intent);
    }

    private void updateDateLabel() {
        String myFormat = "MM/dd/yy EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateEdit.setText(sdf.format(myCalendar.getTime()));
    }

    private void populateTransporterSpinner() {
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

                    if (role != null && role.equals("Transporter")) {
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
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addcmd.this, "Failed to retrieve users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCommand() {
        String date = dateEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();
        String dest = destEdit.getText().toString().trim();
        String selectedTransporterName = transporterSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(dest) || TextUtils.isEmpty(selectedTransporterName) || selectedTransporterName.equals("Select Transporter")) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar currentDateTime = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
        try {
            Date selectedDate = sdf.parse(date);
            if (selectedDate != null) {
                Calendar selectedDateTime = Calendar.getInstance();
                selectedDateTime.setTime(selectedDate);
                if (selectedDateTime.compareTo(currentDateTime) <= 0) {
                    Toast.makeText(this, "Please select a date and time in the future", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("name").equalTo(selectedTransporterName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String selectedTransporterUid = snapshot.getKey();
                        saveCommand(date, desc, dest, selectedTransporterUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addcmd.this, "Failed to retrieve transporter UID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCommand(String date, String desc, String dest, String selectedTransporterUid) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String commandId = databaseReference.push().getKey();
            String defaultEtat = "Not Completed";
            commande command = new commande();
            command.setDateLimite(date);
            command.setDesc(desc);
            command.setDestination(dest);
            command.setIdtransporter(selectedTransporterUid);
            command.setEtat(defaultEtat);
            databaseReference.child(commandId).setValue(command).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    notifyTransporter(selectedTransporterUid, desc);
                    Toast.makeText(addcmd.this, "Command added successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    dateEdit.setText("");
                    descEdit.setText("");
                    destEdit.setText("");
                } else {
                    Toast.makeText(addcmd.this, "Failed to add command", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void notifyTransporter(String transporterUid, String commandDescription) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(transporterUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User transporter = dataSnapshot.getValue(User.class);
                if (transporter != null) {
                    String transporterEmail = transporter.getEmail();
                    NotificationUtils.sendNotification(addcmd.this, "New Command Assigned", "A new command has been assigned to you: " + commandDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(addcmd.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
