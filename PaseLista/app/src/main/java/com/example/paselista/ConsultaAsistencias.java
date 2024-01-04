package com.example.paselista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class ConsultaAsistencias extends AppCompatActivity {

    FirebaseFirestore db;
    private RecyclerView recyclerView;
    TextView textViewNoData, txtNombreMateria;
    Button btnConsultFecha;

    private String materiaId1;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_asistencias);

        String materiaId = getIntent().getStringExtra("materiaId");

        materiaId1 = materiaId;

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewAsistencias);
        textViewNoData = findViewById(R.id.textViewNoData);
        txtNombreMateria = (TextView) findViewById(R.id.txtMateria);

        btnConsultFecha = (Button) findViewById(R.id.btnConsultFecha);

        obtenerNombreMateria(materiaId);

        btnConsultFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateSelectedDate();

                String formattedDate = getFormattedDate(selectedDate);
                consultarAsistenciasPorFecha(formattedDate, materiaId1);
                Log.e("Materia", "La materia es: " + materiaId1);
                Log.e("Fecha", "La fecha es: " + formattedDate);
            }
        };

        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private void updateSelectedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnConsultFecha.setText(sdf.format(selectedDate.getTime()));
        //textViewNoData.setText(sdf.format(selectedDate.getTime()));
    }

    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void consultarAsistenciasPorFecha(String fecha, String idMateria) {
        db.collection("Asistencias")
                .document(fecha)
                .collection(idMateria)
                .document("registro")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = documentSnapshot.getData();
                            Log.d("ConsultaAsistencias", "Data: " + data);

                            if (data != null && !data.isEmpty()) {
                                ArrayList<AlumnoModel> asistenciasList = new ArrayList<>();

                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    if (entry.getValue() instanceof Boolean) {
                                        Boolean asistenciaValue = (Boolean) entry.getValue();
                                        String nombreAlumnoId = entry.getKey();
                                        obtenerDatosAlumno(nombreAlumnoId, asistenciaValue, asistenciasList);
                                    }
                                }

                                Log.d("ConsultaAsistencias", "AsistenciasList size: "
                                        + asistenciasList.size());

                                recyclerView.setLayoutManager(new LinearLayoutManager(ConsultaAsistencias.this));
                                AsistenciasAdapter adapter = new AsistenciasAdapter(asistenciasList);
                                recyclerView.getRecycledViewPool().clear();
                                recyclerView.setAdapter(adapter);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                textViewNoData.setVisibility(View.VISIBLE);
                                textViewNoData.setText("No hay datos de asistencias " +
                                        "para esta materia");
                            }
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            textViewNoData.setVisibility(View.VISIBLE);
                            textViewNoData.setText("No hay datos de asistencias para esta" +
                                    " fecha y materia");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error getting documents", e);
                    }
                });
    }

    private void obtenerDatosAlumno(String alumnoId, Boolean asistenciaValue, ArrayList<AlumnoModel> asistenciasList) {
        db.collection("Alumnos").document(alumnoId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String nombreAlumno = documentSnapshot.getString("name");
                            Log.d("ConsultaAsistencias", "Nombre: " + nombreAlumno);
                            AlumnoModel alumno = new AlumnoModel(nombreAlumno, asistenciaValue);
                            asistenciasList.add(alumno);
                            recyclerView.getRecycledViewPool().clear();
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", "Error getting document", e);
                    }
                });
    }


    private void obtenerNombreMateria(String materiaId) {
        db.collection("Materias").document(materiaId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String nombreMateria = documentSnapshot.getString("name");
                            txtNombreMateria.setText(nombreMateria);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", "Error getting document", e);
                    }
                });
    }
}
