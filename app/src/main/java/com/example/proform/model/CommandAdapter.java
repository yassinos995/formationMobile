package com.example.proform.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proform.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.ViewHolder> {

    private Context context;
    private List<commande> commandList;
    private List<String> commandIds;

    public CommandAdapter(Context context) {
        this.context = context;
    }

    public void setCommands(List<commande> commandList, List<String> commandIds) {
        this.commandList = commandList;
        this.commandIds = commandIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (commandList != null && position < commandList.size()) {
            commande command = commandList.get(position);
            holder.dateTextView.setText("Date Limite: " + command.getDateLimite());
            holder.descTextView.setText("Description: " + command.getDesc());
            holder.destTextView.setText("Destination: " + command.getDestination());
            holder.etatTextView.setText("Etat: " + command.getEtat());
            holder.transporterTextView.setText("idtransporter: " + command.getIdtransporter());

            if ("Not Completed".equals(command.getEtat())) {
                holder.btnCompleted.setVisibility(View.VISIBLE);
                holder.btnRejected.setVisibility(View.VISIBLE);
                holder.btnCompleted.setOnClickListener(v -> updateEtat(position, "Completed"));
                holder.btnRejected.setOnClickListener(v -> updateEtat(position, "Rejected"));
            } else {
                holder.btnCompleted.setVisibility(View.GONE);
                holder.btnRejected.setVisibility(View.GONE);
            }

        }
    }


    private void updateEtat(int position, String etat) {
        if (position >= 0 && position < commandList.size()) {
            commande command = commandList.get(position);
            String commandId = commandIds.get(position);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("commands").child(commandId);
            databaseReference.child("etat").setValue(etat)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Etat updated successfully", Toast.LENGTH_SHORT).show();
                        command.setEtat(etat);
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update Etat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public int getItemCount() {
        return commandList == null ? 0 : commandList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, descTextView, destTextView, transporterTextView, etatTextView;
        Button btnCompleted, btnRejected;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            descTextView = itemView.findViewById(R.id.descTextView);
            destTextView = itemView.findViewById(R.id.destTextView);
            transporterTextView = itemView.findViewById(R.id.transporterTextView);
            etatTextView = itemView.findViewById(R.id.etatTextView);
            btnCompleted = itemView.findViewById(R.id.btnCompleted);
            btnRejected = itemView.findViewById(R.id.btnRejected);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
