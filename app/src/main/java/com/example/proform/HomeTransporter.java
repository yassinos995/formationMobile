package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeTransporter extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CardView orderCardView, administrationCardView, delivredCardView;
    private TextView textView2;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transporter);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("users").child(user.getUid());

        textView2 = findViewById(R.id.textView2);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        orderCardView = findViewById(R.id.orderCardView);
        administrationCardView = findViewById(R.id.administrationCardView);
        menuButton2 = findViewById(R.id.id_menu);
        bottomNavigationView.setSelectedItemId(R.id.homeT);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setupNavigationView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = snapshot.child("name").getValue(String.class);
                        if (userName != null) {
                            textView2.setText(userName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeTransporter", "Failed to load user profile: " + error.getMessage());
                }
            });
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homeT) {
                    return true;
                } else if (itemId == R.id.profileT) {
                    startActivity(new Intent(HomeTransporter.this, Profil.class));
                    return true;
                }
                return false;
            }
        });

        menuButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        orderCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeTransporter.this, OrdersT.class);
                startActivity(intent);
            }
        });

        administrationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeTransporter.this, administration.class);
                startActivity(intent);
            }
        });

        loadUserProfile();
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null) {
                        // Update UI with user profile information
                        Toast.makeText(HomeTransporter.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeTransporter", "Failed to load user profile: " + databaseError.getMessage());
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
                } else if (itemId == R.id.nav_settings) {
                    return true;
                } else if (itemId == R.id.nav_info) {
                    return true;
                } else if (itemId == R.id.nav_share) {
                    shareApp();
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

        // Toggle visibility of menu items based on user type (admin or not)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);
                        if (isAdmin != null) {
                            MenuItem listEmployersMenuItem = navigationView.getMenu().findItem(R.id.nav_list_employers);
                            MenuItem listCommandsMenuItem = navigationView.getMenu().findItem(R.id.nav_list_commands);
                            if (isAdmin.booleanValue()) {
                                listEmployersMenuItem.setVisible(true);
                                listCommandsMenuItem.setVisible(true);
                            } else {
                                listEmployersMenuItem.setVisible(false);
                                listCommandsMenuItem.setVisible(false);
                            }
                        } else {
                            // Handle the case where isAdmin is null
                            Log.e("HomeTransporter", "isAdmin is null");
                        }
                    }
                }



                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("setupNavigationView", "Failed to retrieve user data: " + error.getMessage());
                }
            });
        }
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }

    private void gohome() {
        Intent intent = new Intent(this, HomeTransporter.class);
        startActivity(intent);
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(HomeTransporter.this, MainActivity.class);
        startActivity(intent);
    }
}
