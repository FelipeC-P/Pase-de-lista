package com.example.paselista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<MateriaModel, MateriaViewHolder> adapter;
    private LinearLayout layoutAddClassOptions;
    private EditText editTextClassName;
    Button btnAddClass, btnConfirmAddClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();
        layoutAddClassOptions = findViewById(R.id.layoutAddClassOptions);
        btnAddClass = (Button) findViewById(R.id.btnAddClass);
        btnConfirmAddClass = (Button) findViewById(R.id.btnConfirmAddClass);
        editTextClassName = (EditText) findViewById(R.id.editTextClassName);

        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAddClassOptions.setVisibility(View.VISIBLE);
            }
        });

        btnConfirmAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatosEnFirebase();
                layoutAddClassOptions.setVisibility(View.GONE);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setupRecyclerView();
    }

    private void guardarDatosEnFirebase() {
        final String nombre = editTextClassName.getText().toString().trim();

        if (!nombre.isEmpty()) {
            CollectionReference materiasCollection = db.collection("Materia");

            materiasCollection.whereEqualTo("name", nombre)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    guardarMateria(nombre);
                                } else {
                                    Toast.makeText(MainActivity.this, "Ya existe una " +
                                                    "materia con ese nombre",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Error", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Completa los campos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarMateria(final String nombre) {
        final String materiaId = db.collection("Materias").document().getId();

        Map<String, Object> materia = new HashMap<>();
        materia.put("name", nombre);

        db.collection("Materias").document(materiaId)
                .set(materia)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editTextClassName.setText("");
                        Log.d("Success", "DocumentSnapshot successfully written!");
                        Toast.makeText(MainActivity.this, "¡Materia Guardada!",
                                Toast.LENGTH_SHORT).show();
                        recyclerView.getRecycledViewPool().clear();
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

    private void setupRecyclerView() {
        Query query = db.collection("Materias");

        FirestoreRecyclerOptions<MateriaModel> options = new FirestoreRecyclerOptions.Builder<MateriaModel>()
                .setQuery(query, MateriaModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<MateriaModel, MateriaViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MateriaViewHolder holder, int position,
                                            @NonNull MateriaModel model) {
                holder.bind(model);
                // Agregar OnClickListener al ViewHolder
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String materiaId = getSnapshots().getSnapshot(position).getId();

                        Intent intent = new Intent(MainActivity.this, PaseLista.class);
                        intent.putExtra("materiaId", materiaId);
                        startActivity(intent);

                        finish();
                    }
                });
            }

            @NonNull
            @Override
            public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new ViewHolder
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_materia,
                        parent, false);
                return new MateriaViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable background = new ColorDrawable(Color.RED);
        private final Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this,
                R.drawable.ic_delete);

        public SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            adapter.getSnapshots().getSnapshot(position).getReference().delete();
            Toast.makeText(MainActivity.this, "Materia eliminada", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right(derecha)
                int iconLeft = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getLeft() + iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft()
                        + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left(izquierda)
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else {
                background.setBounds(0, 0, 0, 0);
            }
            background.draw(c);
            deleteIcon.draw(c);
        }
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