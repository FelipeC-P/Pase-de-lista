package com.example.paselista;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlumnoViewHolder extends RecyclerView.ViewHolder {
    private TextView txtNombreAlumno;
    private CheckBox checkBoxAsistencia;

    public AlumnoViewHolder(@NonNull View itemView) {
        super(itemView);

        txtNombreAlumno = itemView.findViewById(R.id.txtNombreAlumno);
        checkBoxAsistencia = itemView.findViewById(R.id.chkAsistencia);
    }

    public void bind(AlumnoModel alumno) {
        if (alumno != null) {
            Log.d("AlumnoViewHolder", "bind: Name=" + alumno.getName());

            txtNombreAlumno.setText(alumno.getName());
            checkBoxAsistencia.setChecked(alumno.isAsistencia());

            checkBoxAsistencia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alumno.setAsistencia(checkBoxAsistencia.isChecked());
                }
            });
        } else {
            Log.e("AlumnoViewHolder", "bind: Model is null");
        }
    }
}