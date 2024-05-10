package com.example.proform;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.List;

public class UpdateCmd extends AppCompatActivity {
    private EditText dateEdit;
    private EditText descEdit;
    private EditText destEdit;
    private Spinner transporterSpinner;
    private Button updateButton;
    private DatabaseReference commandRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_cmd);

        dateEdit = findViewById(R.id.dateEdit);
        descEdit = findViewById(R.id.descEdit);
        destEdit = findViewById(R.id.destEdit);
        transporterSpinner = findViewById(R.id.transporterSpinner);
        updateButton = findViewById(R.id.buttonSubmit);

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
                            populateFields(command);
                        }}}
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UpdateCmd.this, "Failed to retrieve command data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No command ID found", Toast.LENGTH_SHORT).show();
        }
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCommand();
            }
        });
    }
    private void updateCommand() {
        String date = dateEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();
        String dest = destEdit.getText().toString().trim();
        String selectedTransporter = transporterSpinner.getSelectedItem().toString();
        commandRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    commande existingCommand = dataSnapshot.getValue(commande.class);
                    if (existingCommand != null) {
                        existingCommand.setDateLimite(date);
                        existingCommand.setDesc(desc);
                        existingCommand.setDestination(dest);
                        existingCommand.setIdtransporter(selectedTransporter);
                        commandRef.setValue(existingCommand, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    Toast.makeText(UpdateCmd.this, "Command updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(UpdateCmd.this, "Failed to update command: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
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


    private String getTransporterIdByName(String transporterName) {

        if ("Transporter A".equals(transporterName)) {
            return "1";
        } else if ("Transporter B".equals(transporterName)) {
            return "2";
        } else {
            return null;
        }
    }



    private void populateFields(commande command) {
        String transporterId = command.getIdtransporter();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> transporterIds = new ArrayList<>(); // List to store transporter UIDs
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.child("name").getValue(String.class);
                    if (userId != null && userName != null && userId.equals(transporterId)) {
                        transporterIds.add(userId); // Add the UID to the list
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdateCmd.this, android.R.layout.simple_spinner_item, transporterIds);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                transporterSpinner.setAdapter(adapter);
                transporterSpinner.setEnabled(false); // Disable the spinner
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

}