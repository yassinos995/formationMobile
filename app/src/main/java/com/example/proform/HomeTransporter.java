package com.example.proform;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proform.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class HomeTransporter extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private BottomNavigationView bottomNavigationView;
    private CardView orderCardView, administrationCardView;
    private TextView textView2, textView3;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton2;
    private ImageView redCircle;
    private ImageView imageBell;
    private FloatingActionButton btn_scan;
    private static final int REQUEST_CODE_ADD_TRANSPORTER = 1003;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1033;
    private static final int REQUEST_CODE_ADD_COMMAND = 1043;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transporter);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("users").child(user.getUid());

        imageBell = findViewById(R.id.imageView2);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        orderCardView = findViewById(R.id.orderCardView);
        administrationCardView = findViewById(R.id.administrationCardView);
        menuButton2 = findViewById(R.id.id_menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        btn_scan = findViewById(R.id.floatingActionButton);
        btn_scan.setOnClickListener(v -> {
            scanCode();
        });
        navigationView = findViewById(R.id.nav_view);

        setupNavigationView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
        menuButton2.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        orderCardView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeTransporter.this, OrdersT.class);
            startActivity(intent);
        });
        administrationCardView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeTransporter.this, administration.class);
            startActivity(intent);
        });
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

    private void scanCode() {
        ScanOptions options =new ScanOptions();
        options.setPrompt("up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                new AlertDialog.Builder(this)
                        .setMessage("Camera permission is required to scan QR codes.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            Log.d("ScanResult", "Result: " + result.getContents());
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeTransporter.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        } else {
            Log.d("ScanResult", "No result found");
        }
    });


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
    public void showNewCommandNotification() {
        String channelId = "NEW_COMMAND_NOTIFICATION_CHANNEL";
        String channelName = "New Command Notification Channel";
        String channelDescription = "Notification for new commands";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle("New Command Added")
                .setContentText("A new command has been successfully added.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(1, builder.build());
        redCircle.setVisibility(View.VISIBLE);
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
                goToSetting();
                return true;
            } else if (itemId == R.id.nav_info) {
                openInfo();
                return true;
            } else if (itemId == R.id.nav_share) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody = "https://www.mediafire.com/file/2lexzz9fg61w60g/TrackZone1.apk/file";
                String shareSub = "EypCnn";
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(myIntent, "Partager avec"));
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

    private void openInfo() {
        Intent intent = new Intent(HomeTransporter.this, info.class);
        startActivity(intent);
    }

    private void goToSetting() {
        Intent intent = new Intent(HomeTransporter.this, setting.class);
        startActivity(intent);
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
        } else if (requestCode == REQUEST_CODE_ADD_COMMAND) {
            if (resultCode == RESULT_OK) {
                showNewCommandNotification();
            }
        }
    }
}
