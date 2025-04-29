package com.example.gestorxpress;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Tarea.Tarea;

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
        tareas.setValue(listaTareas);
    }
}
