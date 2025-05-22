package com.example.gestorxpress.ui.Cuenta;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.MainActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.GestionPerfiles.LoginSoloContrasenaActivity;
import com.example.gestorxpress.ui.GestionPerfiles.PerfilAdapterSinClase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuentaPadreActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private PerfilAdapterSinClase perfilAdapter;
    private List<HashMap<String, Object>> listaPerfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta_padre);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activar botón de retroceso
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Manejar la flecha
        toolbar.setNavigationOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Opcional
        });

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
            boolean primerRegistroSaltado = false;
            do {
                if (!primerRegistroSaltado)
                {
                    // Saltamos el primer perfil (primer registro)
                    primerRegistroSaltado = true;
                    continue;  // va al siguiente registro sin añadir nada
                }
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

    @Override
    public boolean onSupportNavigateUp() {
        // Cuando se pulse la flecha, vuelve al MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Opcional, para cerrar esta activity
        return true;
    }

}
