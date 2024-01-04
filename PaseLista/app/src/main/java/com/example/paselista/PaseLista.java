package com.example.paselista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaseLista extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseFirestore db;
    private FirestoreRecyclerAdapter<AlumnoModel, AlumnoViewHolder> adapter;
    private LinearLayout layoutAddAlumnoOptions;
    EditText editTextAlumno;
    TextView txtNombreMateria;
    Button btnAddClass, btnConfirmAddAlumno, btnGuardarAsistencia, btnConsultaAsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pase_lista);

        String materiaId = getIntent().getStringExtra("materiaId");

        recyclerView = findViewById(R.id.recyclerViewAlumnos);
        db = FirebaseFirestore.getInstance();
        layoutAddAlumnoOptions = findViewById(R.id.layoutAddAlumnoOptions);
        btnAddClass = (Button) findViewById(R.id.btnAddAlumno);
        btnConfirmAddAlumno = (Button) findViewById(R.id.btnConfirmAddAlumno);
        editTextAlumno = (EditText) findViewById(R.id.editTextAlumno);
        txtNombreMateria = (TextView) findViewById(R.id.txtMateria);
        btnGuardarAsistencia = findViewById(R.id.btnGuardarAsistencia);
        btnConsultaAsis = (Button) findViewById(R.id.btnConsultaAsis);

        setupRecyclerView(materiaId);
        obtenerNombreMateria(materiaId);


        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAddAlumnoOptions.setVisibility(View.VISIBLE);
            }
        });

        btnConfirmAddAlumno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreAlumno = editTextAlumno.getText().toString().trim();
                agregarAlumnoAMateria(materiaId, nombreAlumno);
                layoutAddAlumnoOptions.setVisibility(View.GONE);
            }
        });

        btnGuardarAsistencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarAsistencia(materiaId);
            }
        });

        btnConsultaAsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaseLista.this, ConsultaAsistencias.class);
                intent.putExtra("materiaId", materiaId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void agregarAlumnoAMateria(String materiaId, String nombreAlumno) {
        final String alumnoId = FirebaseFirestore.getInstance().collection("Alumnos").document().getId();

        Map<String, Object> alumno = new HashMap<>();
        alumno.put("name", nombreAlumno);
        alumno.put("materiaId", materiaId);

        FirebaseFirestore.getInstance().collection("Alumnos").document(alumnoId)
                .set(alumno)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editTextAlumno.setText("");
                        Toast.makeText(PaseLista.this, "Alumno agregado a la materia",
                                Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error writing document", e);
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

    private void setupRecyclerView(String materiaId) {
        Query query = db.collection("Alumnos")
                .whereEqualTo("materiaId", materiaId);

        FirestoreRecyclerOptions<AlumnoModel> options = new FirestoreRecyclerOptions.Builder<AlumnoModel>()
                .setQuery(query, AlumnoModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<AlumnoModel, AlumnoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AlumnoViewHolder holder, int position,
                                            @NonNull AlumnoModel model) {
                // Bind the data to the ViewHolder
                holder.bind(model);
            }

            @NonNull
            @Override
            public AlumnoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new ViewHolder
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alumno,
                        parent, false);
                return new AlumnoViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    private void guardarAsistencia(String materiaId) {
        String idDia = obtenerIdDia();
        final Map<String, Object> asistencias = new HashMap<>();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            AlumnoModel alumno = adapter.getItem(i);
            String alumnoId = adapter.getSnapshots().getSnapshot(i).getId();
            boolean asistencia = alumno.isAsistencia();

            asistencias.put(alumnoId, asistencia);
        }

        FirebaseFirestore.getInstance().collection("Asistencias")
                .document(obtenerIdDia())
                .collection(materiaId)
                .document("registro")
                .set(asistencias)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PaseLista.this, "Asistencias guardadas",
                                Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error writing document", e);
                    }
                });
    }

    private String obtenerIdDia() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
