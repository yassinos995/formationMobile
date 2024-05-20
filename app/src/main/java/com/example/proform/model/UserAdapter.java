package com.example.proform.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proform.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private DatabaseReference testsRef;
    private OnSwipeListener onSwipeListener;
    private boolean isListForTests;

    public UserAdapter(Context context, List<User> userList, boolean isListForTests) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>();
        this.isListForTests = isListForTests;
        testsRef = FirebaseDatabase.getInstance().getReference("tests");
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isListForTests ? R.layout.list_itemtest : R.layout.list_item;
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
        if (isListForTests) {
            holder.plusIcon.setVisibility(View.VISIBLE);
            holder.plusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog(user);
                }
            });
        } else {
           // holder.plusIcon.setVisibility(View.GONE);
        }
    }
    private void showConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to add a test for " + user.getName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addTestForUser(user);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void addTestForUser(User user) {
        String userId = user.getUid();
        String testName = "Test";
        String testResult = null;
        Test test = new Test(userId, testResult, testName);
        String testId = testsRef.push().getKey();
        testsRef.child(testId).setValue(test);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameLabelTextView;
        TextView nameTextView;
        TextView posteLabelTextView;
        TextView posteTextView;
        ImageButton plusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabelTextView = itemView.findViewById(R.id.nameLabelTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            posteLabelTextView = itemView.findViewById(R.id.posteLabelTextView);
            posteTextView = itemView.findViewById(R.id.posteTextView);
            itemView.setOnClickListener(this);
        }

        public void bind(User user) {
            nameLabelTextView.setText("Name :");
            nameTextView.setText(user.getName());
            posteLabelTextView.setText("Poste :");
            posteTextView.setText(user.getPoste());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                onSwipeListener.onSwipe(position);
            }
        }
    }
    public interface OnSwipeListener {
        void onSwipe(int position);
    }
}
