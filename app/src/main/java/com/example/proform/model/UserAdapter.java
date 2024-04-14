package com.example.proform.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private OnSwipeListener onSwipeListener;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.onSwipeListener = onSwipeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
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
