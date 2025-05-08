package com.example.gestorxpress.ui.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView textHome;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        textHome = root.findViewById(R.id.text_home);
        recyclerView = root.findViewById(R.id.recycler_view_tareas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarTareasDelUsuarioLogueado();

        return root;
    }

    private void cargarTareasDelUsuarioLogueado() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            int idUsuario = dbHelper.obtenerIdUsuario();
            List<Map<String, String>> listaTareas = new ArrayList<>();

            if (idUsuario != -1) {
                Cursor cursor = db.rawQuery("SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite FROM Tarea WHERE usuario_id = ?", new String[]{String.valueOf(idUsuario)});


                if (cursor.moveToFirst()) {
                    do {
                        Map<String, String> tarea = new HashMap<>();
                        tarea.put("id", cursor.getString(0));
                        tarea.put("titulo", cursor.getString(1));
                        tarea.put("descripcion", cursor.getString(2));
                        tarea.put("prioridad", cursor.getString(3));
                        tarea.put("estado", cursor.getString(4));
                        tarea.put("fechaHoraInicio", cursor.getString(5));
                        tarea.put("fechaLimite", cursor.getString(6));

                        listaTareas.add(tarea);
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

            db.close();

            requireActivity().runOnUiThread(() -> {
                if (idUsuario == -1 || listaTareas.isEmpty()) {
                    textHome.setText("No hay tareas creadas.");
                    textHome.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textHome.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new TareaAdapter(requireContext(), listaTareas, dbHelper));
                }
            });
        }).start();
    }
}
