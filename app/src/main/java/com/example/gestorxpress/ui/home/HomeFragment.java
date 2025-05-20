package com.example.gestorxpress.ui.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.*;
import android.widget.PopupMenu;
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

    private String filtroPrioridad = null;
    private String filtroEstado = null;

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext()); // Crea una sola instancia
        setHasOptionsMenu(true); // Permite mostrar iconos en el toolbar
    }

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

    /**
     * Con este metodo obtenemos las tareas del usuario que esta logeado en este momento
     * .
     * 1. Obtenemos el id del usuario logeado en nuestra aplicación en este momento.
     * 2. Comprobamos si el id obtenido es el padre (administrador).
     * 3. Comprobamos si obtenimos el id del usuario.
     * 4. Luego de haber obtenido la validación de que si es padre o no, se realizara dos
     *    sentencias según si es padre (administrador) o hijo (otro usuario).
     * 5. Dentro de esas sentencias obtenemos las tareas, el padre obtiene el suyo y el delos hijos,
     *    en cambio los hijos obtiene sus propias tareas.
     * 6. Los datos obtenido lo guardamos en una colección para mostrarlo finalmente en el HOME.
     */
    private void cargarTareasDelUsuarioLogueado()
    {
        new Thread(() -> {
            int idUsuario = dbHelper.obtenerIdUsuario();
            boolean esPadre = dbHelper.esUsuarioPadrePorId(idUsuario);
            List<Map<String, String>> listaTareas = new ArrayList<>();

            if (idUsuario != -1)
            {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor;

                if (esPadre)
                {
                    // Padre: ver tareas de todos los usuarios que no son padres
                    String query = "SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite " +
                            "FROM Tarea WHERE usuario_id IN (SELECT id FROM Usuario WHERE esPadre = 0)";
                    cursor = db.rawQuery(query, null);
                }
                else
                {
                    // Hijo: ver solo sus tareas
                    String query = "SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite FROM Tarea WHERE usuario_id = ?";
                    cursor = db.rawQuery(query, new String[]{String.valueOf(idUsuario)});
                }

                if (cursor.moveToFirst())
                {
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
                // No cerrar dbHelper ni db aquí
            }

            requireActivity().runOnUiThread(() -> {
                if (listaTareas.isEmpty()) {
                    textHome.setText("No hay tareas disponibles.");
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

    /**
     * Metodo que llamamos para cerrar la bbdd
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close(); // Cerrar dbHelper sólo aquí
            dbHelper = null;
        }
    }



    // Muestra el popup de filtro anclado al toolbar
    public void mostrarMenuFiltro(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor, Gravity.END | Gravity.BOTTOM);
        popup.getMenu().add("Prioridad: Alta");
        popup.getMenu().add("Prioridad: Media");
        popup.getMenu().add("Prioridad: Baja");
        popup.getMenu().add("Estado: Pendiente");
        popup.getMenu().add("Estado: Completada");
        popup.getMenu().add("Quitar filtros");

        popup.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();
            if (titulo.startsWith("Prioridad")) {
                filtroPrioridad = titulo.split(": ")[1];
            } else if (titulo.startsWith("Estado")) {
                filtroEstado = titulo.split(": ")[1];
            } else {
                filtroPrioridad = null;
                filtroEstado = null;
            }
            cargarTareasDelUsuarioLogueado();
            return true;
        });

        popup.show();
    }




}
