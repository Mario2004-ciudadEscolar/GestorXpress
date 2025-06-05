package com.example.gestorxpress.ui.Cuenta.AvisoLegal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestorxpress.R;
import com.example.gestorxpress.ui.Cuenta.Padre.CuentaPadreActivity;

public class AvisosLegales extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos_legales);

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
            Intent intent = new Intent(AvisosLegales.this, CuentaPadreActivity.class);
            startActivity(intent);
            finish(); // Opcional
        });

    }
}