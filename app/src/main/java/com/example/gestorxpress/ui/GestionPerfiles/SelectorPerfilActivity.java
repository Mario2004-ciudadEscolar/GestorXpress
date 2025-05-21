package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private List<HashMap<String, Object>> listaPerfiles;

    /**
     * Método llamado al crear la actividad.
     * Infla la vista, configura el RecyclerView, carga los perfiles desde la base de datos
     * y configura la acción de clic para seleccionar perfil o añadir uno nuevo.
     *
     * @param savedInstanceState Bundle con estado previo de la actividad.
     */
   /* @Override
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

        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil -> {
            int id = (int) perfil.get("id");
            Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);
            intent.putExtra("usuarioId", id);
            startActivity(intent);
        });

        // Asocia el adaptador al RecyclerView para mostrar los perfiles
        recyclerView.setAdapter(perfilAdapter);

        // Define ancho total efectivo por ítem en dp (imagen + márgenes)
        final int itemWidthDp = 64 + 6 + 6; // imagen + margin left + margin right
        final int itemWidthPx = dpToPx(itemWidthDp);
        final int itemMarginPx = dpToPx(6);

        // Ajustamos padding para centrar los items si caben todos
        recyclerView.post(() -> {
            int itemCount = perfilAdapter.getItemCount();
            int recyclerViewWidth = recyclerView.getWidth();
            int totalItemsWidth = itemWidthPx * itemCount;

            if (totalItemsWidth < recyclerViewWidth) {
                int sidePadding = (recyclerViewWidth - totalItemsWidth) / 2;
                recyclerView.setPadding(sidePadding, recyclerView.getPaddingTop(), sidePadding, recyclerView.getPaddingBottom());
            } else {
                recyclerView.setPadding(itemMarginPx, recyclerView.getPaddingTop(), itemMarginPx, recyclerView.getPaddingBottom());
            }
        });

        // Añadimos decoración para espacio entre items
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = state.getItemCount();
                if (position != itemCount - 1) {
                    outRect.right = itemMarginPx;  // margen derecha excepto último
                }
            }
        });

        // Configura el clic en el ícono “añadir perfil” para abrir la actividad de registro
        findViewById(R.id.iconoAgregar).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }*/


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_perfil);

        recyclerView = findViewById(R.id.recyclerViewPerfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dbHelper = new DatabaseHelper(this);
        listaPerfiles = new ArrayList<>();

        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil -> {
            int id = (int) perfil.get("id");
            Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);
            intent.putExtra("usuarioId", id);
            startActivity(intent);
        });

        recyclerView.setAdapter(perfilAdapter);

        final int itemWidthDp = 64 + 6 + 6;
        final int itemWidthPx = dpToPx(itemWidthDp);
        final int itemMarginPx = dpToPx(6);

        recyclerView.post(() -> {
            int itemCount = perfilAdapter.getItemCount();
            int recyclerViewWidth = recyclerView.getWidth();
            int totalItemsWidth = itemWidthPx * itemCount;

            if (totalItemsWidth < recyclerViewWidth) {
                int sidePadding = (recyclerViewWidth - totalItemsWidth) / 2;
                recyclerView.setPadding(sidePadding, recyclerView.getPaddingTop(), sidePadding, recyclerView.getPaddingBottom());
            } else {
                recyclerView.setPadding(itemMarginPx, recyclerView.getPaddingTop(), itemMarginPx, recyclerView.getPaddingBottom());
            }
        });

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = state.getItemCount();
                if (position != itemCount - 1) {
                    outRect.right = itemMarginPx;
                }
            }
        });

        findViewById(R.id.iconoAgregar).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });

        cargarPerfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPerfiles();  // recargar datos al volver a esta pantalla
    }

    private void cargarPerfiles() {
        listaPerfiles.clear();

        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

                    Bitmap imagen = null;
                    int fotoIndex = cursor.getColumnIndexOrThrow("fotoPerfil");
                    if (!cursor.isNull(fotoIndex)) {
                        byte[] imagenBytes = cursor.getBlob(fotoIndex);
                        if (imagenBytes != null && imagenBytes.length > 0) {
                            imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        }
                    }

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombre);
                    map.put("foto", imagen);
                    listaPerfiles.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        perfilAdapter.notifyDataSetChanged();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

}
