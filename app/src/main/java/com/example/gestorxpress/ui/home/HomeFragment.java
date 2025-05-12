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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_home, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.action_filtrar) {
//            // Mostrar el menú justo debajo del icono
//            View view = requireActivity().findViewById(R.id.action_filtrar);
//            if (view == null) {
//                // fallback a la toolbar entera
//                view = requireActivity().findViewById(R.id.toolbar);
//            }
//            mostrarMenuFiltro(view);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }





    private void cargarTareasDelUsuarioLogueado() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            int idUsuario = dbHelper.obtenerIdUsuario();
            List<Map<String, String>> listaTareas = new ArrayList<>();

            if (idUsuario != -1) {
                StringBuilder query = new StringBuilder("SELECT id, titulo, descripcion, prioridad, estado, fechaHoraInicio, fechaLimite FROM Tarea WHERE usuario_id = ?");
                List<String> args = new ArrayList<>();
                args.add(String.valueOf(idUsuario));

                if (filtroPrioridad != null) {
                    query.append(" AND prioridad = ?");
                    args.add(filtroPrioridad);
                }
                if (filtroEstado != null) {
                    query.append(" AND estado = ?");
                    args.add(filtroEstado);
                }

                Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

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
