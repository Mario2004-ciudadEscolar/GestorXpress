package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.gestorxpress.database.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private EditText editNombre, editApellido, editCorreo, editContrasena;
    private Button btnRegistrar;

    // Instancia de la base de datos local
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editCorreo = findViewById(R.id.editCorreo);
        editContrasena = findViewById(R.id.editContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Inicializamos la base de datos
        dbHelper = new DatabaseHelper(this);

        /**
         * Se ejecuta cuando le damos el botón de registrar.
         * - Lo que hace es validar los campos del formulario.
         * - Y registra el usuario en la base de datos local si todo es correcto.
         */
        btnRegistrar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            String apellido = editApellido.getText().toString().trim();
            String correo = editCorreo.getText().toString().trim();
            String contrasena = editContrasena.getText().toString().trim();

            /**
             * Verifica que todos los campos no estén vacíos.
             * Y si alguno está vacío, se muestra un mensaje y se detiene el registro.
             */
            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                    TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intentamos registrar al usuario en la base de datos
            boolean registroExitoso = dbHelper.registrarUsuario(nombre, apellido, correo, contrasena);

            if (registroExitoso) {
                Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegistroActivity.this, "Correo ya registrado", Toast.LENGTH_LONG).show();
            }

        });
    }
}