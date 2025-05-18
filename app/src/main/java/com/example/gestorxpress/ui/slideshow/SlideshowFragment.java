package com.example.gestorxpress.ui.slideshow;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.databinding.FragmentSlideshowBinding;

import java.util.LinkedHashMap;
import java.util.Map;

public class SlideshowFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_slideshow, container, false);

        TextView tvTareasRealizadas = view.findViewById(R.id.tv_tareas_realizadas);
        TextView tvPorcentaje = view.findViewById(R.id.tv_porcentaje);
        Grafica grafica = view.findViewById(R.id.barChart);

        // Inicializamos el helper para la base de datos
        dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int usuarioId = dbHelper.obtenerIdUsuario();

        // === Parte 1: Datos para resumen ===
        /**
         * Ejecuta una consulta SQL para obtener el número de tareas completadas
         * y el promedio de tareas completadas del usuario con usuarioId.
         * Luego actualiza los TextView con esos datos.
         */
        Cursor resumenCursor = db.rawQuery(
                "SELECT tareasCompletadas, promedioCompletadas FROM Actividad_usuario WHERE usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        if (resumenCursor.moveToFirst())
        {
            int tareasCompletadas = resumenCursor.getInt(resumenCursor.getColumnIndexOrThrow("tareasCompletadas"));
            int promedioCompletadas = resumenCursor.getInt(resumenCursor.getColumnIndexOrThrow("promedioCompletadas"));

            // Muestra las tareas completadas y el porcentaje en la UI
            tvTareasRealizadas.setText(String.valueOf(tareasCompletadas));
            tvPorcentaje.setText(promedioCompletadas + "%");
        }
        else
        {
            // Si no hay datos, muestra 0
            tvTareasRealizadas.setText("0");
            tvPorcentaje.setText("0%");
        }
        resumenCursor.close();

        // === Parte 2: Datos para la gráfica (tareas completadas por fecha) ===
        /**
         * Ejecuta una consulta SQL para contar cuántas tareas completadas tiene el usuario
         * agrupadas por fecha de creación. Guarda estos datos en un Map para luego graficarlos.
         */
        Cursor graficaCursor = db.rawQuery(
                "SELECT fechaCreacion, COUNT(*) as total " +
                        "FROM Tarea " +
                        "WHERE usuario_id = ? AND estado = 'completada' " +
                        "GROUP BY fechaCreacion " +
                        "ORDER BY fechaCreacion ASC",
                new String[]{String.valueOf(usuarioId)}
        );

        Map<String, Integer> datosGrafica = new LinkedHashMap<>();
        while (graficaCursor.moveToNext()) {
            String fecha = graficaCursor.getString(graficaCursor.getColumnIndexOrThrow("fechaCreacion"));
            int total = graficaCursor.getInt(graficaCursor.getColumnIndexOrThrow("total"));
            datosGrafica.put(fecha, total);
        }

        graficaCursor.close();
        db.close();

        // === Cargar los datos en la gráfica personalizada ===
        grafica.setDatos(datosGrafica);

        return view;
    }

   /* @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }*/
}
