package com.example.gestorxpress.ui.Cuenta.AvisoLegal;

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
import com.example.gestorxpress.ui.Cuenta.SobreGestoXpress.SobreNosotros;

public class AvisosLegales extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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