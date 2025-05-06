package com.example.gestorxpress;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.database.VerBBDDActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editNombre, editPassword;
    private Button btnLogin;
    private TextView signupText, verBaseDatos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editNombre = findViewById(R.id.editNombre);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);
        verBaseDatos = findViewById(R.id.verBaseDatos);

        /**
         * Inicializa la base de datos (esto crea o abre la base si ya existe)
         */
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        /**
         * Si el usuario hace click en "No estoy registrado (Esta en ingles)"
         * Nos enviara a la pagina de Registro, donde el usuario se va a dar de alta en
         * nuestra aplicación.
         */
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        verBaseDatos.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, VerBBDDActivity.class);
            startActivity(intent);
        });

        /**
         * Cuando el usuario le da al boton de login, lo que hace es primero comprobar
         * si el usuario ha puesto el correo y la contraseña, ya que si algún campo
         * esta vacio, daria un error, que se mostraria en el terminal
         * (como punto de mejora, poner un popup donde informe un advertencia o error)
         *
         * Si eso campos no esta vacio, pasaria a la siguiente parte, que es donde
         * se llama el metodo validarUsuario, que es un metodo que esta en la clase
         * DayabaseHelper, y lo que hace es comprobar que ese correo y contraseña exista en nuestra BBDD.
         *
         * Si es correcto se inicia la sesión y se abrira la pagina principal con todas las funciones,
         * si no es correcta nos mostraria un error.
         */
        btnLogin.setOnClickListener(v -> {
            String correo = editNombre.getText().toString().trim();
            String contrasenia = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasenia)) {
                Toast.makeText(this, "Por favor, introduce correo electrónico y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean loginCorrecto = dbHelper.validarUsuario(correo, contrasenia);

            if (loginCorrecto) {
                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("correo", correo);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
            }
        });



    }
}
