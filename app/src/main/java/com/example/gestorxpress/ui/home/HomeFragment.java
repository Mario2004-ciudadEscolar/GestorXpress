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

public class HomeFragment extends Fragment
{

    private TextView textHome;
    private RecyclerView recyclerView;

    private String filtroPrioridad = null;
    private String filtroEstado = null;

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext()); // Instancia a la bbdd
        setHasOptionsMenu(true); // Permite mostrar iconos en el toolbar
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
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
        new Thread(() ->
        {
            // Obtenemos el ID del usuario que esta logeado en este momento en nuestra aplicación
            int idUsuario = dbHelper.obtenerIdUsuario();

            // Comprobamos mediante el id si ese usuario es el padre
            boolean esPadre = dbHelper.esUsuarioPadrePorId(idUsuario);

            // Generamos una colección donde vamos a guardar las tareas y mostrarlas en el home
            List<Map<String, String>> listaTareas = new ArrayList<>();

            // Si el usuario es diferente que -1, osea que si existe ese id
            // Me realiza las siguientes funciones
            if (idUsuario != -1)
            {
                // Conexión a la bbdd
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
                Cursor consulta;

                // Construimos la consulta con filtros opcionales
                StringBuilder queryBuilder = new StringBuilder();
                List<String> argsList = new ArrayList<>();

                // Si es el padre (administrador) realizamos la siguiente consulta.
                if (esPadre)
                {

                    // Padre: visualiza las tareas de usuarios que no son padres
                    queryBuilder.append("SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite, usuario_id ")
                            .append("FROM Tarea WHERE usuario_id IN (SELECT id FROM Usuario WHERE esPadre = 0)");
                }
                // Si es el hijo, realiza la siguiente consulta.
                else
                {
                    // Hijo: visualiza solo sus tareas
                    queryBuilder.append("SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite, usuario_id ")
                            .append("FROM Tarea WHERE usuario_id = ?");
                    argsList.add(String.valueOf(idUsuario));
                }

                // Si NO hay filtro de estado, ocultamos las tareas completadas
                if (filtroEstado == null)
                {
                    queryBuilder.append(" AND estado <> 'Completada'");
                }
                else
                {
                    // Si hay filtroEstado, filtramos según lo seleccionado
                    queryBuilder.append(" AND estado = ?");
                    argsList.add(filtroEstado);
                }

                // Aplicar filtros si existen
                if (filtroPrioridad != null)
                {
                    queryBuilder.append(" AND prioridad = ?");
                    argsList.add(filtroPrioridad);
                }

                String[] args = argsList.toArray(new String[0]);
                consulta = db.rawQuery(queryBuilder.toString(), args);

                // Ponemos el cursor a principio
                if (consulta.moveToFirst())
                {
                    // Y cada vez que recorremos una tarea lo vamos guardando en una colección
                    do
                    {
                        Map<String, String> tarea = new HashMap<>();
                        tarea.put("id", consulta.getString(0));
                        tarea.put("titulo", consulta.getString(1));
                        tarea.put("descripcion", consulta.getString(2));
                        tarea.put("prioridad", consulta.getString(3));
                        tarea.put("estado", consulta.getString(4));
                        tarea.put("fechaHoraInicio", consulta.getString(5));
                        tarea.put("fechaLimite", consulta.getString(6));
                        tarea.put("usuario_id", consulta.getString(7));
                        listaTareas.add(tarea);
                    }
                    while (consulta.moveToNext());
                }

                consulta.close();

            }

            requireActivity().runOnUiThread(() ->
            {
                if (listaTareas.isEmpty())
                {
                    textHome.setText("No hay tareas disponibles.");
                    textHome.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                else
                {
                    textHome.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new TareaAdapter(requireContext(), listaTareas, dbHelper, esPadre));
                }
            });
        }).start();
    }

    /**
     * Metodo que llamamos para cerrar la bbdd
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (dbHelper != null)
        {
            dbHelper.close(); // Cerrar dbHelper sólo aquí
            dbHelper = null;
        }
    }



    // Muestra el popup de filtro anclado al toolbar

    /**
     * Muestra un menú emergente (popup) de filtros anclado a una vista (generalmente el botón del toolbar).
     *.
     * Este menú permite filtrar las tareas según su prioridad (Alta, Media, Baja)
     * o estado (Pendiente, Completada). También permite quitar los filtros.
     *.
     * Cuando el usuario selecciona una opción:
     *  - Se actualizan las variables `filtroPrioridad` y/o `filtroEstado`.
     *  - Se recarga la lista de tareas usando los filtros aplicados.
     *
     * @param anchor Vista a la que se ancla el menú emergente (normalmente un bóton del toolbar)
     */
    public void mostrarMenuFiltro(View anchor)
    {
        // Crea el popupMenu anclado a la vista 'anchor', alineado a la derecha e inferior
        PopupMenu popup = new PopupMenu(requireContext(), anchor, Gravity.END | Gravity.BOTTOM);

        // Agregamos opciones al menú de filto por prioridad y estado
        popup.getMenu().add("Prioridad: Alta");
        popup.getMenu().add("Prioridad: Media");
        popup.getMenu().add("Prioridad: Baja");
        popup.getMenu().add("Estado: Pendiente");
        popup.getMenu().add("Estado: Completada");
        popup.getMenu().add("Quitar filtros"); // Opción para quitar los filtros actuales

        // Se activa cuando el usuaio selecciona una opción del menú
        popup.setOnMenuItemClickListener(item ->
        {
            // Guardamos el texto del item seleccionado
            String titulo = item.getTitle().toString();

            // Si el título comienza con "Prioridad", extrae y guarda la prioridad seleccionada
            if (titulo.startsWith("Prioridad"))
            {
                filtroPrioridad = titulo.split(": ")[1]; // Ej: "Prioridad: Alta" --> "Alta"
            }
            // Si el títutlo comienza con "Estado", extrae y guarda el estado seleccionado
            else if (titulo.startsWith("Estado"))
            {
                filtroEstado = titulo.split(": ")[1]; // Ej: "Estado: Completada" --> "Completada"
            }
            // Si se elige "Quitar filtros", se elimina los filtros aplicados
            else
            {
                filtroPrioridad = null;
                filtroEstado = null;
            }

            // Recarga las tareas desde la base de datos, aplicando los filtros (si los hay)
            cargarTareasDelUsuarioLogueado();

            return true;
        });

        // Mostramos el menú en pantalla
        popup.show();
    }

}
