package com.example.gestorxpress.ui.Cuenta.Suscripcion;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestorxpress.R;
import com.example.gestorxpress.ui.Cuenta.Padre.CuentaPadreActivity;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class CambioDeSuscrip extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_de_suscrip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activar botón de retroceso
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Cuando selecciono la flecha me vuelve a la pagina anterior
        toolbar.setNavigationOnClickListener(v ->
        {
            Intent intent = new Intent(CambioDeSuscrip.this, VisualSuscripcion  .class);
            startActivity(intent);
            finish(); // Opcional
        });

    }
}