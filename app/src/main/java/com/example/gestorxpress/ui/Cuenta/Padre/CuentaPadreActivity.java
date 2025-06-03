package com.example.gestorxpress.ui.Cuenta.Padre;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorxpress.MainActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Cuenta.AvisoLegal.AvisosLegales;
import com.example.gestorxpress.ui.Cuenta.SobreGestoXpress.SobreNosotros;
import com.example.gestorxpress.ui.Cuenta.Suscripcion.VisualSuscripcion;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;
import com.google.android.flexbox.FlexboxLayout;

import java.util.HashMap;

/**
 * Autores: Alfonso Chenche y Mario Herrero
 * Versión: 1.0
 */
public class CuentaPadreActivity extends AppCompatActivity
{

    // Layout flexible para mostrar perfiles horizontalmente
    private FlexboxLayout flexboxPerfiles;

    // Clase DatabaseHelper (Donde realizamos todas las interacciones con la bbdd SQLite)
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta_padre); // Instancia al xml

        // Configuración de la barra de herramientas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Habilitamos el botón de retroceso en el toolbar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Vuelve a la pagina principal cuando le damos a la flecha
        toolbar.setNavigationOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Opcional
        });

        // El FlexboxLayout es donde se añade dinámicamente las vistas de cada perfil
        // (usuario) de forma flexible y adaptable al tamaño de la pantalla.
        flexboxPerfiles = findViewById(R.id.flexboxPerfiles);

        dbHelper = new DatabaseHelper(this); // Instancia de la base de datos

        // Llamada al metodo donde cargamos los perfiles al xml (Los datos que se ve en la aplicación)
        cargarPerfiles();

        // Lamada al metodo donde al seleccionar un button o EditText me lleva a otro Activity (Otra pagina)
        llamadasActivity();
    }

    /**
     * Método del ciclo de vida de la actividad que se llama cuando la actividad vuelve al primer plano.
     *.
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
     * Metodo donde obtengo los tados de los usuario para luego mostralo en la lista de FlexboxLayout
     */
    private void cargarPerfiles()
    {
        // Limpiamos la vista de perfiles por si hay usuarios nuevos o se a dado de baja (eliminado) uno
        flexboxPerfiles.removeAllViews();

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
                // Variable para comprobar el primero que esta en la lista, que es el padre (administrador)
                boolean primerRegistroSaltado = false;

                // Obtener índices de columna fuera del bucle para optimizar
                int idIndex = consulta.getColumnIndexOrThrow("id");
                int nombreIndex = consulta.getColumnIndexOrThrow("nombre");
                int fotoIndex = consulta.getColumnIndexOrThrow("fotoPerfil");

                do
                {
                    // Como siempre pasamos por el primer usuario y la variable es false
                    // Pasa a ser True ya que el primero es el padre.
                    if (!primerRegistroSaltado)
                    {
                        // Saltamos el primer perfil (primer registro)
                        primerRegistroSaltado = true;
                        continue;  // va al siguiente registro sin añadir nada
                    }

                    try
                    {
                        // Con los datos que obtenemos en la consulta que hemos hecho,
                        // lo guardamos en las variables
                        int id = consulta.getInt(idIndex);
                        String nombre = consulta.getString(nombreIndex);

                        Bitmap imagen = null;
                        if (!consulta.isNull(fotoIndex))
                        {
                            byte[] imagenBytes = consulta.getBlob(fotoIndex);
                            if (imagenBytes != null && imagenBytes.length > 0)
                            {
                                imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                            }
                        }

                        agregarPerfilVista(id, nombre, imagen);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                while (consulta.moveToNext());
            }
        }
        finally
        {
            if (consulta != null)
            {
                consulta.close();
            }
        }
    }

    /**
     * Método para agregar cada perfil como una vista dentro del FlexboxLayout.
     * Se infla un layout personalizado para cada perfil y se configura con sus datos.
     *
     * @param id Identificador del usuario.
     * @param nombre Nombre del usuario.
     * @param foto Foto de perfil como Bitmap (puede ser null).
     */
    private void agregarPerfilVista(int id, String nombre, Bitmap foto)
    {
        // Inflamos un layout personalizado para el perfil
        View perfilView = LayoutInflater.from(this).inflate(R.layout.item_perfil, flexboxPerfiles, false);

        // Obtenemos referencias a los elementos del layout
        ImageView imagenPerfil = perfilView.findViewById(R.id.imagenPerfil);
        TextView nombrePerfil = perfilView.findViewById(R.id.nombrePerfil);

        // Seteamos el nombre
        nombrePerfil.setText(nombre);

        // Si hay imagen, la asignamos, si no, asignamos imagen por defecto
        if (foto != null)
        {
            imagenPerfil.setImageBitmap(foto);
        }
        else
        {
            imagenPerfil.setImageResource(R.drawable.baseline_person_24); // Imagen por defecto si no hay foto
        }

        // Configuramos el evento click para navegar a EditarBorrarCuenta con el id de usuario
        perfilView.setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, EditarBorrarCuenta.class);
            intent.putExtra("usuarioId", id);
            startActivity(intent);
        });

        // Añadimos la vista al FlexboxLayout
        flexboxPerfiles.addView(perfilView);
    }

    /**
     * Este metodo se utiliza para llamar a los activity (otras paginas) cuando seleccionamos o
     * clicamos sobre un boton o un Texto
     */
    private void llamadasActivity()
    {
        findViewById(R.id.btncuentaPadre).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, EditarBorrarCuenta.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSuscripcion).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, VisualSuscripcion.class);
            startActivity(intent);
        });

        findViewById(R.id.btnAvisoLegalPriv).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, AvisosLegales.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSobreNosotros).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, SobreNosotros.class);
            startActivity(intent);
        });

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, SelectorPerfilActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Maneja el evento cuando el usuario pulsa la flecha de "volver atrás" en la barra de herramientas (ActionBar).
     *.
     * Este método lanza el `MainActivity` y finaliza la actividad actual, simulando una navegación hacia atrás.
     *
     * @return true para indicar que se ha manejado el evento de navegación manualmente.
     */
    @Override
    public boolean onSupportNavigateUp()
    {
        // Cuando se pulse la flecha, vuelve al MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        // Inicia el MainActivity
        startActivity(intent);
        finish(); // Para cerrar esta activity
        return true;
    }

}
