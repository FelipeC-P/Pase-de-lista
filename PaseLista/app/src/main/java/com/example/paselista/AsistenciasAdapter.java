package com.example.paselista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paselista.R;

import java.util.ArrayList;

public class AsistenciasAdapter extends RecyclerView.Adapter<AsistenciasAdapter.AsistenciaViewHolder> {

    private ArrayList<AlumnoModel> asistenciasList;

    public AsistenciasAdapter(ArrayList<AlumnoModel> asistenciasList) {
        this.asistenciasList = asistenciasList;
    }

    @NonNull
    @Override
    public AsistenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistencia, parent, false);
        return new AsistenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistenciaViewHolder holder, int position) {
        AlumnoModel alumno = asistenciasList.get(position);
        holder.bind(alumno);
    }

    @Override
    public int getItemCount() {
        return asistenciasList.size();
    }

    public static class AsistenciaViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewNombre;
        private TextView textViewAsistencia;

        public AsistenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewAsistencia = itemView.findViewById(R.id.textViewAsistencia);
        }

        public void bind(AlumnoModel alumno) {
            textViewNombre.setText(alumno.getName());
            textViewAsistencia.setText(alumno.isAsistencia() ? "Asistió" : "No Asistió");
        }
    }
}
