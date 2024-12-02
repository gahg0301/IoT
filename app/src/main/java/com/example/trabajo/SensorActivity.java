package com.example.trabajo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajo.datos.Repositorio;
import com.example.trabajo.model.Sensor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;


import java.util.List;

public class SensorActivity extends AppCompatActivity {

    private EditText nombreEditText;
    private EditText descripcionEditText;
    private EditText idealEditText;
    private Button ingresarSensorButton;
    private Button verSensoresButton;
    private Button modificarSensorButton;
    private Button eliminarSensorButton;
    private Button buscarSensorButton;

    private Sensor sensorSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // Inicializar campos de texto y botones
        nombreEditText = findViewById(R.id.sensores);
        descripcionEditText = findViewById(R.id.modeloSensorEditText);
        idealEditText = findViewById(R.id.idealEditText);
        ingresarSensorButton = findViewById(R.id.ingresarSensorButton);
        verSensoresButton = findViewById(R.id.SensoresHistorial);
        modificarSensorButton = findViewById(R.id.ModificarSensorButton);
        eliminarSensorButton = findViewById(R.id.eliminarSensorButton);
        buscarSensorButton = findViewById(R.id.buscarSensorButton);

        // Spinner de Tipo de Sensor y Ubicación
        Spinner spinnerTipoSensor = findViewById(R.id.tipoSensorSpinner);
        Spinner spinnerUbicacion = findViewById(R.id.ubicacionSpinner);

        // Obtener datos desde el Repositorio
        List<String> tiposSensor = Repositorio.getInstance().obtenerTiposSensor();
        List<String> ubicaciones = Repositorio.getInstance().obtenerUbicaciones();

