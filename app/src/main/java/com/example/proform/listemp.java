package com.example.proform;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.model.User;
import com.example.proform.model.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class listemp extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseAuth mAuth;
    private ImageButton menubuttonL;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean testD;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UID = "uid";
    private TextView listEmployersTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listemp);
        mAuth = FirebaseAuth.getInstance();
        testD=false;
        adapter = new UserAdapter(this, userList);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        menubuttonL=findViewById(R.id.id_menuL);
        drawerLayout = findViewById(R.id.drawer_layout_listemp);
        navigationView = findViewById(R.id.nav_view);
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);
        setupNavigationView();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            saveUidToSharedPreferences(currentUserUid);
        }
        menubuttonL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            saveUidToSharedPreferences(currentUserUid);

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUid);
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            String currentUserRole = currentUser.getPoste();
                            usersRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    userList.clear();
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        User user = userSnapshot.getValue(User.class);
                                        if (user != null && user.getPoste() != null && user.getCin() != null) {
                                            if (currentUserUid.equals(user.getCin())) {
                                                continue;
                                            }
                                            if (user.getPoste().equals("Admin")) {
                                                continue;
                                            }
                                            if (currentUserRole.equals("Chef")) {

                                                if (!user.getPoste().equals("Transporter")) {
                                                    continue;
                                                }
                                            }
                                            userList.add(user);
                                        } else {
                                            Log.e("UserSnapshot", "Null values found in user object or its properties");
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(listemp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(listemp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("CurrentUser", "Current user is null");
        }
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
                    openSettingActivity();
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
        Intent intent = new Intent(listemp.this, info.class);
        startActivity(intent);
    }

    private void openSettingActivity() {
        Intent intent = new Intent(listemp.this, setting.class);
        startActivity(intent);
    }

    private void openListTestsActivity() {
        Intent intent = new Intent(listemp.this, listTests.class);
        startActivity(intent);
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable background = new ColorDrawable(Color.RED);
        private final ColorDrawable backgroundU = new ColorDrawable(Color.GRAY);
        private final Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete);
        private final Drawable updateIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.updatei);
        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;}
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (!userList.isEmpty() && position >= 0 && position < userList.size()) {
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
                return;}
            if (dX > 0) {
                backgroundU.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                backgroundU.draw(c);
                int updateIconTop = itemView.getTop() + (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                int updateIconMargin = (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                int updateIconLeft = itemView.getLeft() + updateIconMargin;
                int updateIconRight = itemView.getLeft() + updateIconMargin + updateIcon.getIntrinsicWidth();
                int updateIconBottom = updateIconTop + updateIcon.getIntrinsicHeight();
                updateIcon.setBounds(updateIconLeft, updateIconTop, updateIconRight, updateIconBottom);
                updateIcon.draw(c);
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
            final EditText input = new EditText(listemp.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(listemp.this);
            builder.setView(input);
            builder.setMessage("Enter Your Password:")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = input.getText().toString().trim();
                            if (!TextUtils.isEmpty(password)) {
                                validatePassword(position, password);
                            } else {
                                Toast.makeText(listemp.this, "Please enter your password", Toast.LENGTH_SHORT).show();
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

        private void validatePassword(final int position , String enteredPassword) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    deleteUser(position);
                                } else {
                                    Toast.makeText(listemp.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                    adapter.notifyItemChanged(position);
                                }
                            }
                        });
            } else {
                Toast.makeText(listemp.this,"User not authenticated", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        }
        private void deleteUser(final int position) {
            final User userToDelete = userList.get(position);
            final ProgressDialog progressDialog = ProgressDialog.show(listemp.this, "", "Please wait deleting...", true);

            String userEmail = userToDelete.getEmail();
            String userPassword = userToDelete.getPassword();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    final String userUID = firebaseUser.getUid();
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                                    databaseReference.child(userUID).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (error == null) {
                                                userList.remove(userToDelete);
                                                adapter.notifyItemRemoved(position);
                                                adapter.notifyDataSetChanged();
                                                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(listemp.this, "User Deleted", Toast.LENGTH_SHORT).show();
                                                            signInStoredUser();
                                                        } else {
                                                            Toast.makeText(listemp.this, "Failed to delete user from Firebase Authentication: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(listemp.this, "Failed to delete user data from Realtime Database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(listemp.this, "Failed to get current user", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(listemp.this, "Failed to authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void showUpdateDialog(int position) {
        final EditText input = new EditText(listemp.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(listemp.this);
        builder.setView(input);
        builder.setMessage("Enter Your Password:")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(password)) {
                            validatePassword2(position, password);
                        } else {
                            Toast.makeText(listemp.this, "Please enter your password", Toast.LENGTH_SHORT).show();
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
    private void validatePassword2(int position, String enteredPassword) {
        User user = userList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                openUpdate(user);
                            } else {
                                Toast.makeText(listemp.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position);
                            }
                        }
                    });
        } else {
            Toast.makeText(listemp.this,"User not authenticated", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position);
        }

    }
    private void openUpdate(User user) {
        Intent intent = new Intent(listemp.this, updatep.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
    private void signInStoredUser() {
        String storedUid = getUidFromSharedPreferences();
        if (storedUid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(storedUid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User storedUser = dataSnapshot.getValue(User.class);
                        if (storedUser != null) {
                            String email = storedUser.getEmail();
                            String password = storedUser.getPassword();
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                Toast.makeText(listemp.this, "Failed to sign in user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                Toast.makeText(listemp.this, "the email is : "  + email+"and password is"+ password, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(listemp.this, "Stored user data is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(listemp.this, "User data does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(listemp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // UID not available in SharedPreferences
            Toast.makeText(listemp.this, "Stored UID not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }
    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);}
    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);}
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
                                Intent intent = new Intent(listemp.this, home.class);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(listemp.this, HomeChef.class);
                                startActivity(intent);
                            }}
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(listemp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(listemp.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void saveUidToSharedPreferences(String uid) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_UID, uid);
        editor.apply();
    }

    private String getUidFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_UID, null);
    }
}