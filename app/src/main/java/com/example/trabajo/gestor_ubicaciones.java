package com.example.trabajo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabajo.datos.Repositorio;
import com.example.trabajo.model.Ubicacion;
import com.google.firebase.firestore.FirebaseFirestore;

public class gestor_ubicaciones extends AppCompatActivity {

    private EditText ubiSensorEditText;
    private EditText descripcionEditText;
    private Button ingresarUbiButton;
    private Button verUbicacionesButton; // Botón para ver las ubicaciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestor_ubicaciones);

        // Inicio vistas
        ubiSensorEditText = findViewById(R.id.UbiSensor2);
        descripcionEditText = findViewById(R.id.descripc);
        ingresarUbiButton = findViewById(R.id.ingresarUbiButton);
        verUbicacionesButton = findViewById(R.id.btn_ver_ubicaciones); // Inicializar el botón de ver ubicaciones

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Listener para guardar ubicaciones
        ingresarUbiButton.setOnClickListener(v -> validarYGuardarUbicacion());

        // Listener para redirigir a la actividad que muestra ubicaciones
        verUbicacionesButton.setOnClickListener(v -> {
            Intent intent = new Intent(gestor_ubicaciones.this, listarUbicaciones.class);
            startActivity(intent);
        });
    }

    private void validarYGuardarUbicacion() {
        String nombre = ubiSensorEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "¡El nombre es OBLIGATORIO!", Toast.LENGTH_SHORT).show();
            return;
        } else if (nombre.length() < 5 || nombre.length() > 15) {
            Toast.makeText(this, "El nombre debe tener entre 5 y 15 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(descripcion) && descripcion.length() > 30) {
            Toast.makeText(this, "La descripción no debe ser tan larga (max 30 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Ubicacion
        Ubicacion ubicacion = new Ubicacion(nombre, descripcion);

        // Guardar en Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ubicaciones").document() // Si deseas un ID automático aleatorio
                .set(ubicacion)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(gestor_ubicaciones.this, "Ubicación guardada correctamente en base de datos", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(gestor_ubicaciones.this, "Error al guardar en la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // También guardar en el repositorio local (opcional)
        Repositorio.getInstance().agregarUbicacion(nombre, descripcion);

        // Mensaje de éxito local
        Toast.makeText(this, "Ubicación agregada correctamente", Toast.LENGTH_SHORT).show();

        // Cerrar la actividad después de guardar
        finish();
    }
}
