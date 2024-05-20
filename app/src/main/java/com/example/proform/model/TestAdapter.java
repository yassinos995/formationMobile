package com.example.proform.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private Context context;
    private List<Test> testList;

    public TestAdapter(Context context, List<Test> testList) {
        this.context = context;
        this.testList = testList;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = testList.get(position);
        holder.bind(test);
        holder.goTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoTestDialog(test.getIdTransporter());
            }
        });
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public static class TestViewHolder extends RecyclerView.ViewHolder {
        private TextView idTransporterTextView;
        private TextView testAlcoolTextView;
        private TextView testReconnaissanceTextView;
        private ImageView goTestButton;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            idTransporterTextView = itemView.findViewById(R.id.id_transporter_text_view);
            testAlcoolTextView = itemView.findViewById(R.id.test_alcool_text_view);
            testReconnaissanceTextView = itemView.findViewById(R.id.test_reconnaissance_text_view);
            goTestButton = itemView.findViewById(R.id.go_test_button);
        }

        public void bind(Test test) {
            idTransporterTextView.setText(test.getIdTransporter());
            testAlcoolTextView.setText(test.getTestAlcool());
            testReconnaissanceTextView.setText(test.getTestReconnaissance());
        }
    }

    private void showGoTestDialog(String transporterUid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Go Test")
                .setMessage("Are you sure you want to initiate the test for this transporter?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initiateTestForTransporter(transporterUid);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void initiateTestForTransporter(String transporterUid) {
        DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");
        String testId = testsRef.push().getKey(); // Generate unique test ID
        Test test = new Test("transporterId", "testAlcoolValue", "testReconnaissanceValue");
        testsRef.child(testId).setValue(test)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Test initiated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to initiate test: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
