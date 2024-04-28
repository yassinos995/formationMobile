package com.example.proform;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proform.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class updatep extends AppCompatActivity {
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button updateButton;
    private Button cancelButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatep);

        nameEditText = findViewById(R.id.id_nameProfileU);
        emailEditText = findViewById(R.id.id_emailsProfileU);
        phoneEditText = findViewById(R.id.id_phoneProfileU);
        updateButton = findViewById(R.id.btn_edit_profileU);
        cancelButton = findViewById(R.id.btn_CancelProfileU);

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            nameEditText.setText(user.getName());
            emailEditText.setText(user.getEmail());
            phoneEditText.setText(user.getPhoneNumber());
        }

        updateButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newPhoneNumber = phoneEditText.getText().toString().trim();

            if (user != null) {
                userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUserId());
                userRef.child("name").setValue(newName);
                userRef.child("email").setValue(newEmail);
                userRef.child("phoneNumber").setValue(newPhoneNumber)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("UpdateDebug", "User profile updated successfully");
                                Toast.makeText(updatep.this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e("UpdateDebug", "Failed to update user profile: " + task.getException());
                                Toast.makeText(updatep.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }
}
