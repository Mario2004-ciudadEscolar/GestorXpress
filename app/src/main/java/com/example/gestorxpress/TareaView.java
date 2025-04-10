package com.example.gestorxpress;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.gestorxpress.Tarea;
import java.util.ArrayList;
import java.util.List;

public class TareaView extends ViewModel {
    private final MutableLiveData<List<Tarea>> listaTareas = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Tarea>> getTareas() {
        return listaTareas;
    }

    public void agregarTarea(Tarea tarea) {
        List<Tarea> tareasActuales = new ArrayList<>(listaTareas.getValue());
        tareasActuales.add(tarea);
        listaTareas.setValue(tareasActuales);
    }
}
