package com.example.gestorxpress.ui.Cuenta;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorxpress.LoginActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

public class CuentaActivity extends AppCompatActivity {

    private EditText editCorreo, editPassword, editNombre, editApellido;
    private Button btnEditarGuardar, btnEliminarCuenta;
    private boolean enModoEdicion = false;

    private DatabaseHelper dbHelper;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        dbHelper = new DatabaseHelper(this);

        // Llamamos el metodo que tenemos en nuestra clase de la bbdd para obtener el id
        // del usuario que esta logeado.
        usuarioId = dbHelper.obtenerIdUsuario();

        // Verificamos si se obtuvo un ID válido
        if (usuarioId == -1) {
            // Si el ID es -1, significa que no hay ningún usuario logueado
            Toast.makeText(this, "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            finish();  // Terminamos la actividad si no hay usuario logueado
            return;
        }

        editCorreo = findViewById(R.id.editCorreo);
        editPassword = findViewById(R.id.editPassword);
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        btnEditarGuardar = findViewById(R.id.btnEditarGuardar);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        cargarDatosUsuario();

        btnEditarGuardar.setOnClickListener(v -> {
            if (!enModoEdicion)
            {
                // Activar edición
                enModoEdicion = true;
                cambiarModoEdicion(true);
                btnEditarGuardar.setText("Guardar");
            }
            else
            {
                // Guardar cambios
                guardarCambios();
                enModoEdicion = false;
                cambiarModoEdicion(false);
                btnEditarGuardar.setText("Editar");
            }
        });

        btnEliminarCuenta.setOnClickListener(v -> {
            // Aquí deberías implementar una confirmación antes de eliminar
            boolean eliminado = dbHelper.eliminarUsuarioPorId(usuarioId);
            if (eliminado) {
                Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

    }

    private void cargarDatosUsuario()
    {
        // Si el usuario está logueado (es decir, el ID es válido)
        if (usuarioId != -1)
        {
            // Obtener los datos del usuario directamente desde la base de datos
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT nombre, apellido, correo FROM Usuario WHERE id = ?",
                    new String[]{String.valueOf(usuarioId)});

            if (cursor != null && cursor.moveToFirst())
            {
                // Comprobar si las columnas existen en el cursor
                int nombreIndex = cursor.getColumnIndex("nombre");
                int apellidoIndex = cursor.getColumnIndex("apellido");
                int correoIndex = cursor.getColumnIndex("correo");

                if (nombreIndex != -1 && apellidoIndex != -1 && correoIndex != -1)
                {
                    String nombre = cursor.getString(nombreIndex);
                    String apellido = cursor.getString(apellidoIndex);
                    String correo = cursor.getString(correoIndex);

                    // Establecemos los valores en los EditText
                    editCorreo.setText(correo);
                    editNombre.setText(nombre);
                    editApellido.setText(apellido);
                    editPassword.setText("********");  // No mostrar la contraseña real
                }
                else
                {
                    Log.e("Database", "Las columnas 'nombre', 'apellido' o 'correo' no existen en la consulta.");
                }
                cursor.close();
            }
            else
            {
                Log.d("Database", "No se encontraron datos para el usuario con ID: " + usuarioId);
            }
        }
        else
        {
            Log.d("Database", "No hay usuario logueado.");
        }
    }


    private void cambiarModoEdicion(boolean habilitar)
    {
        editNombre.setEnabled(habilitar);
        editApellido.setEnabled(habilitar);
        editPassword.setEnabled(habilitar);
        // Correo puede mantenerse deshabilitado si no se permite cambiar
    }

    private void guardarCambios()
    {
        String nuevoCorreo = editCorreo.getText().toString().trim();  // <- aquí está el correo
        String nuevoNombre = editNombre.getText().toString().trim();
        String nuevoApellido = editApellido.getText().toString().trim();
        String nuevaPassword = editPassword.getText().toString().trim();

        dbHelper.actualizarUsuario(usuarioId, nuevoNombre, nuevoApellido, nuevoCorreo, nuevaPassword);  // <-- ahora se pasa el correo también
        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
    }

}