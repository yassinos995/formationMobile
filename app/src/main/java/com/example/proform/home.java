package com.example.proform;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class home extends AppCompatActivity {
    private CardView AddTransCardView, addChefPCardView, addcommandCardView, addtestCardView;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;
    private TextView textView2;
    private TextView textView3;
    private static final int REQUEST_CODE_ADD_TRANSPORTER = 1001;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1002;
    private static final int REQUEST_CODE_ADD_COMMAND = 1003;
    private static final int REQUEST_CODE_ADD_TEST = 1004;
    private ImageView redCircle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottomNavigationView11);
        AddTransCardView = findViewById(R.id.AddTransCardView);
        addChefPCardView = findViewById(R.id.addchefPCardView);
        addcommandCardView = findViewById(R.id.addcommandCardView);
        addtestCardView = findViewById(R.id.addtestCardView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.id_menu);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        redCircle =findViewById(R.id.imageView2);
        redCircle.setVisibility(View.GONE);
        setupNavigationView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homeAd) {
                    Log.d("Navigation", "Home selected");
                    return true;
                } else if (itemId == R.id.profileAd) {
                    Log.d("Navigation", "Profile selected");
                    Intent intent = new Intent(home.this, Profil.class);
                    String userName = textView2.getText().toString();
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        AddTransCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, sign_up.class);
                intent.putExtra("fromHome", true);
                intent.putExtra("caller", "add_trans");
                startActivityForResult(intent, REQUEST_CODE_ADD_TRANSPORTER);
            }
        });

        addChefPCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, sign_up.class);
                intent.putExtra("caller", "add_chef");
                startActivity(intent);
            }
        });

        addcommandCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, addcmd.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_COMMAND);
            }
        });

        addtestCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, addTest.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileInformation();
    }

    private void updateProfileInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchCurrentUserProfileFromFirebase();
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

    private void fetchCurrentUserProfileFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = snapshot.child("name").getValue(String.class);
                        String poste = snapshot.child("poste").getValue(String.class);
                        if ("Admin".equals(poste)) {
                            textView2.setText(userName);
                            textView3.setText(poste);
                        } else {
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(home.this, "Error fetching user profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                } else if (itemId == R.id.nav_list_tests) {
                    openListTestsActivity();
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
            }
        });
    }

    private void openInfo() {
        Intent intent = new Intent(home.this, info.class);
        startActivity(intent);
    }

    public void openNotification(View view){
        Intent intent = new Intent(home.this, Notification.class);
        startActivity(intent);
    }

    private void openLogo() {
        Intent intent = new Intent(home.this, LogoAnimation.class);
        startActivity(intent);
    }

    private void openSetting() {
        Intent intent = new Intent(home.this, setting.class);
        startActivity(intent);
    }

    private void openListTestsActivity() {
        Intent intent = new Intent(home.this, listTests.class);
        startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_ADD_TEST);
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
                                Intent intent = new Intent(home.this, home.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(home.this, HomeChef.class);
                                startActivity(intent);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(home.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(home.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = getSharedPreferences("checkBox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("Remember", false);
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
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
        }else if (requestCode == REQUEST_CODE_ADD_TEST) {
            if (resultCode == RESULT_OK) {
                showNewTestNotification();
            }
        }
    }
        private void showNewTestNotification() {
            String channelId = "NEW_TEST_NOTIFICATION_CHANNEL";
            String channelName = "New Test Notification Channel";
            String channelDescription = "Notification for new tests";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.bell)
                    .setContentTitle("New Test Added")
                    .setContentText("A new test has been successfully added.")
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
            notificationManager.notify(2, builder.build());
        }

    }

