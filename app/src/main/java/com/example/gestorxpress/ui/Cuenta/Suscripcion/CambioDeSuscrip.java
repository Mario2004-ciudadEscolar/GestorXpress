package com.example.gestorxpress.ui.Cuenta.Suscripcion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

    private TextView btnMensual;
    private TextView btnAnual;
    private TextView precioEstandar;
    private TextView periodoEstandar;
    private boolean esMensual = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_de_suscrip);

        // Inicializar vistas
        btnMensual = findViewById(R.id.btn_mensual);
        btnAnual = findViewById(R.id.btn_anual);
        precioEstandar = findViewById(R.id.precio_estandar);
        periodoEstandar = findViewById(R.id.periodo_estandar);

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

        // Configurar listeners para los botones de toggle
        btnMensual.setOnClickListener(v -> cambiarPlan(true));
        btnAnual.setOnClickListener(v -> cambiarPlan(false));
    }

    private void cambiarPlan(boolean mensual) {
        esMensual = mensual;
        
        // Actualizar estilos de los botones
        btnMensual.setBackgroundResource(mensual ? R.color.toggle_selected : android.R.color.transparent);
        btnAnual.setBackgroundResource(mensual ? android.R.color.transparent : R.color.toggle_selected);
        
        btnMensual.setTextColor(getResources().getColor(mensual ? android.R.color.white : R.color.text_unselected));
        btnAnual.setTextColor(getResources().getColor(mensual ? R.color.text_unselected : android.R.color.white));

        // Actualizar precio y periodo
        if (mensual) {
            precioEstandar.setText("4,99 €");
            periodoEstandar.setText("/mes");
        } else {
            precioEstandar.setText("49,90 €");
            periodoEstandar.setText("/año");
        }
    }
}