package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.proform.model.CommandAdapter;
import com.example.proform.model.commande;
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
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listcommand);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commandAdapter = new CommandAdapter(this,true);
        recyclerView.setAdapter(commandAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("commands");

        retrieveCommands();
    }

    private void retrieveCommands() {
        // Query Firebase to get all commands
        Query query = databaseReference.orderByKey();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commandList = new ArrayList<>();
                List<String> commandIds = new ArrayList<>(); // Create a list to hold command IDs
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Convert each DataSnapshot to a commande object and add it to the list
                    commande command = snapshot.getValue(commande.class);
                    commandList.add(command);
                    // Add the command ID to the list
                    commandIds.add(snapshot.getKey());
                }
                // Set the commands and their IDs to the adapter
                commandAdapter.setCommands(commandList, commandIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

}
