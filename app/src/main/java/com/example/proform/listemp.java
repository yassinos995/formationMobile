package com.example.proform;

import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class listemp extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseAuth mAuth;
    private boolean testD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listemp);

        mAuth = FirebaseAuth.getInstance();
         testD=false;
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

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null && user.getPoste().equals("Chef personnelle") && currentUser.getUid().equals(user.getUserId())) {
                            if (user.getPoste().equals("Transporter")) {
                                userList.add(user);
                            }
                        } else {
                            if (!user.isAdmin()) {
                                userList.add(user);
                            }
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

        private void showDeleteConfirmationDialog(final int position) {
            final EditText input = new EditText(listemp.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // Check if testD is false before showing the dialog
            if (!testD) {
                AlertDialog.Builder builder = new AlertDialog.Builder(listemp.this);
                builder.setView(input);
                builder.setMessage("Enter Admin Password:")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String adminPassword = input.getText().toString().trim();
                                if (!TextUtils.isEmpty(adminPassword)) {
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                    Query adminQuery = usersRef.orderByChild("poste").equalTo("Admin").limitToFirst(1);
                                    adminQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                String adminPasswordFromDB = userSnapshot.child("password").getValue(String.class);
                                                if (adminPassword.equals(adminPasswordFromDB)) {
                                                    deleteUser(position);
                                                    return;
                                                }
                                            }
                                            Toast.makeText(listemp.this, "Incorrect Admin Password", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemChanged(position);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(listemp.this, "Failed to retrieve Admin Password", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemChanged(position);
                                        }
                                    });
                                } else {
                                    Toast.makeText(listemp.this, "Please enter Admin Password", Toast.LENGTH_SHORT).show();
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
                testD = true;
            }
        }






        private void deleteUser(final int position) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            final String userId = userList.get(position).getUserId();
            Log.d("UserID", userId);
            ProgressDialog progressDialog = ProgressDialog.show(listemp.this, "", "Please wait...", true);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
                                                databaseReference.child(userId).removeValue(new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                        progressDialog.dismiss();
                                                            if (error == null) {
                                                            if (position >= 0 && position < userList.size()) {
                                                            userList.remove(position);
                                                             adapter.notifyItemRemoved(position);
                                                             testD=true;
                                                            Toast.makeText(listemp.this, "User deleted", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(listemp.this, "Failed to authenticate user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}