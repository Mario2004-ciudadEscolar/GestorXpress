package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private EditText editNombre, editPassword;
    private Button btnLogin;
    private TextView signupText;

    /** Cola de peticiones de Volley para manejar solicitudes HTTP */
    private RequestQueue requestQueue;

    /** URL del servidor que valida el login */
    private static final String URL_LOGIN = "http://10.0.2.2:80/develoGestorXpress/login_usuario.php";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editNombre = findViewById(R.id.editNombre);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        requestQueue = Volley.newRequestQueue(this);
        signupText = findViewById(R.id.signupText);

        /** Si no tiene cuenta en nuestra aplicación y le da al "no tengo cuenta"
         *  le lleva a otra pagina donde se puede registrar en nuestra aplicación
         *  */
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        /** Se activa cuando le damos al boton de login,
         *  obtenemos el correo y contraseña del usuario
         *  donde luego comporbamos que no esten vacios esos campos,
         *  */
        btnLogin.setOnClickListener(v -> {
            String correo = editNombre.getText().toString().trim();
            String contrasenia = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasenia)) {
                Toast.makeText(this, "Por favor, introduce correo electrónico y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            /** Esto crea una solicitud HTTP POST usando la biblioteca Volley.
             *  Esto enviará los datos de correo y contraseña al servidor para la validación.
             * */
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("LoginActivity", "Full JSON Response: " + response); // Linea donde compruebo que tipo de respuesta obtengo
                            try {
                                /** Parsea la respuesta JSON del servidor.
                                 *  Se espera un objeto con "success", "message" y posiblemente "user_id".
                                 */
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                String message = jsonResponse.getString("message");

                                /** Si el login es exitoso, muestra mensaje de bienvenida
                                 *  y navega a la actividad principal (MainActivity).
                                 */
                                if (success) {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    /**
                                     * Si el JSON incluye el ID del usuario, se pasa como extra al intent.
                                     */
                                    if (jsonResponse.has("user_id")) {
                                        intent.putExtra("user_id", jsonResponse.getInt("user_id"));
                                    }
                                    /**
                                     * También se pasa el correo del usuario como extra.
                                     * Y finalmente se inicia la nueva actividad y finaliza la actual.
                                     */
                                    intent.putExtra("correo", correo);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Si el login falla, se muestra el mensaje recibido del servidor.
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                // Si ocurre un error al interpretar el JSON, se informa al usuario.
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Error al parsear la respuesta del servidor", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    /**
                     * Se ejecuta cuando hay un error en la conexión o en la petición HTTP.
                     */
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("LoginActivity", "Volley Error Details:", error); // Añade esta línea para más detalles
                            Toast.makeText(LoginActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {

                /**
                 * Esto lo que hace es definir los parámetros (correo y contraseña) que se enviarán en la solicitud POST.
                 */
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", correo);
                    params.put("contrasena", contrasenia);
                    return params;
                }
            };

            /**
             * Agrega la solicitud HTTP a la cola de ejecución de Volley para que se ejecute.
             */
            requestQueue.add(stringRequest);
        });



    }
}
