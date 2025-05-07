package com.example.gestorxpress.ui.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {


        //private TareaView tareaView;
        private TextView textHome;
        private ListView listView; // Este será el ListView que tenemos en el XML
        private int idUsuario; // ID del usuario logueado

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_home, container, false);

            textHome = root.findViewById(R.id.text_home);
            listView = root.findViewById(R.id.list_view); // Aquí te referirás al ListView en el XML

            cargarTareasDelUsuarioLogueado();

            return root;

        }

    /**
     * Con este metodo lo que estamos haciendo es cargar y mostrar las tareas del usuario
     * que esta loggeado en este momento, ya que antes de cargar las tareas comprobamos
     * quien esta loggeado ahora en la aplicación.
     *
     * Una vez que tengasmos el id del que esta logeado, vamos a obtener las tareas que tiene
     * y mostrarlo en el HOME.
     */
    private void cargarTareasDelUsuarioLogueado()
    {
        new Thread(() -> {

            // Creamos la isntacia a la clase DatabaseHelper, que es donde esta toda la bbdd y los metodos a utilizar
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

            // Obtenemos la bbdd en modo lectura, osea que estamos leyendo de nuestra bbdd.
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Aquí es donde obtenemos el id del usuario
            int idUsuario = dbHelper.obtenerIdUsuario();

            // Lista de Tareas para guardar y mostrar todas las tareas
            List<String> tareas = new ArrayList<>();

            // Comprobamos si el id obtenido no es -1 <-- Que no exista
            if (idUsuario != -1)
            {
                // Aquí realizamos la sentencia para obtener los datos de tareas de dicho usuario logeado
                Cursor cursor = db.rawQuery("SELECT titulo, estado, fechaLimite FROM Tarea WHERE usuario_id = ?", new String[]{String.valueOf(idUsuario)});
               // Aqui movemos el cursor a la primera fila, para leer el primero de la bbdd (De tareas)
                if (cursor.moveToFirst())
                {
                    // Obtenemos los datos de las tareas y lo guardamos en una colección
                    do
                    {
                        String titulo = cursor.getString(0);
                        String estado = cursor.getString(1);
                        String fechaLimite = cursor.getString(2);

                        tareas.add(titulo + " - " + estado + " (vence: " + fechaLimite + ")");
                    } while (cursor.moveToNext());
                }
                cursor.close(); // Cerramos la sentencia como asi decirlo.
            }

            db.close(); // Cerramos la bbdd

            requireActivity().runOnUiThread(() -> {
                if (idUsuario == -1 || tareas.isEmpty())
                {
                    textHome.setText("No hay tareas creadas.");
                    listView.setVisibility(View.GONE);
                }
                else
                {
                    textHome.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tareas);
                    listView.setAdapter(adapter);
                }
            });
        }).start();
    }
}