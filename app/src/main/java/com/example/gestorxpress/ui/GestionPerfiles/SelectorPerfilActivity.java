package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectorPerfilActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private PerfilAdapterSinClase perfilAdapter;

    /**
     * Método llamado al crear la actividad.
     * Infla la vista, configura el RecyclerView, carga los perfiles desde la base de datos
     * y configura la acción de clic para seleccionar perfil o añadir uno nuevo.
     *
     * @param savedInstanceState Bundle con estado previo de la actividad.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_perfil);

        // Obtiene referencia al RecyclerView y establece layout horizontal
        recyclerView = findViewById(R.id.recyclerViewPerfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Instancia el helper para la base de datos
        dbHelper = new DatabaseHelper(this);
        List<HashMap<String, Object>> listaPerfiles = new ArrayList<>();

        // Consulta la base de datos para obtener id, nombre y foto de todos los usuarios
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);

        // Recorre el cursor y convierte los datos en objetos para el adaptador
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    byte[] imagenBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("fotoPerfil"));
                    Bitmap imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombre);
                    map.put("foto", imagen);
                    listaPerfiles.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();

        // Crea el adaptador con la lista de perfiles y define la acción al seleccionar uno
        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil -> {
            int id = (int) perfil.get("id");
            Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);
            intent.putExtra("usuarioId", id);
            startActivity(intent);
        });

        // Asocia el adaptador al RecyclerView para mostrar los perfiles
        recyclerView.setAdapter(perfilAdapter);


        // Configura el clic en el ícono “añadir perfil” para abrir la actividad de registro
        findViewById(R.id.iconoAgregar).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }
}
