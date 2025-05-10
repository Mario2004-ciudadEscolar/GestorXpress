package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_perfil);

        recyclerView = findViewById(R.id.recyclerViewPerfiles);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        dbHelper = new DatabaseHelper(this);

        List<HashMap<String, Object>> listaPerfiles = new ArrayList<>();

   //     Cursor cursor = dbHelper.getReadableDatabase()
 //               .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);
// V1
//        while (cursor.moveToNext()) {
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("id", cursor.getInt(0));
//            map.put("nombre", cursor.getString(1));
//            byte[] imagenBytes = cursor.getBlob(2);
//            Bitmap imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
//            map.put("foto", imagen);
//            listaPerfiles.add(map);
//        }
//        cursor.close();
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);

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
                    e.printStackTrace(); // Para depurar si hay alguna inconsistencia
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Agrega el botón de “+”
        HashMap<String, Object> agregarNuevo = new HashMap<>();
        agregarNuevo.put("id", -1);
        agregarNuevo.put("nombre", "Añadir perfil");
        agregarNuevo.put("foto", null);
        listaPerfiles.add(agregarNuevo);

        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil -> {
            int id = (int) perfil.get("id");
            if (id == -1) {
                startActivity(new Intent(this, RegistroActivity.class));
            } else {
                Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);
                intent.putExtra("usuarioId", id);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(perfilAdapter);
    }
}
