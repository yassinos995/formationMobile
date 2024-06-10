package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.example.proform.model.CommandAdapter;
import com.example.proform.model.User;
import com.example.proform.model.commande;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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
import java.util.List;

public class listcommand extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommandAdapter commandAdapter;
    private List<commande> commandList;
    private ImageButton menuButtonLC;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;
    private boolean isTransporter = false;
    private boolean isAdmin = false;
    private boolean isChef = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listcommand);
        menuButtonLC = findViewById(R.id.id_menuLC);
        drawerLayout = findViewById(R.id.drawer_layout_listeCommand);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.recyclerView);
        commandAdapter = new CommandAdapter(this);
        recyclerView.setAdapter(commandAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference("commands");
        checkUserRoleAndSetupNavigation();
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        editor.apply();
        menuButtonLC.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }


    private void checkUserRoleAndSetupNavigation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User userData = snapshot.getValue(User.class);
                        if (userData != null) {
                            isTransporter = "Transporter".equals(userData.getPoste());
                            isAdmin = "Admin".equals(userData.getPoste());
                            isChef = "Chef".equals(userData.getPoste());

                            MenuItem listEmployersMenuItem = navigationView.getMenu().findItem(R.id.nav_list_employers);
                            if (isChef) {
                                listEmployersMenuItem.setTitle("Liste Transporter");
                            } else {
                                listEmployersMenuItem.setTitle("Liste Employers");
                            }

                            setupNavigationView();
                            retrieveCommands();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("RoleCheck", "Failed to retrieve user data: " + error.getMessage());
                }
            });
        }
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
            }else if (itemId == R.id.nav_settings) {
                openSetting();
                return true;
            } else if (itemId == R.id.nav_list_tests) {
                openListTestsActivity();
                return true;
            }  else if (itemId == R.id.nav_info) {
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
        if (isTransporter) {
            navigationView.getMenu().findItem(R.id.nav_list_employers).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_list_tests).setVisible(false);
        }
    }

    private void openInfo() {
        Intent intent = new Intent(listcommand.this, info.class);
        startActivity(intent);
    }

    private void openSetting() {
        Intent intent = new Intent(listcommand.this, setting.class);
        startActivity(intent);
    }

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goHome() {
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
                            Intent intent;
                            if ("Admin".equals(currentUserRole)) {
                                intent = new Intent(listcommand.this, home.class);
                            } else if ("Chef".equals(currentUserRole)) {
                                intent = new Intent(listcommand.this, HomeChef.class);
                            } else {
                                intent = new Intent(listcommand.this, HomeTransporter.class);
                            }
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(listcommand.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(listcommand.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveCommands() {
        if (isAdmin || isChef) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commandList = new ArrayList<>();
                    List<String> commandIds = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        commande command = snapshot.getValue(commande.class);
                        if (command != null) {
                            String uid = snapshot.getKey();
                            command.setUid(uid);
                            commandList.add(command);
                            commandIds.add(uid);
                        }
                    }
                    commandAdapter.setCommands(commandList, commandIds);
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle cancellation
                }
            });
        } else {
            String transporterUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Query query = databaseReference.orderByChild("idtransporter").equalTo(transporterUid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commandList = new ArrayList<>();
                    List<String> commandIds = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        commande command = snapshot.getValue(commande.class);
                        if (command != null) {
                            String uid = snapshot.getKey();
                            command.setUid(uid);
                            commandList.add(command);
                            commandIds.add(uid);
                        }
                    }
                    commandAdapter.setCommands(commandList, commandIds);
                    if (!isTransporter) {
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
                        itemTouchHelper.attachToRecyclerView(recyclerView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable backgroundDelete = new ColorDrawable(Color.RED);
        private final ColorDrawable backgroundUpdate = new ColorDrawable(Color.GRAY);
        private final Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete);
        private final Drawable updateIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.updatei);

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            commande command = commandList.get(position);

            if (direction == ItemTouchHelper.LEFT) {
                showPasswordDialog(position);
            } else if (direction == ItemTouchHelper.RIGHT && !"Rejected".equals(command.getEtat())) {
                showUpdateDialog(position);
            } else {
                // If the command is rejected, notify the adapter to reset the item view
                commandAdapter.notifyItemChanged(position);
            }
        }

        private void showPasswordDialog(final int position) {
            final EditText input = new EditText(listcommand.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(listcommand.this);
            builder.setView(input);
            builder.setMessage("Enter Your Password:")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String password = input.getText().toString().trim();
                            if (!TextUtils.isEmpty(password)) {
                                validatePassword(position, password);
                            } else {
                                Toast.makeText(listcommand.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                                commandAdapter.notifyItemChanged(position);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            commandAdapter.notifyItemChanged(position);
                        }
                    })
                    .show();
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
                                    deleteCommand(position);
                                } else {
                                    Toast.makeText(listcommand.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                    commandAdapter.notifyItemChanged(position);
                                }
                            }
                        });
            } else {
                Toast.makeText(listcommand.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                commandAdapter.notifyItemChanged(position);
            }
        }

        private void deleteCommand(final int position) {
            String commandId = commandList.get(position).getUid();
            DatabaseReference commandRef = FirebaseDatabase.getInstance().getReference("commands").child(commandId);
            commandRef.removeValue((error, ref) -> {
                if (error == null) {
                    Toast.makeText(listcommand.this, "Command deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("DeleteCommand", "Failed to delete command: " + error.getMessage());
                    commandAdapter.notifyItemChanged(position);
                }
            });
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getHeight();

            if (dX > 0) {
                commande command = commandList.get(viewHolder.getAdapterPosition());
                if (!"Rejected".equals(command.getEtat())) {
                    backgroundUpdate.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                    backgroundUpdate.draw(c);
                    int updateIconTop = itemView.getTop() + (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                    int updateIconMargin = (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                    int updateIconLeft = itemView.getLeft() + updateIconMargin;
                    int updateIconRight = itemView.getLeft() + updateIconMargin + updateIcon.getIntrinsicWidth();
                    int updateIconBottom = updateIconTop + updateIcon.getIntrinsicHeight();
                    updateIcon.setBounds(updateIconLeft, updateIconTop, updateIconRight, updateIconBottom);
                    updateIcon.draw(c);
                }
            } else {
                backgroundDelete.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                backgroundDelete.draw(c);
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
    }

    private void showUpdateDialog(int position) {
        final EditText input = new EditText(listcommand.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(listcommand.this);
        builder.setView(input);
        builder.setMessage("Enter Your Password:")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(password)) {
                            validatePassword2(position, password);
                        } else {
                            Toast.makeText(listcommand.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                            commandAdapter.notifyItemChanged(position);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        commandAdapter.notifyItemChanged(position);
                    }
                })
                .show();
    }

    private void validatePassword2(int position, String enteredPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateCommand(position);
                            } else {
                                Toast.makeText(listcommand.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                commandAdapter.notifyItemChanged(position);
                            }
                        }
                    });
        } else {
            Toast.makeText(listcommand.this,"User not authenticated", Toast.LENGTH_SHORT).show();
            commandAdapter.notifyItemChanged(position);
        }
    }
    private void updateCommand(int position) {
        commande swipedCommand = commandList.get(position);
        String uid = swipedCommand.getUid();
        Intent intent = new Intent(listcommand.this, UpdateCmd.class);
        intent.putExtra("commandId", uid);
        startActivity(intent);
    }
    private void openListTestsActivity() {
        Intent intent = new Intent(this, listTests.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }
}