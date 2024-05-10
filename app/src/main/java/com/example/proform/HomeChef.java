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

public class HomeChef extends AppCompatActivity {
    private CardView AddTransCardViewc, addcommandCardViewc;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton3;
    private TextView textView2;
    private TextView textView3;
    private static final int REQUEST_CODE_ADD_TRANSPORTER = 1002;

    private User currentUser; // Define currentUser variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_chef);

        bottomNavigationView = findViewById(R.id.bottomNavigationView11);
        AddTransCardViewc = findViewById(R.id.AddTransCardViewc);
        addcommandCardViewc = findViewById(R.id.addcommandCardViewc);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton3 = findViewById(R.id.id_menu2);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);

        setupNavigationView();

        menuButton3.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        AddTransCardViewc.setOnClickListener(v -> {
            Intent intent = new Intent(this, sign_up.class);
            startActivity(intent);
        });

        addcommandCardViewc.setOnClickListener(v -> {
            Intent intent = new Intent(this, addcmd.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeAd) {
                Log.d("Navigation", "Home selected");
                return true;
            } else if (itemId == R.id.profileAd) {
                Log.d("Navigation", "Profile selected");
                Intent intent = new Intent(HomeChef.this, Profil.class);
                String userName = textView2.getText().toString();
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
                    if ("Chef personnelle".equals(poste)) {
                        textView2.setText(userName);
                        textView3.setText(poste);
                    } else {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeChef.this, "Error fetching user profile", Toast.LENGTH_SHORT).show();
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
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void goHome() {
        drawerLayout.closeDrawers();
    }

    private void shareApp() {
        Toast.makeText(this, "Mazelna ma gadinahech", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeChef.this, MainActivity.class);
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
