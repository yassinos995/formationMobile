package com.example.proform;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.proform.model.User;
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
    private CardView orderCardView, administrationCardView;
    private TextView textView2, textView3;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton2;
    private static final int REQUEST_CODE_ADD_TRANSPORTER = 1003;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transporter);

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("users").child(user.getUid());

        // Initialize UI Elements
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        orderCardView = findViewById(R.id.orderCardView);
        administrationCardView = findViewById(R.id.administrationCardView);
        menuButton2 = findViewById(R.id.id_menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setupNavigationView();
        menuButton2.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        orderCardView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeTransporter.this, OrdersT.class);
            startActivity(intent);
        });

        administrationCardView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeTransporter.this, administration.class);
            startActivity(intent);
        });

        // Setup Bottom Navigation View
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeT) {
                Log.d("Navigation", "Home selected");
                return true;
            } else if (itemId == R.id.profileT) {
                Log.d("Navigation", "Profile selected");
                Intent intent = new Intent(HomeTransporter.this, Profil.class);
                String userName = textView2.getText().toString();
                Log.d("Navigation", "Starting Profil activity with userName: " + userName);
                intent.putExtra("userName", userName);
                startActivity(intent);
                return true;
            }
            return false;
        });
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateProfileInformation();
    }
    private void updateProfileInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchCurrentUserProfileFromFirebase(user.getUid());
        }
    }
    private void fetchCurrentUserProfileFromFirebase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String poste = snapshot.child("poste").getValue(String.class);
                    textView2.setText(userName);
                    textView3.setText(poste);

                    if ("Transporter".equals(poste)) {
                        MenuItem listEmployersMenuItem = navigationView.getMenu().findItem(R.id.nav_list_employers);
                        MenuItem listTestsMenuItem = navigationView.getMenu().findItem(R.id.nav_list_tests);
                        listEmployersMenuItem.setVisible(false);
                        listTestsMenuItem.setVisible(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeTransporter.this, "Error fetching user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                goHome();
                return true;
            } else if (itemId == R.id.nav_list_employers) {
                return true;
            } else if (itemId == R.id.nav_list_commands) {
                openlistcommand();
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
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String poste = snapshot.child("poste").getValue(String.class);
                        if ("Transporter".equals(poste)) {
                            MenuItem listEmployersMenuItem = navigationView.getMenu().findItem(R.id.nav_list_employers);
                            MenuItem listTestsMenuItem = navigationView.getMenu().findItem(R.id.nav_list_tests);
                            listEmployersMenuItem.setVisible(false);
                            listTestsMenuItem.setVisible(false);
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

    private void openlistcommand() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeTransporter.class);
        startActivity(intent);
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeTransporter.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_TRANSPORTER) {
            if (resultCode == RESULT_OK) {
                updateProfileInformation();
            }
        }
    }
}
