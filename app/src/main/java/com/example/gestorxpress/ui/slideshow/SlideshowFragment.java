package com.example.gestorxpress.ui.slideshow;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.slideshow.Graficas.Grafica;
import com.example.gestorxpress.ui.slideshow.Graficas.GraficaCircular;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SlideshowFragment extends Fragment {

    private DatabaseHelper bd;
    private TextView tvTareasRealizadas, tvPorcentaje;
    private Grafica grafica;
    private GraficaCircular graficaCircular;
    private LinearLayout legendContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        bd = new DatabaseHelper(requireContext());

        tvTareasRealizadas = root.findViewById(R.id.tv_tareas_realizadas);
        tvPorcentaje = root.findViewById(R.id.tv_porcentaje);
        grafica = root.findViewById(R.id.barChart);
        graficaCircular = root.findViewById(R.id.grafica_circular);
        legendContainer = root.findViewById(R.id.legend_container);

        int idUsuario = bd.obtenerIdUsuario();

        if (idUsuario == -1) {
            // Usuario no logueado o error
            tvTareasRealizadas.setText("0");
            tvPorcentaje.setText("0%");
            grafica.setDatos(new LinkedHashMap<>());
            graficaCircular.setVisibility(View.GONE);
            grafica.setVisibility(View.VISIBLE);
        } else {
            boolean esPadre = bd.esUsuarioPadrePorId(idUsuario);
            if (esPadre) {
                cargarDatosParaPadre();
            } else {
                cargarDatosParaHijo(idUsuario);
            }
        }

        return root;
    }

    private void cargarDatosParaHijo(int idUsuario) {
        SQLiteDatabase consulta = bd.getReadableDatabase();

        // Contar tareas completadas por día
        Cursor cursorCompletadas = consulta.rawQuery(
                "SELECT DATE(fechaTareaFinalizada) AS fecha, COUNT(*) AS total " +
                        "FROM Tarea " +
                        "WHERE usuario_id = ? AND estado = 'Completada' " +
                        "GROUP BY DATE(fechaTareaFinalizada) " +
                        "ORDER BY fecha",
                new String[]{String.valueOf(idUsuario)}
        );

        Map<String, Integer> datos = new LinkedHashMap<>();
        int totalCompletadas = 0;

        while (cursorCompletadas.moveToNext()) {
            String fecha = cursorCompletadas.getString(0);
            int cantidad = cursorCompletadas.getInt(1);
            datos.put(fecha, cantidad);
            totalCompletadas += cantidad;
        }
        cursorCompletadas.close();

        // Contar todas las tareas del usuario (sin importar estado)
        Cursor cursorTotal = consulta.rawQuery(
                "SELECT COUNT(*) FROM Tarea WHERE usuario_id = ?",
                new String[]{String.valueOf(idUsuario)}
        );

        int totalTareas = 0;
        if (cursorTotal.moveToFirst()) {
            totalTareas = cursorTotal.getInt(0);
        }
        cursorTotal.close();

        tvTareasRealizadas.setText(String.valueOf(totalCompletadas));

        // Calcular porcentaje (evitando división por cero)
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        tvPorcentaje.setText(porcentaje + "%");

        grafica.setDatos(datos);
        grafica.setVisibility(View.VISIBLE);
        graficaCircular.setVisibility(View.GONE);
    }

    private void cargarDatosParaPadre()
    {
        SQLiteDatabase consulta = bd.getReadableDatabase();

        Cursor cursor = consulta.rawQuery(
                "SELECT Usuario.nombre, COUNT(Tarea.id) as total " +
                        "FROM Tarea " +
                        "JOIN Usuario ON Tarea.usuario_id = Usuario.id " +
                        "WHERE Usuario.esPadre = 0 AND Tarea.estado = 'Completada' " +
                        "GROUP BY Usuario.id, Usuario.nombre", null
        );

        Map<String, Integer> datosPorHijo = new LinkedHashMap<>();
        int totalCompletadas = 0;

        while (cursor.moveToNext())
        {
            String nombreHijo = cursor.getString(0);
            int tareasCompletadas = cursor.getInt(1);
            datosPorHijo.put(nombreHijo, tareasCompletadas);
            totalCompletadas += tareasCompletadas;
        }
        cursor.close();

        Cursor cursorTotal = consulta.rawQuery(
                "SELECT COUNT(*) FROM Tarea WHERE usuario_id IN (SELECT id FROM Usuario WHERE esPadre = 0)",
                null
        );

        int totalTareas = 0;
        if (cursorTotal.moveToFirst())
        {
            totalTareas = cursorTotal.getInt(0);
        }
        cursorTotal.close();

        tvTareasRealizadas.setText(String.valueOf(totalCompletadas));
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        tvPorcentaje.setText(porcentaje + "%");

        graficaCircular.setDatos(datosPorHijo);
        graficaCircular.setVisibility(View.VISIBLE);
        grafica.setVisibility(View.GONE);

        legendContainer.removeAllViews(); // Limpiar leyenda anterior

        List<String> etiquetas = graficaCircular.getEtiquetas();
        List<Integer> colores = graficaCircular.getColores();

        for (int i = 0; i < etiquetas.size(); i++)
        {
            String nombre = etiquetas.get(i);
            int color = colores.get(i);

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(16, 8, 16, 8);
            item.setGravity(Gravity.CENTER_VERTICAL);

            View colorBox = new View(requireContext());
            int size = (int) (20 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            colorBox.setLayoutParams(params);
            colorBox.setBackgroundColor(color);

            TextView label = new TextView(requireContext());
            label.setText(nombre);
            label.setTextSize(16f);
            label.setPadding(8, 0, 0, 0);

            item.addView(colorBox);
            item.addView(label);

            legendContainer.addView(item);
        }
    }
}

