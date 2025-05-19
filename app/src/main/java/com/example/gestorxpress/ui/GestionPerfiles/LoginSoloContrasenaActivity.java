package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorxpress.MainActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

public class LoginSoloContrasenaActivity extends AppCompatActivity {

    private ImageView perfilImageView;
    private TextView nombreTextView;
    private EditText passwordEditText;
    private Button loginButton;

    private int usuarioId = -1;
    private String correoUsuario;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_contrasena);

        perfilImageView = findViewById(R.id.imagenPerfil);
        nombreTextView = findViewById(R.id.nombreUsuario);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.botonLogin);

        dbHelper = new DatabaseHelper(this);


        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        if (usuarioId == -1) {
            Toast.makeText(this, "ID de usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener nombre, correo e imagen del usuario desde la base de datos
        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery("SELECT nombre, correo, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

        /**
         * Movemos el cursor al principio de lo que hemos obtenido, de ahi sacamos la información
         * de lo que estamos buscando, y por último comprobamos que la imagen no sea nula
         * ya que esa imagen se mostraria en el perfil del usuario.
         */
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(0);
            correoUsuario = cursor.getString(1);
            byte[] imagenBytes = cursor.getBlob(2);

            nombreTextView.setText(nombre);

            if (imagenBytes != null) {
                Bitmap imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                perfilImageView.setImageBitmap(imagen);
            }
        }
        cursor.close();

        /**
         * Cuando el usuario de al boton de iniciar sesión, recogemos la contraseña que introduce.
         * .
         * Primero comprobamos que el campo donde se introduce la contraseña no sea vacia,
         * Si el campo no es vacio, llamamos un metodo donde hacemos la validación del usuario,
         * osea que comprobamos que ese usuario este de alta en nuestra aplicación y tambien
         * comprobams que la contraseña que introduce es la correcta.
         * .
         * Si es correcto se logea en nuestra aplicación y se mostrara la pagina principal
         * donde puede realizar todas las funcionalidades de nuestra aplicación.
         * .
         * Si no es correcto la contraseña, se le mostrara una advertencia.
         */
        loginButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean loginCorrecto = dbHelper.validarUsuario(correoUsuario, password);

            if (loginCorrecto) {
                boolean esPadre = dbHelper.esUsuarioPadrePorId(usuarioId);
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
