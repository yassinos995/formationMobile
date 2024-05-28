package com.example.proform.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proform.R;
import java.util.ArrayList;
import java.util.List;

public class UserTestAdapter extends RecyclerView.Adapter<UserTestAdapter.ViewHolder> {

    private Context context;
    private List<UserTest> userTestList;

    public UserTestAdapter(Context context, List<UserTest> userTestList) {
        this.context = context;
        this.userTestList = userTestList != null ? userTestList : new ArrayList<>();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = R.layout.list_itemtest;
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTest userTest = userTestList.get(position);
        holder.bind(userTest);
    }
    @Override
    public int getItemCount() {
        return userTestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView idTransporterTextView;
        TextView testAlcoolTextView;
        TextView testReconnaissanceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.EmailTextView);
            idTransporterTextView = itemView.findViewById(R.id.IdTTextView);
            testAlcoolTextView = itemView.findViewById(R.id.ConTextView);
            testReconnaissanceTextView = itemView.findViewById(R.id.RecTextView);
        }

        public void bind(UserTest userTest) {
            nameTextView.setText(userTest.getName());
            emailTextView.setText(userTest.getEmail());
            idTransporterTextView.setText(userTest.getIdTransporter());
            testAlcoolTextView.setText(userTest.getTestAlcool());
            testReconnaissanceTextView.setText(userTest.getTestReconnaissance());
        }
    }
}
