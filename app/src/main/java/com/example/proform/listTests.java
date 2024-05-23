package com.example.proform;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proform.model.Test;
import com.example.proform.model.User;
import com.example.proform.model.UserTest;
import com.example.proform.model.UserTestAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class listTests extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserTestAdapter adapter;
    private List<UserTest> userTestList;
    private FirebaseAuth mAuth;
    private ImageButton menubuttonL;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean testD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tests);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        testD = false;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menubuttonL = findViewById(R.id.id_menuLT);
        drawerLayout = findViewById(R.id.drawer_layout_listeTest);
        navigationView = findViewById(R.id.nav_view);
        userTestList = new ArrayList<>();
        adapter = new UserTestAdapter(this, userTestList);
        recyclerView.setAdapter(adapter);
        setupNavigationView();

        menubuttonL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        fetchTests();
    }

    private void fetchTests() {
        DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");

        testsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> transporterIds = new HashSet<>();
                for (DataSnapshot testSnapshot : dataSnapshot.getChildren()) {
                    String idTransporter = testSnapshot.child("idTransporter").getValue(String.class);
                    if (idTransporter != null) {
                        transporterIds.add(idTransporter);
                    }
                }
                fetchUsers(transporterIds, dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
                Toast.makeText(listTests.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsers(Set<String> transporterIds, DataSnapshot testsSnapshot) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userTestList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null && transporterIds.contains(userId)) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            for (DataSnapshot testSnapshot : testsSnapshot.getChildren()) {
                                String idTransporter = testSnapshot.child("idTransporter").getValue(String.class);
                                if (userId.equals(idTransporter)) {
                                    String testAlcool = testSnapshot.child("testAlcool").getValue(String.class);
                                    String testReconnaissance = testSnapshot.child("testReconnaissance").getValue(String.class);
                                    UserTest userTest = new UserTest(user.getName(), user.getEmail(), idTransporter, testAlcool, testReconnaissance);
                                    userTestList.add(userTest);
                                }
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
                Toast.makeText(listTests.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    gohome();
                    return true;
                } else if (itemId == R.id.nav_list_employers) {
                    openListEmployersActivity();
                    return true;
                } else if (itemId == R.id.nav_list_commands) {
                    openListCommandsActivity();
                    return true;
                } else if (itemId == R.id.nav_settings || itemId == R.id.nav_info || itemId == R.id.nav_share) {
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

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void gohome() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(listTests.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserUid = currentUser.getUid();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserUid);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser == null) return;

                String currentUserRole = currentUser.getPoste();
                Intent intent;
                if ("Admin".equals(currentUserRole)) {
                    intent = new Intent(listTests.this, home.class);
                } else {
                    intent = new Intent(listTests.this, HomeChef.class);
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
                Toast.makeText(listTests.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
