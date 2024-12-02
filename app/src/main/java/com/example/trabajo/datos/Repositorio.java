package com.example.trabajo.datos;

import com.example.trabajo.model.TipoSensor;
import com.example.trabajo.model.Ubicacion;
import com.example.trabajo.model.Sensor;

import java.util.ArrayList;
import java.util.List;

public class Repositorio {

    private static Repositorio instance = null;
    private List<TipoSensor> listaTipoSensor;
    private List<Ubicacion> listaUbicaciones;
    private List<Sensor> listaSensores; // Lista de sensores

    protected Repositorio() {
        listaTipoSensor = new ArrayList<>();
        listaUbicaciones = new ArrayList<>();
        listaSensores = new ArrayList<>(); // Inicialización de la lista de sensores

        // Agregar tipos de sensor
        listaTipoSensor.add(new TipoSensor("Temperatura"));
        listaTipoSensor.add(new TipoSensor("Humedad"));

        // Ubicaciones de muestra
        listaUbicaciones.add(new Ubicacion("Invernadero", "Invernadero aislado"));
        listaUbicaciones.add(new Ubicacion("San José de la Mariquina", "Mediano de 100 personas"));
        listaUbicaciones.add(new Ubicacion("Norte", "Grande para 200 personas"));
    }

    // Singleton
    public static synchronized Repositorio getInstance() {
        if (instance == null) {
            instance = new Repositorio();
        }
        return instance;
    }

    // Obtener tipos de sensor
    public List<String> obtenerTiposSensor() {
        List<String> tipos = new ArrayList<>();
        for (TipoSensor tipo : listaTipoSensor) {
            tipos.add(tipo.getNombre());
        }
        return tipos;
    }

    // Obtener ubicaciones para el spinner
    public List<String> obtenerUbicaciones() {
        List<String> ubicaciones = new ArrayList<>();
        for (Ubicacion ubicacion : listaUbicaciones) {
            ubicaciones.add(ubicacion.getNombre());
        }
        return ubicaciones;
    }

    // Agregar una ubicación
    public void agregarUbicacion(String nombre, String descripcion) {
        listaUbicaciones.add(new Ubicacion(nombre, descripcion));
    }

    // Agregar un sensor
    public void agregarSensor(Sensor sensor) {
        listaSensores.add(sensor);
    }

    // Obtener lista de sensores para el adaptador
    public List<Sensor> obtenerSensores() {
        return new ArrayList<>(listaSensores);
    }

    // Buscar un sensor por nombre
    public Sensor buscarSensor(String nombre) {
        for (Sensor sensor : listaSensores) {
            if (sensor.getNombre().equalsIgnoreCase(nombre)) {
                return sensor; // Sensor encontrado
            }
        }
        return null; // Si no se encuentra el sensor
    }

    // Modificar un sensor
    public boolean modificarSensor(Sensor sensor) {
        for (int i = 0; i < listaSensores.size(); i++) {
            if (listaSensores.get(i).getNombre().equalsIgnoreCase(sensor.getNombre())) {
                listaSensores.set(i, sensor); // Reemplazar el sensor
                return true; // Modificación exitosa
            }
        }
        return false; // Si no se encuentra el sensor
    }

    // Eliminar un sensor por nombre
    public boolean eliminarSensor(String nombre) {
        for (int i = 0; i < listaSensores.size(); i++) {
            if (listaSensores.get(i).getNombre().equalsIgnoreCase(nombre)) {
                listaSensores.remove(i); // Eliminar el sensor de la lista
                return true; // Eliminación exitosa
            }
        }
        return false; // Si no se encuentra el sensor
    }
}
