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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class SlideshowFragment extends Fragment
{
    // Instancia a la clase DatabaseHelper
    private DatabaseHelper bd;

    private TextView txtTareasRealizadas, txtPorcentaje;

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
        txtTareasRealizadas = root.findViewById(R.id.txtTareasRealizadas);
        txtPorcentaje = root.findViewById(R.id.txtPorcentaje);
        grafica = root.findViewById(R.id.barChart);
        graficaCircular = root.findViewById(R.id.grafica_circular);
        legendContainer = root.findViewById(R.id.legend_container);

        int idUsuario = bd.obtenerIdUsuario(); // Metodo donde obtenemos el ID del usuario actual

        if (idUsuario == -1)
        {
            // Usuario no logueado o error (No muestra nada)
            txtTareasRealizadas.setText("0");
            txtPorcentaje.setText("0%");
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
     * Este método se llama cuando el fragmento pasa al estado **resumed**,
     * osea, cuando el fragmento se vuelve visible y está listo para interactuar con el usuario.
     *.
     * En este caso, slo usamos para actualizar la interfaz mostrando datos recientes
     * cada vez que el usuario vuelve a ver el fragmento.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        //Obtenemos el id del usuario que esta loggeado en este momento
        int idUsuario = bd.obtenerIdUsuario();

        // Si no obtenemos el ID del usuario, no mostramos nada
        if (idUsuario == -1)
        {
            txtTareasRealizadas.setText("0");
            txtPorcentaje.setText("0%");
            grafica.setDatos(new LinkedHashMap<>());
            graficaCircular.setVisibility(View.GONE);
            grafica.setVisibility(View.VISIBLE);
        }
        else
        {
            // Si obtnemos un ID, comprobamos que si ese ID obtenido
            // es el padre, osea que el usuario es el padre (administrador)
            boolean esPadre = bd.esUsuarioPadrePorId(idUsuario);

            // Si es el padre, mostramos los datos que solo puede ver el padre
            if (esPadre)
            {
                cargarDatosParaPadre();
            }
            else
            {
                // Si no mostramos los datos que solo lo puede ver el hijo
                cargarDatosParaHijo(idUsuario);
            }
        }
    }


    /**
     * Carga datos de tareas completadas por un usuario hijo.
     * Muestra gráfica de barras con tareas completadas por día.
     * @param idUsuario ID del usuario hijo.
     */
    private void cargarDatosParaHijo(int idUsuario)
    {
        SQLiteDatabase db = bd.getReadableDatabase(); // Modo lectura

        // Obtenemos el dia de la semana actual (Osea el lunes y domingo)
        // Para luego solo obtener las tareas completadas de esa semana
        String lunes = obtenerLunesSemanaActual();
        String domingo = obtenerDomingoSemanaActual();

        // Contar tareas completadas por día
        Cursor consultaCompletadas = db.rawQuery(
                "SELECT DATE(fechaTareaFinalizada) AS fecha, COUNT(*) AS total " +
                        "FROM Tarea " +
                        "WHERE usuario_id = ? AND estado = 'Completada' " +
                        "AND DATE(fechaTareaFinalizada) BETWEEN ? AND ? " +
                        "GROUP BY DATE(fechaTareaFinalizada) " +
                        "ORDER BY fecha",
                new String[]{String.valueOf(idUsuario), lunes, domingo}
        );

        // Recorre los resultados y guarda datos
        Map<String, Integer> datos = new LinkedHashMap<>();
        int totalCompletadas = 0;

        // Recorre los resultados y agrega a la colección
        while (consultaCompletadas.moveToNext())
        {
            String fecha = consultaCompletadas.getString(0);
            int cantidad = consultaCompletadas.getInt(1);
            datos.put(fecha, cantidad);
            totalCompletadas += cantidad;
        }
        consultaCompletadas.close();

        // Contar todas las tareas del usuario (sin importar estado)
        Cursor cursorTotal = db.rawQuery(
                "SELECT COUNT(*) FROM Tarea " +
                        "WHERE usuario_id = ? AND estado != 'Completada'",
                new String[]{String.valueOf(idUsuario)}
        );


        int totalNoCompletadas = 0;
        if (cursorTotal.moveToFirst())
        {
            totalNoCompletadas = cursorTotal.getInt(0);
        }
        cursorTotal.close();

        // 3. Total = completadas + no completadas
        int totalTareas = totalCompletadas + totalNoCompletadas;

        // Actualiza texto de tareas completadas
        txtTareasRealizadas.setText(String.valueOf(totalCompletadas));

        // Calcular porcentaje (evitando división por cero)
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        txtPorcentaje.setText(porcentaje + "%");

        // Muestra gráfica de barras con datos
        grafica.setDatosSemanaActual(datos);
        grafica.setVisibility(View.VISIBLE);
        graficaCircular.setVisibility(View.GONE);
    }

    /**
     * Carga datos para un usuario padre.
     * Muestra gráfica circular con tareas completadas por cada hijo.
     */
    private void cargarDatosParaPadre()
    {
        SQLiteDatabase db = bd.getReadableDatabase();

        // Obtenemos el dia de la semana actual (Osea el lunes y domingo)
        // Para luego solo obtener las tareas completadas de esa semana
        String lunes = obtenerLunesSemanaActual();
        String domingo = obtenerDomingoSemanaActual();

        // Consulta las tareas completadas por cada hijo
        Cursor consulta = db.rawQuery(
                "SELECT Usuario.nombre, COUNT(Tarea.id) as total " +
                        "FROM Tarea " +
                        "JOIN Usuario ON Tarea.usuario_id = Usuario.id " +
                        "WHERE Usuario.esPadre = 0 AND Tarea.estado = 'Completada' " +
                        "AND DATE(fechaTareaFinalizada) BETWEEN ? AND ? " +
                        "GROUP BY Usuario.id, Usuario.nombre",
                new String[]{lunes, domingo}
        );

        Map<String, Integer> datosPorHijo = new LinkedHashMap<>();
        int totalCompletadas = 0;

        // Recorre los hijos y cuenta las tareas que tiene
        while (consulta.moveToNext())
        {
            String nombreHijo = consulta.getString(0);
            int tareasCompletadas = consulta.getInt(1);
            datosPorHijo.put(nombreHijo, tareasCompletadas);
            totalCompletadas += tareasCompletadas;
        }
        consulta.close();

        // Consulta donde obtenemos el total de tareas de todos los hijos
        Cursor consultaTotal = db.rawQuery(
                "SELECT COUNT(*) FROM Tarea " +
                        "WHERE usuario_id IN (SELECT id FROM Usuario WHERE esPadre = 0) " +
                        "AND estado != 'Completada'",
                null
        );


        int totalNoCompletadas = 0;
        if (consultaTotal.moveToFirst())
        {
            totalNoCompletadas = consultaTotal.getInt(0);
        }
        consultaTotal.close();

        //Total = completadas + no completadas
        int totalTareas = totalCompletadas + totalNoCompletadas;

        // Muestra datos en los TextViews
        txtTareasRealizadas.setText(String.valueOf(totalCompletadas));
        int porcentaje = totalTareas > 0 ? (int) ((totalCompletadas * 100) / totalTareas) : 0;
        txtPorcentaje.setText(porcentaje + "%");

        // Muestra gráfica circular con los datos por hijo
        graficaCircular.setDatos(datosPorHijo);


        // Desactiva leyenda (nombre de los hijos) dibujada en la gráfica para evitar duplicados
        graficaCircular.setMostrarLeyenda(false);

        graficaCircular.setVisibility(View.VISIBLE);
        grafica.setVisibility(View.GONE);

        // Borra leyenda anterior (si existía)
        legendContainer.removeAllViews();

        // Obtiene etiquetas (nombres de hijos) y colores usados en la gráfica circular
        List<String> etiquetas = graficaCircular.getEtiquetas();
        List<Integer> colores = graficaCircular.getColores();

        // Construye leyenda manualmente para mostrar colores y nombres de los hijos
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

    /**
     * Este método nos sirve para obtener la fecha del lunes
     * de la semana actual en formato "yyyy-MM-dd".
     */
    private String obtenerLunesSemanaActual()
    {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY); // Establece lunes como primer día de la semana
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Se posiciona en el lunes actual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    /**
     * Este método nos sirve para obtener la fecha del domingo
     * de la semana actual en formato "yyyy-MM-dd".
     */
    private String obtenerDomingoSemanaActual()
    {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY); // Establece lunes como primer día de la semana
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Se posiciona en el domingo actual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }


}

