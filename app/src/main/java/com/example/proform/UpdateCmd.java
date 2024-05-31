package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.proform.model.commande;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateCmd extends AppCompatActivity {
    private EditText dateEdit;
    private EditText descEdit;
    private EditText destEdit;
    private Spinner transporterSpinner;
    private Button updateButton;
    private Button cancelButton;
    private DatabaseReference commandRef;
    private DatabaseReference usersRef;
    private commande originalCommand;
    private Map<String, String> transporterNameToIdMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_cmd);

        dateEdit = findViewById(R.id.dateEdit);
        descEdit = findViewById(R.id.descEdit);
        destEdit = findViewById(R.id.destEdit);
        transporterSpinner = findViewById(R.id.transporterSpinner);
        updateButton = findViewById(R.id.buttonSubmit);
        cancelButton = findViewById(R.id.buttonCancel);

        updateButton.setEnabled(false); // Disable the button initially

        originalCommand = (commande) getIntent().getSerializableExtra("commande");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        String commandId = getIntent().getStringExtra("commandId");
        if (commandId != null) {
            commandRef = FirebaseDatabase.getInstance().getReference("commands").child(commandId);
            commandRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        commande command = dataSnapshot.getValue(commande.class);
                        if (command != null) {
                            originalCommand = command; // Ensure originalCommand is set
                            populateFields(command);
                            addTextWatchers(); // Add text watchers after fields are populated
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UpdateCmd.this, "Failed to retrieve command data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No command ID found", Toast.LENGTH_SHORT).show();
        }

        updateButton.setOnClickListener(v -> updateCommand());

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateCmd.this, listcommand.class);
            startActivity(intent);
        });
    }

    private void addTextWatchers() {
        // Adding text watchers to detect changes
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButton.setEnabled(hasCommandChanged());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        dateEdit.addTextChangedListener(textWatcher);
        descEdit.addTextChangedListener(textWatcher);
        destEdit.addTextChangedListener(textWatcher);
    }

    private void updateCommand() {
        String date = dateEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();
        String dest = destEdit.getText().toString().trim();
        String selectedTransporterName = transporterSpinner.getSelectedItem().toString();
        String selectedTransporterId = transporterNameToIdMap.get(selectedTransporterName);  // Get the UID from the map

        commandRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    commande existingCommand = dataSnapshot.getValue(commande.class);
                    if (existingCommand != null) {
                        existingCommand.setDateLimite(date);
                        existingCommand.setDesc(desc);
                        existingCommand.setDestination(dest);
                        existingCommand.setIdtransporter(selectedTransporterId);  // Use UID for idtransporter
                        commandRef.setValue(existingCommand, (error, ref) -> {
                            if (error == null) {
                                Toast.makeText(UpdateCmd.this, "Command updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(UpdateCmd.this, "Failed to update command: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateCmd.this, "Failed to retrieve command data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void populateFields(commande command) {
        String transporterId = command.getIdtransporter();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> transporterNames = new ArrayList<>();
                int selectedIndex = -1;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.child("name").getValue(String.class);
                    if (userId != null && userName != null) {
                        transporterNames.add(userName);
                        transporterNameToIdMap.put(userName, userId);  // Map name to UID
                        if (userId.equals(transporterId)) {
                            selectedIndex = transporterNames.size() - 1;
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdateCmd.this, android.R.layout.simple_spinner_item, transporterNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                transporterSpinner.setAdapter(adapter);
                if (selectedIndex >= 0) {
                    transporterSpinner.setSelection(selectedIndex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateCmd.this, "Failed to retrieve users data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dateEdit.setText(command.getDateLimite());
        descEdit.setText(command.getDesc());
        destEdit.setText(command.getDestination());
    }


    private boolean hasCommandChanged() {
        if (originalCommand == null) {
            return false;
        }
        String newDate = dateEdit.getText().toString().trim();
        String newDesc = descEdit.getText().toString().trim();
        String newDest = destEdit.getText().toString().trim();
        String newTransporter = transporterSpinner.getSelectedItem() != null ? transporterSpinner.getSelectedItem().toString() : "";
        String newTransporterId = transporterNameToIdMap.get(newTransporter);

        return !newDate.equals(originalCommand.getDateLimite())
                || !newDesc.equals(originalCommand.getDesc())
                || !newDest.equals(originalCommand.getDestination())
                || (newTransporterId != null && !newTransporterId.equals(originalCommand.getIdtransporter()));
    }

}
