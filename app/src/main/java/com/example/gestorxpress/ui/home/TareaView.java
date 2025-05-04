package com.example.gestorxpress.ui.home;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Tarea.Tarea;
import com.example.gestorxpress.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
public class TareaView extends ViewModel {
    private MutableLiveData<List<Tarea>> tareas = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Tarea>> getTareas() {
        return tareas;
    }

    public void cargarTareasDesdeBD(Context context, int idUsuario) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Tarea> listaTareas = db.obtenerTareasPorUsuario(idUsuario);
        Log.d("Tareas", "Tareas cargadas: " + listaTareas.size()); // Agregado para depuración
        tareas.setValue(listaTareas);
    }

}
