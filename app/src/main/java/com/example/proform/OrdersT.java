package com.example.proform;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.model.CommandAdapter;
import com.example.proform.model.commande;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersT extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommandAdapter adapter;
    private DatabaseReference databaseReference;

    private static final int PAGE_SIZE = 10;
    private String transporterUid;
    private Query commandQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_t);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            transporterUid = user.getUid();
            setupCommandQuery();
        } else {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupCommandQuery() {
        databaseReference = FirebaseDatabase.getInstance().getReference("commands");
        commandQuery = databaseReference.orderByChild("idtransporter").equalTo(transporterUid).limitToLast(PAGE_SIZE);

        commandQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<commande> commands = new ArrayList<>();
                List<String> commandIds = new ArrayList<>(); // Create a list to hold command IDs
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    commande command = snapshot.getValue(commande.class);
                    commands.add(command);
                    commandIds.add(snapshot.getKey()); // Add the command ID to the list
                }
                initAdapter(OrdersT.this, commands, commandIds); // Pass both lists to the adapter initialization method
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OrdersT.this, "Failed to retrieve commands: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initAdapter(Context context, List<commande> commands, List<String> commandIds) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    GetTokenResult tokenResult = task.getResult();
                    if (tokenResult != null) {
                        adapter = new CommandAdapter(context);
                        adapter.setCommands(commands, commandIds);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                }
            });
        }
    }
}