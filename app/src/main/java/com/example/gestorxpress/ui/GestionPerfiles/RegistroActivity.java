package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class RegistroActivity extends AppCompatActivity {

    private EditText editNombre, editApellido, editCorreo, editContrasena, editRepetirContrasena;
    private Button btnRegistrar;
    private ImageView imagenSeleccionada;
    private byte[] imagenEnBytes;
    private static final int REQUEST_GALERIA = 1;


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
        editRepetirContrasena = findViewById(R.id.editRepetirContrasena);

        imagenSeleccionada = findViewById(R.id.imagenSeleccionada);
        imagenSeleccionada.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Selecciona imagen de perfil")
                    .setItems(new CharSequence[]{"Elegir de la galería", "Elegir avatar predefinido"}, (dialog, which) -> {
                        if (which == 0) {
                            // Galería
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_GALERIA);
                        } else {
                            // Avatares predefinidos
                            SelectorAvatarDialog dialogo = new SelectorAvatarDialog(imagen -> {
                                imagenEnBytes = imagen;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
                                imagenSeleccionada.setImageBitmap(bitmap);
                            });
                            dialogo.show(getSupportFragmentManager(), "selector_avatar");
                        }
                    })
                    .show();
        });




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
                    TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena))
            {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validación de formato de correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches())
            {
                Toast.makeText(this, "Formato de correo invalido [usuario@dominio.com] ", Toast.LENGTH_LONG).show();
                return;
            }


            String repetirContrasena = editRepetirContrasena.getText().toString().trim();

            if (!contrasena.equals(repetirContrasena)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                return;
            }

            // Intentamos registrar al usuario en la base de datos
            if (imagenEnBytes == null) {
                // Imagen por defecto si no selecciona una
                try {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_person_24); //esto sirve para comvertir la imagen a bits y guardarlo en la bbdd
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imagenEnBytes = stream.toByteArray();


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar imagen por defecto", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean registroExitoso = dbHelper.registrarUsuario(nombre, apellido, correo, contrasena, imagenEnBytes);


            if (registroExitoso)
            {
                Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegistroActivity.this, SelectorPerfilActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(RegistroActivity.this, "Correo ya registrado", Toast.LENGTH_LONG).show();
            }

        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALERIA && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                imagenSeleccionada.setImageURI(imageUri);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                imagenEnBytes = getBytes(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}