package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.proform.model.CommandAdapter;
import com.example.proform.model.commande;
import com.google.android.material.navigation.NavigationView;
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
    private ImageButton menubuttonLC;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listcommand);
        menubuttonLC=findViewById(R.id.id_menuLC);
        drawerLayout = findViewById(R.id.drawer_layout_listeCommand);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commandAdapter = new CommandAdapter(this,true);
        recyclerView.setAdapter(commandAdapter);
        setupNavigationView();
        databaseReference = FirebaseDatabase.getInstance().getReference("commands");

        retrieveCommands();

        menubuttonLC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
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
                    return true;
                } else if (itemId == R.id.nav_info) {
                    return true;
                } else if (itemId == R.id.nav_share) {
                    // shareApp();
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

    private void openListCommandsActivity() {
        Intent intent = new Intent(this, listcommand.class);
        startActivity(intent);
    }

    private void openListEmployersActivity() {
        Intent intent = new Intent(this, listemp.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void gohome() {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
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
