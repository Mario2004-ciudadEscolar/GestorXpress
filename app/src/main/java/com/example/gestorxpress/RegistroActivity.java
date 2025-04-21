package com.example.gestorxpress;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private EditText editNombre, editApellido, editCorreo, editContrasena;
    private Button btnRegistrar;

    /** URL del servidor que valida el login */
    private static final String URL_REGISTRO = "http://10.0.2.2:8080/develoGestorXpress/registro_usuario.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editCorreo = findViewById(R.id.editCorreo);
        editContrasena = findViewById(R.id.editContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        /**
         * Se ejecuta cuando le damos el botón de registrar.
         * - Lo que hace es validar los campos del formulario.
         * - Y envía los datos al servidor si todo es correcto.
         */
        btnRegistrar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            String apellido = editApellido.getText().toString().trim();
            String correo = editCorreo.getText().toString().trim();
            String contrasena = editContrasena.getText().toString().trim();

            /**
             * Verifica que todos los campos no esten vacios.
             * Y si alguno está vacío, se muestra un mensaje y se detiene el registro.
             */
            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                    TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            /**
             * Esto lo que hace es crear una solicitud POST con los datos del formulario.
             * Con lo cual se enviará al servidor para crear un nuevo usuario.
             *
             *  Se ejecuta cuando el servidor responde correctamente (HTTP 200).
             *  Interpreta el JSON devuelto y muestra el mensaje al usuario.
             */
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTRO,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            Toast.makeText(RegistroActivity.this, message, Toast.LENGTH_LONG).show();

                            /**
                             * Si el registro fue exitoso, finaliza la actividad
                             * y vuelve a la pantalla anterior (Login).
                             */
                            if (success) {
                                finish(); // Vuelve atrás (al login) si el registro fue exitoso
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegistroActivity.this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show();
                        }
                    },
                    /**
                     * Se ejecuta cuando ocurre un error de red o de conexión con el servidor.
                     */
                    error -> {
                        Toast.makeText(RegistroActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }) {

                /**
                 * Aquí define los parámetros que se enviarán al servidor en la solicitud POST.
                 */
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("nombre", nombre);
                    params.put("apellido", apellido);
                    params.put("correo", correo);
                    params.put("contrasena", contrasena);
                    return params;
                }
            };

            /**
             * Crea una nueva cola de peticiones con Volley y agrega la solicitud creada.
             * Esto permite que la solicitud se envíe al servidor.
             */
            RequestQueue queue = Volley.newRequestQueue(RegistroActivity.this);
            queue.add(stringRequest);
        });
    }
}