package com.example.proform;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.model.User;
import com.example.proform.model.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class listemp extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listemp);

        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        // Fetch authenticated users except admin
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        Log.d("UserDebug", "Name: " + user.getName() + ", isAdmin: " + user.isAdmin());

                        if (user.getPoste().equals("Admin")) {
                            user.setAdmin(true);
                        } else {
                            user.setAdmin(false);
                        }
                        if (!user.isAdmin()) {
                            userList.add(user);
                        }
                    }
                }
                Log.d("UserListDebug", "UserList size: " + userList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(listemp.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Attach swipe-to-delete functionality to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Swipe-to-delete callback class
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
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (!userList.isEmpty()) {
                // Perform operations on the list
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < userList.size()) {
                    // Access the list's elements safely
                    if (direction == ItemTouchHelper.LEFT) {
                        showDeleteConfirmationDialog(position);
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        Intent intent = new Intent(listemp.this, updatep.class);
                        intent.putExtra("user", userList.get(position));
                        startActivity(intent);
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    Log.e("SwipeToDelete", "Invalid position: " + position);
                }
            } else {
                Log.e("SwipeToDelete", "User list is empty");
            }

            int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                showDeleteConfirmationDialog(position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                Intent intent= new Intent(listemp.this,updatep.class);
                intent.putExtra("user", userList.get(position));
                startActivity(intent);
                adapter.notifyItemChanged(position);
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

            // Draw the appropriate background based on swipe direction
            if (dX > 0) {
                // Draw the gray background for update
                backgroundU.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                backgroundU.draw(c);

                // Calculate position of update icon
                int updateIconTop = itemView.getTop() + (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                int updateIconMargin = (itemHeight - updateIcon.getIntrinsicHeight()) / 2;
                int updateIconLeft = itemView.getLeft() + updateIconMargin;
                int updateIconRight = itemView.getLeft() + updateIconMargin + updateIcon.getIntrinsicWidth();
                int updateIconBottom = updateIconTop + updateIcon.getIntrinsicHeight();

                // Draw the update icon
                updateIcon.setBounds(updateIconLeft, updateIconTop, updateIconRight, updateIconBottom);
                updateIcon.draw(c);
            } else {
                // Draw the red background for delete
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // Calculate position of delete icon
                int deleteIconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconMargin = (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteIcon.getIntrinsicWidth();
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + deleteIcon.getIntrinsicHeight();

                // Draw the delete icon
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

        private void showDeleteConfirmationDialog(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(listemp.this);
            builder.setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteUser(position);
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



        private void deleteUser(final int position) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            final String userId = userList.get(position).getUserId();
            Log.d("UserID", userId);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                // Sign in with the credentials of the user being deleted
                FirebaseAuth.getInstance().signInWithEmailAndPassword(userList.get(position).getEmail(), userList.get(position).getPassword())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (firebaseUser != null) {
                                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // User authentication and deletion succeeded, now delete user data from database
                                                    databaseReference.child(userId).removeValue(new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                            if (error == null) {
                                                                userList.remove(position);
                                                                adapter.notifyItemRemoved(position);
                                                                Toast.makeText(listemp.this, "User deleted", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                // Error deleting user data from database
                                                                Toast.makeText(listemp.this, "Failed to delete user from Realtime Database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    // Error deleting user from Firebase Authentication
                                                    Toast.makeText(listemp.this, "Failed to delete user from Firebase Authentication: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    // Failed to authenticate user
                                    Toast.makeText(listemp.this, "Failed to authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }




    }



