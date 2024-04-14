package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

public class HomeChef extends AppCompatActivity {
    private CardView AddTransCardViewc, listemployersCardViewc;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_chef);
        bottomNavigationView = findViewById(R.id.bottomNavigationView11);
        AddTransCardViewc = findViewById(R.id.AddTransCardViewc);
        listemployersCardViewc = findViewById(R.id.listemployersCardViewc);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setupNavigationView();

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.homeAd) {
                    Log.d("Navigation", "Home selected");
                    return true;
                } else if (itemId == R.id.profileAd) {
                    Log.d("Navigation", "Profile selected");
                    startActivity(new Intent(HomeChef.this, Profil.class));
                    return true;
                }
                return false;
            }
        });

        AddTransCardViewc.setOnClickListener(v -> {
            Intent intent = new Intent(this, sign_up.class);
            startActivity(intent);
        });
        listemployersCardViewc.setOnClickListener(v -> {
            Intent intent = new Intent(this, listemp.class);
            startActivity(intent);
        });

    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
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
        });

        // Toggle visibility of menu items based on user type (admin or not)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);
                        MenuItem listEmployersMenuItem = navigationView.getMenu().findItem(R.id.nav_list_employers);
                        MenuItem listCommandsMenuItem = navigationView.getMenu().findItem(R.id.nav_list_commands);
                        if (isAdmin) {
                            listEmployersMenuItem.setVisible(true);
                            listCommandsMenuItem.setVisible(true);
                        } else {
                            listEmployersMenuItem.setVisible(false);
                            listCommandsMenuItem.setVisible(false);
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

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void gohome() {
        Intent intent = new Intent(this, HomeChef.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isFromProfil = getIntent().getBooleanExtra("from_profil", false);

        if (isFromProfil) {
            bottomNavigationView.setSelectedItemId(R.id.homeAd);
        }
    }
}
