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

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class SlideshowFragment extends Fragment
{
    // Instancia a la clase DatabaseHelper
    private DatabaseHelper bd;

    private TextView tvTareasRealizadas, tvPorcentaje;

    // Instancia a la clase Grafica (lo utilizamos para dibujar la grafica)
    private Grafica grafica;
    // Instancia a la clase GraficaCircular (Lo utiliamos para dibujar la grafica circular)
    private GraficaCircular graficaCircular;

    private LinearLayout legendContainer;

    /**
     * Se llama para inflar el diseño del fragmento y configurar los datos visuales.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Vista del fragmento.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        // Instancia al layout XML
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        // Inicializa base de datos
        bd = new DatabaseHelper(requireContext());

        // Vincula elementos xml del layout
        tvTareasRealizadas = root.findViewById(R.id.tv_tareas_realizadas);
        tvPorcentaje = root.findViewById(R.id.tv_porcentaje);
        grafica = root.findViewById(R.id.barChart);
        graficaCircular = root.findViewById(R.id.grafica_circular);
        legendContainer = root.findViewById(R.id.legend_container);

        int idUsuario = bd.obtenerIdUsuario(); // Metodo donde obtenemos el ID del usuario actual

        if (idUsuario == -1)
        {
            // Usuario no logueado o error (No muestra nada)
            tvTareasRealizadas.setText("0");
            tvPorcentaje.setText("0%");
            grafica.setDatos(new LinkedHashMap<>());
            graficaCircular.setVisibility(View.GONE);
            grafica.setVisibility(View.VISIBLE);
        }
        else
        {
            // Llamamos el metodo donde comprobamos si el usuario que esta logeado en este momento
            // es el padre (administrador)
            boolean esPadre = bd.esUsuarioPadrePorId(idUsuario);

            // Si es el padre (administrador) mostramos dichos datos que solo puede ver el padre
            if (esPadre)
            {
                cargarDatosParaPadre();
            }
            // Si es el hijo, muestra datos que solo lo puede ver el hijo (otro usuario)
            else
            {
                cargarDatosParaHijo(idUsuario);
            }
        }

        return root;
    }

    /**
     * Carga datos de tareas completadas por un usuario hijo.
     * Muestra gráfica de barras con tareas completadas por día.
     * @param idUsuario ID del usuario hijo.
     */
    private void cargarDatosParaHijo(int idUsuario)
    {
        SQLiteDatabase consulta = bd.getReadableDatabase(); // Modo lectura

        // Contar tareas completadas por día
        Cursor cursorCompletadas = consulta.rawQuery(
                "SELECT DATE(fechaTareaFinalizada) AS fecha, COUNT(*) AS total " +
                        "FROM Tarea " +
                        "WHERE usuario_id = ? AND estado = 'Completada' " +
                        "GROUP BY DATE(fechaTareaFinalizada) " +
                        "ORDER BY fecha",
                new String[]{String.valueOf(idUsuario)}
        );

        // Recorre los resultados y guarda datos
        Map<String, Integer> datos = new LinkedHashMap<>();
        int totalCompletadas = 0;

        // Recorre los resultados y agrega a la colección
        while (cursorCompletadas.moveToNext())
        {
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
        if (cursorTotal.moveToFirst())
        {
            totalTareas = cursorTotal.getInt(0);
        }
        cursorTotal.close();

        // Actualiza texto de tareas completadas
        tvTareasRealizadas.setText(String.valueOf(totalCompletadas));

        // Calcular porcentaje (evitando división por cero)
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        tvPorcentaje.setText(porcentaje + "%");

        // Muestra gráfica de barras con datos
        grafica.setDatos(datos);
        grafica.setVisibility(View.VISIBLE);
        graficaCircular.setVisibility(View.GONE);
    }

    /**
     * Carga datos para un usuario padre.
     * Muestra gráfica circular con tareas completadas por cada hijo.
     */
    private void cargarDatosParaPadre()
    {
        SQLiteDatabase consulta = bd.getReadableDatabase();

        // Consulta las tareas completadas por cada hijo
        Cursor cursor = consulta.rawQuery(
                "SELECT Usuario.nombre, COUNT(Tarea.id) as total " +
                        "FROM Tarea " +
                        "JOIN Usuario ON Tarea.usuario_id = Usuario.id " +
                        "WHERE Usuario.esPadre = 0 AND Tarea.estado = 'Completada' " +
                        "GROUP BY Usuario.id, Usuario.nombre", null
        );

        Map<String, Integer> datosPorHijo = new LinkedHashMap<>();
        int totalCompletadas = 0;

        // Recorre los hijos y cuenta las tareas que tiene
        while (cursor.moveToNext())
        {
            String nombreHijo = cursor.getString(0);
            int tareasCompletadas = cursor.getInt(1);
            datosPorHijo.put(nombreHijo, tareasCompletadas);
            totalCompletadas += tareasCompletadas;
        }
        cursor.close();

        // Consulta donde obtenemos el total de tareas de todos los hijos
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

        // Muestra datos en los TextViews
        tvTareasRealizadas.setText(String.valueOf(totalCompletadas));
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        tvPorcentaje.setText(porcentaje + "%");

        // Muestra gráfica circular con los datos por hijo
        graficaCircular.setDatos(datosPorHijo);
        graficaCircular.setVisibility(View.VISIBLE);
        grafica.setVisibility(View.GONE);

        // Borra leyenda anterior (si existía)
        legendContainer.removeAllViews();

        // Obtiene etiquetas (nombres de hijos) y colores usados en la gráfica circular
        List<String> etiquetas = graficaCircular.getEtiquetas();
        List<Integer> colores = graficaCircular.getColores();

        // Por cada etiqueta (nombre de usuarios) crea un item en la leyenda
        for (int i = 0; i < etiquetas.size(); i++)
        {
            String nombre = etiquetas.get(i);
            int color = colores.get(i);

            // Contenedor horizontal por elemento
            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(16, 8, 16, 8);
            item.setGravity(Gravity.CENTER_VERTICAL);

            // Vista cuadrada para mostrar el color
            View colorBox = new View(requireContext());
            int size = (int) (20 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            colorBox.setLayoutParams(params);
            colorBox.setBackgroundColor(color);

            // Texto con el nombre del hijo (usuario)
            TextView label = new TextView(requireContext());
            label.setText(nombre);
            label.setTextSize(16f);
            label.setPadding(8, 0, 0, 0);

            // Agrega a la fila y luego al contenedor principal
            item.addView(colorBox);
            item.addView(label);

            legendContainer.addView(item);
        }
    }
}

