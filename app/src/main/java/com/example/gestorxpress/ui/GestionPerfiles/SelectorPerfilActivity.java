package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.google.android.flexbox.FlexboxLayout;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class SelectorPerfilActivity extends AppCompatActivity
{

    // Declaramos la clase DataBaseHelper
    private DatabaseHelper dbHelper;

    // Layout flexible para mostrar perfiles horizontalmente
    private FlexboxLayout flexboxPerfiles;

    /**
     * Método llamado al crear la actividad.
     * Infla la vista, configura el RecyclerView, carga los perfiles desde la base de datos
     * y configura la acción de clic para seleccionar perfil o añadir uno nuevo.
     *
     * @param savedInstanceState Bundle con estado previo de la actividad.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_perfil);

        // El FlexboxLayout es donde se añade dinámicamente las vistas de cada perfil
        // (usuario) de forma flexible y adaptable al tamaño de la pantalla.
        flexboxPerfiles = findViewById(R.id.flexboxPerfiles);
        if (flexboxPerfiles == null) {
            throw new RuntimeException("FlexboxLayout flexboxPerfiles es NULL, verifica el layout XML");
        }

        // Instancia a la clase DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        findViewById(R.id.iconoAgregar).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });

        cargarPerfiles();
    }

    /**
     * Método del ciclo de vida de la actividad que se llama cuando la actividad vuelve al primer plano.
     *
     * Se utiliza para recargar los perfiles desde la fuente de datos cada vez que la pantalla se muestra al usuario,
     * asegurando que la información esté actualizada.
     */
    @Override
    protected void onResume()
    {
        // Llama al método onResume() de la clase padre,
        // asegurando que el ciclo de vida del Activity funcione correctamente.
        super.onResume();

        // Llama a un método para recargar los perfiles o datos
        // cuando la actividad vuelve a estar en primer plano.
        cargarPerfiles();
    }

    /**
     * Metodo donde obtengo los tados de los usuario para luego mostralo en la lista de RecyclerView
     */
    private void cargarPerfiles()
    {
        if (flexboxPerfiles == null)
        {
            // Aquí loguea o ignora la llamada para evitar NPE
            return;
        }

        // Limpiamos la lista de los perfiles por si hay usuarios nuevos o se a dado de baja (eliminado) uno
        flexboxPerfiles.removeAllViews(); // Limpiamos perfiles previos

        Cursor consulta = null;
        try
        {
            // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
            consulta = dbHelper.getReadableDatabase() // Obtiene una instancia en modo solo lectura de la bbdd a traves de DatabaseHelper.
                    .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);
            // Se ejecuta una consulta SQL "raw" (cruda) que selecciona los campos id, nombre y fotoPerfil
            // de la tabla "Usuario". El segundo parámetro (null) indica que no se están utilizando argumentos con reemplazo (?).

            // Comrpobamos que hayamos obtenido al menos un dato y movemos el cursor a la primera fila.
            if (consulta != null && consulta.moveToFirst())
            {
                // Obtener índices de columna para evitar llamarlos en cada iteración
                int idIndex = consulta.getColumnIndexOrThrow("id");
                int nombreIndex = consulta.getColumnIndexOrThrow("nombre");
                int fotoIndex = consulta.getColumnIndexOrThrow("fotoPerfil");

                do
                {
                    // Con los datos que obtenemos en la consulta que hemos hecho,
                    // lo guardamos en las variables
                    int id = consulta.getInt(idIndex);
                    String nombre = consulta.getString(nombreIndex);

                    // Obtenemos la imagen de perfil si existe
                    Bitmap imagen = null;
                    if (!consulta.isNull(fotoIndex))
                    {
                        byte[] imagenBytes = consulta.getBlob(fotoIndex);
                        if (imagenBytes != null && imagenBytes.length > 0)
                        {
                            imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        }
                    }

                    // Inflar la vista del perfil
                    View perfilView = getLayoutInflater().inflate(R.layout.item_perfil, flexboxPerfiles, false);

                    ImageView fotoPerfil = perfilView.findViewById(R.id.imagenPerfil);
                    TextView nombrePerfil = perfilView.findViewById(R.id.nombrePerfil);

                    if (imagen != null)
                    {
                        fotoPerfil.setImageBitmap(imagen);
                    }
                    else
                    {
                        fotoPerfil.setImageResource(R.drawable.baseline_person_24); // Imagen por defecto
                    }

                    nombrePerfil.setText(nombre);

                    perfilView.setOnClickListener(v -> {
                        Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);
                        intent.putExtra("usuarioId", id);
                        startActivity(intent);
                    });

                    flexboxPerfiles.addView(perfilView);

                } while (consulta.moveToNext());
            }
        }
        finally
        {
            // Cerramos el cursor para evitar fugas de memoria
            if (consulta != null)
            {
                consulta.close();
            }
        }
    }

}