        // Configurar adaptadores para los Spinners
        ArrayAdapter<String> tipoSensorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposSensor);
        tipoSensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSensor.setAdapter(tipoSensorAdapter);

        ArrayAdapter<String> ubicacionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ubicaciones);
        ubicacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUbicacion.setAdapter(ubicacionAdapter);

        // Configurar botón para ingresar sensor
        ingresarSensorButton.setOnClickListener(v -> validarYGuardarDatosEnFirebase(spinnerTipoSensor, spinnerUbicacion));

        // Configurar botón para ver sensores agregados
        verSensoresButton.setOnClickListener(v -> {
            Intent intent = new Intent(SensorActivity.this, RecyclerVerSensoresActivity.class);
            startActivity(intent);
        });

        // Configurar botón de buscar sensor
        buscarSensorButton.setOnClickListener(v -> buscarSensor(spinnerTipoSensor, spinnerUbicacion));

        // Configurar botón para modificar sensor
        modificarSensorButton.setOnClickListener(v -> modificarSensor(spinnerTipoSensor, spinnerUbicacion));

        // Configurar botón para eliminar sensor
        eliminarSensorButton.setOnClickListener(v -> eliminarSensor());
    }

    private void validarYGuardarDatosEnFirebase(Spinner spinnerTipoSensor, Spinner spinnerUbicacion) {
        String nombre = nombreEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String idealStr = idealEditText.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        } else if (nombre.length() < 5 || nombre.length() > 15) {
            Toast.makeText(this, "El nombre debe tener entre 5 y 15 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(descripcion) && descripcion.length() > 30) {
            Toast.makeText(this, "La descripción no debe exceder los 30 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(idealStr)) {
            Toast.makeText(this, "El valor ideal es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        float ideal;
        try {
            ideal = Float.parseFloat(idealStr);
            if (ideal <= 0) {
                Toast.makeText(this, "El valor ideal debe ser un número positivo", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El valor ideal debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoSensor = spinnerTipoSensor.getSelectedItem().toString();
        String ubicacion = spinnerUbicacion.getSelectedItem().toString();

        // Crear objeto Sensor
        Sensor sensor = new Sensor(nombre, descripcion, ideal, tipoSensor, ubicacion);

        // Guardar en Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sensores").document()  // Creación de un nuevo documento sin especificar ID
                .set(sensor.toMap())  // Guardamos el sensor usando el método toMap()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(SensorActivity.this, "Sensor agregado a la base de datos correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SensorActivity.this, "Error al guardar en la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // También agregar al repositorio local (opcional)
        Repositorio.getInstance().agregarSensor(sensor);
    }

    private void buscarSensor(Spinner spinnerTipoSensor, Spinner spinnerUbicacion) {
        String nombre = nombreEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Por favor ingrese el nombre del sensor a buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sensores")
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Sensor no encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        // Obtener el primer sensor encontrado
                        sensorSeleccionado = queryDocumentSnapshots.getDocuments().get(0).toObject(Sensor.class);
                        if (sensorSeleccionado != null) {
                            nombreEditText.setText(sensorSeleccionado.getNombre());
                            descripcionEditText.setText(sensorSeleccionado.getDescripcion());
                            idealEditText.setText(String.valueOf(sensorSeleccionado.getIdeal()));

                            int tipoPos = ((ArrayAdapter<String>) spinnerTipoSensor.getAdapter()).getPosition(sensorSeleccionado.getTipoSensor());
                            spinnerTipoSensor.setSelection(tipoPos);

                            int ubicacionPos = ((ArrayAdapter<String>) spinnerUbicacion.getAdapter()).getPosition(sensorSeleccionado.getUbicacion());
                            spinnerUbicacion.setSelection(ubicacionPos);

                            Toast.makeText(this, "Sensor encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar el sensor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void modificarSensor(Spinner spinnerTipoSensor, Spinner spinnerUbicacion) {
        if (sensorSeleccionado == null) {
            Toast.makeText(this, "No se ha seleccionado un sensor para modificar", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = nombreEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String idealStr = idealEditText.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        float ideal;
        try {
            ideal = Float.parseFloat(idealStr);
            if (ideal <= 0) {
                Toast.makeText(this, "El valor ideal debe ser un número positivo", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El valor ideal debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoSensor = spinnerTipoSensor.getSelectedItem().toString();
        String ubicacion = spinnerUbicacion.getSelectedItem().toString();

        // Actualizar el objeto sensor
        sensorSeleccionado.setNombre(nombre);
        sensorSeleccionado.setDescripcion(descripcion);
        sensorSeleccionado.setIdeal(ideal);
        sensorSeleccionado.setTipoSensor(tipoSensor);
        sensorSeleccionado.setUbicacion(ubicacion);

        // Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar el documento que tiene el campo "nombre" igual al nombre del sensor
        db.collection("sensores")
                .whereEqualTo("nombre", sensorSeleccionado.getNombre())  // Filtro por el campo "nombre"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Si encontramos documentos que coinciden
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String documentId = document.getId();  // Obtener el ID del documento
                            Log.d("ModificarSensor", "Documento encontrado con ID: " + documentId);

                            // Ahora modificamos el documento usando su ID
                            db.collection("sensores").document(documentId)
                                    .set(sensorSeleccionado.toMap())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ModificarSensor", "Sensor modificado correctamente");
                                        Toast.makeText(SensorActivity.this, "Sensor modificado correctamente", Toast.LENGTH_SHORT).show();
                                        sensorSeleccionado = null;
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ModificarSensor", "Error al modificar el sensor", e);
                                        Toast.makeText(SensorActivity.this, "Error al modificar el sensor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.d("ModificarSensor", "No se encontró el documento con el nombre: " + sensorSeleccionado.getNombre());
                        Toast.makeText(SensorActivity.this, "El sensor no existe en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ModificarSensor", "Error al realizar la consulta", e);
                    Toast.makeText(SensorActivity.this, "Error al verificar el sensor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarSensor() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar el documento que tiene el campo "nombre" igual a sensorSeleccionado.getNombre()
        db.collection("sensores")
                .whereEqualTo("nombre", sensorSeleccionado.getNombre())  // Filtro por el campo "nombre"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Si encontramos documentos que coinciden
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String documentId = document.getId();  // Obtener el ID del documento
                            Log.d("EliminarSensor", "Documento encontrado con ID: " + documentId);

                            // Ahora eliminamos el documento usando su ID
                            db.collection("sensores").document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("EliminarSensor", "Sensor eliminado correctamente");
                                        Toast.makeText(SensorActivity.this, "Sensor eliminado correctamente", Toast.LENGTH_SHORT).show();
                                        sensorSeleccionado = null;
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EliminarSensor", "Error al eliminar el sensor", e);
                                        Toast.makeText(SensorActivity.this, "Error al eliminar el sensor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.d("EliminarSensor", "No se encontró el documento con el nombre: " + sensorSeleccionado.getNombre());
                        Toast.makeText(SensorActivity.this, "El sensor no existe en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EliminarSensor", "Error al realizar la consulta", e);
                    Toast.makeText(SensorActivity.this, "Error al verificar el sensor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
