package com.example.gestorxpress.database;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestorxpress.R;

public class VerBBDDActivity extends AppCompatActivity {

    private TextView textViewBBDD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_bbdd);

        textViewBBDD = findViewById(R.id.textViewBBDD);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        StringBuilder allData = new StringBuilder();

        // Verificando que el contenido de la base de datos no esté vacío
        String usuariosData = dbHelper.getUsuarios();
        if (usuariosData.isEmpty()) {
            Log.d("Database", "No hay datos de usuarios para mostrar");
        } else {
            allData.append("--- Usuarios ---\n").append(usuariosData).append("\n\n");
        }

        String tareasData = dbHelper.getTareas();
        if (tareasData.isEmpty()) {
            Log.d("Database", "No hay datos de tareas para mostrar");
        } else {
            allData.append("--- Tareas ---\n").append(tareasData).append("\n\n");
        }

        String notificacionesData = dbHelper.getNotificaciones();
        if (notificacionesData.isEmpty()) {
            Log.d("Database", "No hay datos de notificaciones para mostrar");
        } else {
            allData.append("--- Notificaciones ---\n").append(notificacionesData).append("\n\n");
        }

        // Mostrando los datos en el TextView
        textViewBBDD.setText(allData.toString());
    }
}