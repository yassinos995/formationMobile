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

public class home extends AppCompatActivity {
    private CardView AddTransCardView, addChefPCardView, addcommandCardView;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView11);
        AddTransCardView = findViewById(R.id.AddTransCardView);
        addChefPCardView = findViewById(R.id.addchefPCardView);
        addcommandCardView = findViewById(R.id.addcommandCardView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.id_menu);
        textView2 = findViewById(R.id.textView2);
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

                }
            });

        }
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homeAd) {
                    Log.d("Navigation", "Home selected");
                    return true;
                } else if (itemId == R.id.profileAd) {
                    Log.d("Navigation", "Profile selected");
                    startActivity(new Intent(home.this, Profil.class));
                    return true;
                }
                return false;
            }
        });

        AddTransCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, sign_up.class);
                startActivity(intent);
            }
        });

        addChefPCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, sign_up.class);
                startActivity(intent);
            }
        });

        addcommandCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, addcmd.class);
                startActivity(intent);
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
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(home.this, listcommand.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(home.this, listemp.class);
        startActivity(intent);
    }

    private void gohome() {
        Intent intent = new Intent(home.this, home.class);
        startActivity(intent);
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(home.this, MainActivity.class);
        startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        boolean isFromProfil = getIntent().getBooleanExtra("from_profil", false);
        if (isFromProfil) {
            bottomNavigationView.setSelectedItemId(R.id.homeAd);
        }
    }
}