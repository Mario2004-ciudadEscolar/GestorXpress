package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class RegistroActivity extends AppCompatActivity {

    // Atributos
    private EditText editNombre, editApellido, editCorreo, editContrasena, editRepetirContrasena;
    private Button btnRegistrar;
    private ImageView imagenSeleccionada;
    private byte[] imagenEnBytes;
    private static final int REQUEST_GALERIA = 1;
    private boolean imagenSeleccionadaPorUsuario = false;


    // Instancia de la base de datos local
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro); // Instancia al xml

        // Referencia a los elementos de la interfaz (XML)
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editCorreo = findViewById(R.id.editCorreo);
        editContrasena = findViewById(R.id.editContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        editRepetirContrasena = findViewById(R.id.editRepetirContrasena);
        imagenSeleccionada = findViewById(R.id.imagenSeleccionada);

        /**
         * Muestra un diálogo que permite al usuario seleccionar una imagen de perfil,
         * ya sea desde la galería del dispositivo o desde un conjunto de avatares predefinidos.
         *.
         * Si el usuario elige una imagen, se muestra en el ImageView y se guarda como un arreglo de bytes.
         */
        imagenSeleccionada.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Selecciona imagen de perfil")
                    .setItems(new CharSequence[]{"Elegir de la galería", "Elegir avatar predefinido"}, (dialog, which) -> {
                        if (which == 0)
                        {
                            // Opción: abrir galería de imágenes
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_GALERIA);
                        }
                        else
                        {
                            // Opción: seleccionar avatar predefinido
                            SelectorAvatarDialog dialogo = new SelectorAvatarDialog(imagen -> {
                                imagenEnBytes = imagen;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
                                imagenSeleccionadaPorUsuario = true;

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
        btnRegistrar.setOnClickListener(v ->
        {
            // Obtenemos el texto introducido en los EditText en una variable
            String nombre = editNombre.getText().toString().trim();
            String apellido = editApellido.getText().toString().trim();
            String correo = editCorreo.getText().toString().trim();
            String contrasena = editContrasena.getText().toString().trim();
            String contrasena2 = editRepetirContrasena.getText().toString().trim();

            /**
             * Verifica que todos los campos no estén vacíos.
             * Y si alguno está vacío, se muestra un mensaje y se detiene el registro.
             */
            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                    TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)
                    || TextUtils.isEmpty((contrasena2)))
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

            // Aqui valido que la contraseña contenga mayusculas, caracter especial
            // y que la contraseña sea igual o mayor de 6 digitos
            if(!esContraseniaValida(contrasena) )
            {
                Toast.makeText(this, "Contraseña no válida: debe contener una mayúscula, " +
                        "un carácter especial y tener más de 6 caracteres.", Toast.LENGTH_LONG).show();
                return;
            }

            /**
             * Validamos que las dos contraseñas que introduce el usuario a la hora de darse de alta
             * coincidan, ya que asi el usuario comprueba y afirma que ha puesto la contraseña que
             * queria poner.
             *.
             * Si las dos contraseñas no coinciden, no le dejara registrarse y le mostramos un mensaje informativo
             * indicandole que las contraseñas introducidas no coinciden.
             */
            if (!contrasena.equals(contrasena2))
            {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                return;
            }


            /**
             * Asigna una imagen de perfil por defecto si el usuario no ha seleccionado ninguna.
             *.
             * La imagen por defecto se obtiene de los recursos (por ejemplo, un ícono de persona),
             * se convierte a Bitmap y luego a un arreglo de bytes (`imagenEnBytes`) para poder almacenarla o mostrarla.
             *.
             * También maneja errores en caso de que la imagen no pueda cargarse correctamente.
             */
            if (!imagenSeleccionadaPorUsuario)

            {
                try
                {
                    Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_person_24);
                    if (drawable != null)
                    {
                        Bitmap bitmap;
                        if (drawable instanceof BitmapDrawable)
                        {
                            bitmap = ((BitmapDrawable) drawable).getBitmap();
                        }
                        else
                        {
                            int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 100;
                            int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 100;
                            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            drawable.draw(canvas);
                        }

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        imagenEnBytes = stream.toByteArray();
                    }
                    else
                    {
                        Toast.makeText(this, "No se pudo cargar la imagen por defecto", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar imagen por defecto", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            /**
             * Una vez hayamos comprobado que toda la información que necesitamos para registrar el usuario
             * este correcto, procedemos a registrar dicho usuario, llamando un metodo que esta en la clase
             * donde esta nuestra bbdd.
             *
             * Devuelve un boolean para comrpbar si se ha registrado o no.
             */
            boolean registroExitoso = dbHelper.registrarUsuario(nombre, apellido, correo, contrasena, imagenEnBytes);

            /**
             * Comprobamos si ha tenido exito al registrarse, y si es asi le enviamos a la pagina principal,
             * que es la familiar y donde puede ver todos los usuario creados, incluyendo la suya.
             *
             * Si no ha tenido existo, le mostramos un mensaje informativo indicandole el error.
             */
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
            imagenSeleccionadaPorUsuario = true;
        }
    }

    /**
     * Lee todos los datos de un {@link InputStream} y los convierte en un arreglo de bytes.
     *.
     * Este método es útil cuando necesitas convertir archivos, imágenes u otros flujos de datos
     * en memoria para almacenarlos o procesarlos como un arreglo de bytes (`byte[]`).
     *.
     * @param inputStream El flujo de entrada desde el cual se leerán los datos.
     * @return Un arreglo de bytes que contiene todos los datos leídos del InputStream.
     * @throws IOException Si ocurre un error de lectura durante el proceso.
     */
    private byte[] getBytes(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1)
        {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Valida que la contraseña cumpla con los requisitos:
     * - Mínimo 7 caracteres
     * - Que contenga al menos una letra mayúscula
     * - Que contenga al menos un carácter especial
     *
     * @param contrasenia La contraseña a validar
     * @return true si cumple, false si no cumple
     */
    public boolean esContraseniaValida(String contrasenia)
    {
        if (contrasenia == null) return false;
        if (contrasenia.length() <= 6) return false;

        // Para que al menos contenga una mayúscula
        boolean tieneMayuscula = contrasenia.matches(".*[A-Z].*");

        // Para que al menos contenga un carácter especial
        boolean tieneCaracterEspecial = contrasenia.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>?/].*");

        return tieneMayuscula && tieneCaracterEspecial;
    }

}