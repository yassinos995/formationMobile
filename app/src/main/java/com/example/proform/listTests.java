package com.example.proform;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.model.User;
import com.example.proform.model.UserTest;
import com.example.proform.model.UserTestAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private DatabaseReference testsRef;
    private DatabaseReference usersRef;
    private ValueEventListener testsListener;
    private ValueEventListener usersListener;

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

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (testsRef != null && testsListener != null) {
            testsRef.addValueEventListener(testsListener);
        }
        if (usersRef != null && usersListener != null) {
            usersRef.addValueEventListener(usersListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (testsRef != null && testsListener != null) {
            testsRef.removeEventListener(testsListener);
        }
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    private void fetchTests() {
        testsRef = FirebaseDatabase.getInstance().getReference("tests");

        testsListener = new ValueEventListener() {
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
        };

        testsRef.addValueEventListener(testsListener);
    }

    private void fetchUsers(Set<String> transporterIds, DataSnapshot testsSnapshot) {
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersListener = new ValueEventListener() {
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
                                    if (testReconnaissance != null && !testReconnaissance.equals("Unknown") && !testReconnaissance.equals("In Progress")) {
                                        if (user.getName().equals(testReconnaissance.trim())) {
                                            userTest.setTestReconnaissance("Right");
                                        } else {
                                            userTest.setTestReconnaissance("Unknown");
                                        }
                                    }
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
        };

        usersRef.addValueEventListener(usersListener);
    }


    private void attachUsersListener() {
        if (usersListener != null) {
            usersRef.addValueEventListener(usersListener);
        }
    }
    private void detachUsersListener() {
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
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
        Intent intent = new Intent(listTests.this, info.class);
        startActivity(intent);
    }

    private void openListTestsActivity() {
        Intent intent = new Intent(listTests.this, listTests.class);
        startActivity(intent);
    }

    private void openSetting() {
        Intent intent = new Intent(listTests.this, setting.class);
        startActivity(intent);
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable background = new ColorDrawable(Color.RED);
        private final Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete);

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (!userTestList.isEmpty() && position >= 0 && position < userTestList.size()) {
                if (direction == ItemTouchHelper.LEFT) {
                    showPasswordDialog(position);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    showUpdateDialog(position);
                } else {
                    Log.e("SwipeToDelete", "Invalid position: " + position);
                }
            } else {
                Log.e("SwipeToDelete", "User list is empty");
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getHeight();
            boolean isCancelled = dX == 0 && !isCurrentlyActive;
            if (isCancelled) {
                clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                return;
            }
            if (dX > 0) {
                // Code for swiping right, if needed
            } else {
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                int deleteIconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconMargin = (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + deleteIcon.getIntrinsicHeight();
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
            Paint paint = new Paint();
            paint.setColor(((ColorDrawable) background).getColor());
            c.drawRect(left, top, right, bottom, paint);
        }

        private void showPasswordDialog(final int position) {
            final EditText input = new EditText(listTests.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(listTests.this);
            builder.setView(input);
            builder.setMessage("Enter Your Password:")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = input.getText().toString().trim();
                            if (!TextUtils.isEmpty(password)) {
                                validatePassword(position, password);
                            } else {
                                Toast.makeText(listTests.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemChanged(position);
                        }
                    })
                    .show();
        }
    }

    private void validatePassword(final int position, String enteredPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                deleteTest(position);
                            } else {
                                Toast.makeText(listTests.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position);
                            }
                        }
                    });
        } else {
            Toast.makeText(listTests.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position);
        }
    }

    private void deleteTest(int position) {
        UserTest userTest = userTestList.get(position);
        String transporterId = userTest.getIdTransporter();

        DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");
        Query testQuery = testsRef.orderByChild("idTransporter").equalTo(transporterId);
        testQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot testSnapshot : dataSnapshot.getChildren()) {
                    testSnapshot.getRef().removeValue();
                }
                userTestList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(listTests.this, "Test deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
                Toast.makeText(listTests.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(int position) {
        // Implement the update dialog logic here
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
