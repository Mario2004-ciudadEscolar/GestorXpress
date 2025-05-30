package com.example.gestorxpress.ui.Cuenta.Suscripcion;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gestorxpress.MainActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.ui.Cuenta.Padre.CuentaPadreActivity;
import com.example.gestorxpress.ui.Cuenta.Padre.EditarBorrarCuenta;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class VisualSuscripcion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_suscripcion);

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
            Intent intent = new Intent(VisualSuscripcion.this, CuentaPadreActivity.class);
            startActivity(intent);
            finish(); // Opcional
        });

        // Llamada al metodo donde iniciamos otro MainActivity (otra pagina)
        llamadasActivity();

    }

    /**
     * Este metodo se utiliza para llamar a los activity (otras paginas) cuando seleccionamos o
     *  clicamos sobre un boton o un Texto
     */
    private void llamadasActivity()
    {

        findViewById(R.id.btn_cambioPlan).setOnClickListener(v ->
        {
            Intent intent = new Intent(VisualSuscripcion.this, CambioDeSuscrip.class);
            startActivity(intent);
        });


    }
}