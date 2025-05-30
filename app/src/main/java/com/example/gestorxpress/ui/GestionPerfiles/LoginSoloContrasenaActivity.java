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

    // Atributos
    private ImageView perfilImageView;
    private TextView nombreTextView;
    private EditText passwordEditText;
    private Button loginButton;
    private int usuarioId = -1;
    private String correoUsuario;

    // Clase DatabaseHelper (Donde realizamos todas las interacciones con la bbdd SLQite)
    private DatabaseHelper dbHelper;

    boolean esPadre = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_contrasena); // Instancia al XML

        // Los atributos que hemos inicializa anteriormente lo asignamos con los id de los
        // EditText, Button, Label, ImageView, etx... del xml que esta asociado a esta clase.
        perfilImageView = findViewById(R.id.imagenPerfil);
        nombreTextView = findViewById(R.id.nombreUsuario);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.botonLogin);

        // Instancia a la clase DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // En la anterior pagina enviamos el id del usuario como valor extra a este MainActivity
        // Y ese ID que hemos obtenido lo guardamos en una variable.
        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        // Comprobamos si el id que obtenemos es '-1', eso quiere decir que no hemos obtenido ningun id
        // o que no existe en la bbdd.
        if (usuarioId == -1)
        {
            // Mostramos un mensaje informativo, indicando el error.
            Toast.makeText(this, "ID de usuario no válido", Toast.LENGTH_SHORT).show();
            finish(); // Finalizamos esta pagina ya que no existe el usuario en nuesta bbdd.
            return;
        }

        // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
        // Obtener nombre, correo e imagen del usuario desde la base de datos
        Cursor consulta = dbHelper.getReadableDatabase()
                .rawQuery("SELECT nombre, correo, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

        /**
         * Movemos el cursor al principio de lo que hemos obtenido, de ahi sacamos la información
         * de lo que estamos buscando, y por último comprobamos que la imagen no sea nula
         * ya que esa imagen se mostraria en el perfil del usuario.
         */
        if (consulta.moveToFirst())
        {
            String nombre = consulta.getString(0);
            correoUsuario = consulta.getString(1);
            byte[] imagenBytes = consulta.getBlob(2);

            nombreTextView.setText(nombre);

            if (imagenBytes != null)
            {
                Bitmap imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                perfilImageView.setImageBitmap(imagen);
            }
        }
        // Cerramos la consulta
        consulta.close();

        /**
         * Cuando el usuario de al boton de iniciar sesión, recogemos la contraseña que introduce.
         * .
         * 1. Primero comprobamos que el campo donde se introduce la contraseña no sea vacia,
         *    Si el campo no es vacio, llamamos un metodo donde hacemos la validación del usuario,
         *    osea que comprobamos que ese usuario este de alta en nuestra aplicación y tambien
         *    comprobams que la contraseña que introduce es la correcta.
         * .
         * 2. Si es correcto se logea en nuestra aplicación y se mostrara la pagina principal
         *    donde puede realizar todas las funcionalidades de nuestra aplicación.
         * .
         * 3. Si no es correcto la contraseña, se le mostrara una advertencia.
         */
        loginButton.setOnClickListener(v ->
        {
            // Obtenemos el texto introducido en el campo de contraseña, eliminando espacios al inicio y final
            String password = passwordEditText.getText().toString().trim();

            // Comprobamos que la contraseña recogida del 'passwordEditText' sea vacia
            if (TextUtils.isEmpty(password))
            {
                // Si es asi, se mostrara un mensaje de error.
                Toast.makeText(this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // Llama al método del DatabaseHelper de base de datos para validar si el usuario y la contraseña son correctos
            boolean loginCorrecto = dbHelper.validarUsuario(correoUsuario, password);

            // Si es correcto...
            if (loginCorrecto)
            {
                // Llama al metodo para verificar si el usuario en la que estamos inciando sesión es el padre (administrador)
                esPadre = dbHelper.esUsuarioPadrePorId(usuarioId);

                // Mostramos un mensaje de inicio de sesión exitoso
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                // Crea un intent para ir a la pantalla principal (MainActivity)
                Intent intent = new Intent(this, MainActivity.class);

                // Iniciamos el MainActivity (otra pagina)
                startActivity(intent);

                // Finaliza la actividad actual para que no se pueda volver atrás con el botón de retroceso
                finish();
            }
            else
            {
                // Si la contraseña es incorrecta, muestra un mensaje de error
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
