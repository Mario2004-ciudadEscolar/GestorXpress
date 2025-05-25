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

        if (usuarioId == -1) {
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

        cargarDatosUsuario();

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

    private void cargarDatosUsuario() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, apellido, correo, fotoPerfil FROM Usuario WHERE id = ?", new String[]{String.valueOf(usuarioId)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nombreIndex = cursor.getColumnIndex("nombre");
                int apellidoIndex = cursor.getColumnIndex("apellido");
                int correoIndex = cursor.getColumnIndex("correo");
                int fotoIndex = cursor.getColumnIndex("fotoPerfil");

                if (nombreIndex != -1 && apellidoIndex != -1 && correoIndex != -1 && fotoIndex != -1) {
                    String nombre = cursor.getString(nombreIndex);
                    String apellido = cursor.getString(apellidoIndex);
                    String correo = cursor.getString(correoIndex);

                    if (!cursor.isNull(fotoIndex)) {
                        byte[] imagenBytes = cursor.getBlob(fotoIndex);
                        if (imagenBytes != null && imagenBytes.length > 0) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                            imgPerfil.setImageBitmap(bitmap);
                            imagenEnBytes = imagenBytes;
                        }
                    }

                    editCorreo.setText(correo);
                    editNombre.setText(nombre);
                    editApellido.setText(apellido);
                    editPassword.setText("********");
                } else {
                    Toast.makeText(this, "Error en la estructura de los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se encontraron datos para el usuario", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Error al acceder a los datos", Toast.LENGTH_SHORT).show();
        }
    }


    private void cambiarModoEdicion(boolean habilitar) {
        editNombre.setEnabled(habilitar);
        editApellido.setEnabled(habilitar);
        editPassword.setEnabled(habilitar);
    }

    private void guardarCambios() {
        String nuevoCorreo = editCorreo.getText().toString().trim();
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevoApellido = editApellido.getText().toString().trim();
        String nuevaPassword = editPassword.getText().toString().trim();

        if (nuevaPassword.equals("********") || nuevaPassword.isEmpty()) {
            nuevaPassword = null;
        }

        dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword, imagenEnBytes);
        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
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