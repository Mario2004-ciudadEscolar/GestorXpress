package com.example.gestorxpress.ui.Cuenta.Padre;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Cuenta.Suscripcion.VisualSuscripcion;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorAvatarDialog;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 */
public class EditarBorrarCuenta extends AppCompatActivity {

    private ImageView imgPerfil;
    private byte[] imagenEnBytes;
    private EditText editCorreo, editPassword, editNombre, editApellido;
    private Button btnEditarGuardar, btnEliminarCuenta;
    private boolean enModoEdicion = false;
    private DatabaseHelper dbHelper;
    private int usuarioId;

    private ActivityResultLauncher<Intent> galeriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cuenta_hijo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activar botón de retroceso
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Manejar la flecha
        toolbar.setNavigationOnClickListener(v ->
        {
            Intent intent = new Intent(EditarBorrarCuenta.this, CuentaPadreActivity.class);
            startActivity(intent);
            finish(); // Opcional
        });

        dbHelper = new DatabaseHelper(this);

        // Recuperar ID desde el Intent
        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        // Comprobamos que haya obtenido un id, osea que no sea '-1' que eso significa que no obtuvo nada desde el Inten anterior.
        if (usuarioId == -1)
        {
            usuarioId = dbHelper.obtenerIdUsuario();
            if (usuarioId == -1)
            {
                Toast.makeText(this, "No se ha recibido ID del usuario", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Inicializar vistas
        imgPerfil = findViewById(R.id.imgPerfil);
        editCorreo = findViewById(R.id.editCorreo);
        editPassword = findViewById(R.id.editPassword);
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        btnEditarGuardar = findViewById(R.id.btnEditarGuardar);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        /**
         * Llamamos a un metodo donde vamos a recuperar los datos del usuario que esta logeado en este
         * momento en nuestra aplicación y cargarlo en la información de "cuenta", ya que ahi puede editar
         * la cuenta ya sea el correo, nombre, apellido o contraseña,
         */
        cargarDatosUsuario();

        /**
         * Lo que hacemos aqui es que cuando el usuario de el boton de editar, ese boton pase
         * ser a guardar, ya que cambiamos el nombre y el modo de edición que lo logramos
         * haciendolo con un boolena.
         */
        btnEditarGuardar.setOnClickListener(v -> {
            if (!enModoEdicion) {
                enModoEdicion = true;
                cambiarModoEdicion(true);
                btnEditarGuardar.setText("Guardar");
            } else {
                guardarCambios();
                enModoEdicion = false;
                cambiarModoEdicion(false);
                btnEditarGuardar.setText("Editar");
            }
        });

        /**
         * Si el usuario da el boton de eliminar, le mostramos un mensaje informativo, diciendole
         * si de verdad quiere borrar su cuenta, ya si el usuario indica que si, eliminamos la cuenta,
         * lo borramos tambien de nuestra bbdd, que para ello tenemos un metodo en la clase bbdd que
         * ya hace la función de borrar la cuenta de nuestra bbdd.
         *
         * Y por ultimo le enviamos a la pagina principal (donde se inicia o registra).
         */
        btnEliminarCuenta.setOnClickListener(v ->
        {
            boolean eliminado = dbHelper.eliminarUsuarioPorId(usuarioId);

            if (eliminado)
            {
                Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show(); // ✅ CORREGIDO
                startActivity(new Intent(this, SelectorPerfilActivity.class));        // ✅ CORREGIDO
                finish();
            }
        });

        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imgPerfil.setImageURI(imageUri);
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            imagenEnBytes = getBytes(inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        /**
         * Configura un listener para el clic en la imagen de perfil (imgPerfil) que permite al usuario
         * seleccionar una nueva imagen solo si está en modo edición.
         *.
         * Al hacer clic, se muestra un diálogo con dos opciones:
         * 1. Seleccionar una imagen desde la galería.
         * 2. Elegir un avatar predefinido desde un diálogo personalizado.
         *.
         * La imagen seleccionada se convierte a {@code byte[]} y se muestra en el {@code ImageView}.
         */
        imgPerfil.setOnClickListener(v -> {
            if (!enModoEdicion) return;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Selecciona nueva imagen de perfil")
                    .setItems(new CharSequence[]{"Desde galería", "Desde avatares predefinidos"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            galeriaLauncher.launch(intent);
                        } else {
                            SelectorAvatarDialog dialogo = new SelectorAvatarDialog(imagen -> {
                                imagenEnBytes = imagen;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
                                imgPerfil.setImageBitmap(bitmap);
                            });
                            dialogo.show(getSupportFragmentManager(), "selector_avatar");
                        }
                    }).show();
        });
    }

    /**
     * Lo que hacemos en este metodo es cargar la información del usuario y mostrarlo en la pagina,
     * ya que aqui el usuario puede ver su información personal y lo puede editar o actualizar si asi lo desea
     * el usuario o tambien puede eliminar su cuenta (pero eso ya se hace con otro metodo que tenemos
     * definifo).
     */
    private void cargarDatosUsuario() {
        // Generamos la conexión a la bbdd
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Obtener los datos del usuario directamente desde la base de datos
        Cursor consulta = db.rawQuery("SELECT nombre, apellido, correo, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

        // Comprobamos que la consulta no sea nula (que por lo menos haya obtenido un dato)
        // Y movemos el curso a la primera fila de la tabla usuario
        if (consulta != null && consulta.moveToFirst()) {
            int nombreIndex = consulta.getColumnIndex("nombre");
            int apellidoIndex = consulta.getColumnIndex("apellido");
            int correoIndex = consulta.getColumnIndex("correo");
            int fotoIndex = consulta.getColumnIndex("fotoPerfil");

            // Comprobamos que los datos que hemos obtenidos no sean nulos
            if (nombreIndex != -1 && apellidoIndex != -1 && correoIndex != -1 && fotoIndex != -1)
            {
                byte[] imagenBytes = consulta.getBlob(fotoIndex);
                String nombre = consulta.getString(nombreIndex);
                String apellido = consulta.getString(apellidoIndex);
                String correo = consulta.getString(correoIndex);

                if (imagenBytes != null)
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                    imgPerfil.setImageBitmap(bitmap);
                    imagenEnBytes = imagenBytes;
                }

                // Mostramos la información personal sacada al usuario y lo ponemos en los editText
                editCorreo.setText(correo);
                editNombre.setText(nombre);
                editApellido.setText(apellido);
                editPassword.setText("********");
            }

            consulta.close();
        }
    }

    /**
     * Lo que hace este metodo es que cuando el usuario quiere editar su información, es que
     * habilitamos los editTest para que puede editarlo o actualizar su información.
     * @param habilitar Para habilitar la información.
     */
    private void cambiarModoEdicion(boolean habilitar) {
        editNombre.setEnabled(habilitar);
        editApellido.setEnabled(habilitar);
        editPassword.setEnabled(habilitar);
    }

    /**
     * Si el usuario realiza algún cambio, lo que hacemos es un UPDATE en la bbdd
     * que para ello ya tenemos un metodo en nuestra bbdd.
     * .
     * NOTA: TENGO QUE DARLE UNA VUELTA ESTE METODO, SE PUEDE HACER MEJOR.
     */
    private void guardarCambios() {
        String nuevoCorreo = editCorreo.getText().toString().trim();
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevoApellido = editApellido.getText().toString().trim();
        String nuevaPassword = editPassword.getText().toString().trim();

        // Si el campo sigue con los ****** no cambiamos la contraseña
        // que me daba error y si cambio solo la foto me cambiaba la contraseña y no podia entrar una liada
        if (nuevaPassword.equals("********") || nuevaPassword.isEmpty()) {
            nuevaPassword = null;
        }

        dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword, imagenEnBytes);
        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
    }

    /**
     * Lee todos los datos de un {@link InputStream} y los convierte en un arreglo de bytes.
     *.
     * Este método es útil cuando necesitas convertir archivos, imágenes u otros flujos de datos
     * en memoria para almacenarlos o procesarlos como un arreglo de bytes (byte[]).
     *.
     * @param inputStream El flujo de entrada desde el cual se leerán los datos.
     * @return Un arreglo de bytes que contiene todos los datos leídos del InputStream.
     * @throws IOException Si ocurre un error de lectura durante el proceso.
     */
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