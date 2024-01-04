package com.example.paselista;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MateriaViewHolder extends RecyclerView.ViewHolder {
    private TextView tvName;
    public MateriaViewHolder(@NonNull View itemView) {
        super(itemView);
        tvName = itemView.findViewById(R.id.tvName);
    }

    public void bind(MateriaModel model) {
        if (model != null) {
            Log.d("MateriaViewHolder", "bind: Name=" + model.getName());

            tvName.setText(model.getName());
        } else {
            Log.e("CitaViewHolder", "bind: Model is null");
        }
    }
}
