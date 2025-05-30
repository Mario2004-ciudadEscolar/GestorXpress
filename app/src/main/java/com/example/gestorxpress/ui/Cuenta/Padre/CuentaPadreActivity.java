package com.example.gestorxpress.ui.Cuenta.Padre;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.MainActivity;
import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.Cuenta.SobreGestoXpress.SobreNosotros;
import com.example.gestorxpress.ui.Cuenta.Suscripcion.VisualSuscripcion;
import com.example.gestorxpress.ui.GestionPerfiles.PerfilAdapterSinClase;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 */
public class CuentaPadreActivity extends AppCompatActivity
{

    private RecyclerView recyclerView; // Lista de elementos (Donde va ir la lista de todos los usuarios)

    // Clase DatabaseHelper (Donde realizamos todas las interacciones con la bbdd SLQite)
    private DatabaseHelper dbHelper;

    //Clase PerfilAdapterSinClase (es un adaptador personalizado para un RecyclerView
    // que muestra una lista de perfiles (Usuarios que hay en la bbdd))
    private PerfilAdapterSinClase perfilAdapter;

    // Lista donde obtenemos y mostramos los datos de los perfiles (usuarios)
    private List<HashMap<String, Object>> listaPerfiles;

    private Button btnGestionar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta_padre); // Instancia al xml

        btnGestionar = findViewById(R.id.btnGestionar);

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

        // RecyclerView donde se muestran los perfiles
        recyclerView = findViewById(R.id.recyclerViewPerfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dbHelper = new DatabaseHelper(this); // Instancia de la base de datos

        listaPerfiles = new ArrayList<>(); // Inicializamos la lista

        // Se crea una instancia a la clase PerfilAdapterSinClase, ya este adaptador obtiene una lista
        // de perfiles y una función que se llama cuando le haces click en el perfil.
        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil ->
        {
            // Obtiene el id del perfil que ha seleccionado
            // 'perfil' es un Map (clave-valor), por eso se usa get("id").
            int id = (int) perfil.get("id");

            // Navegamos al Activity EditarBorrarCuenta, cuando seleccionamos el perfil
            Intent intent = new Intent(this, EditarBorrarCuenta.class);

            // Mandamos como Id del usuario como dato extra al Intent (EditarBorrarCuenta)
            intent.putExtra("usuarioId", id);

            // Iniciamos el Intent
            startActivity(intent);
        });

        // Llamada a un metodo donde centramos el RecyclerView con la listas de los usuarios
        centrarItemsListaUsuario(recyclerView, perfilAdapter);

        // Llamada al metodo donde cargamos los perfiles al xml (Los datos que se ve en la aplicación)
        cargarPerfiles();

        // Lamada al metodo donde al seleccionar un button o EditText me lleva a otro Activity (Otra pagina)
        llamadasActivity();
    }

    /**
     * Este metodo se utiliza para llamar a los activity (otras paginas) cuando seleccionamos o
     * clicamos sobre un boton o un Texto
     */
    private void llamadasActivity()
    {
       /* btnGestionar.setOnClickListener(v -> {
            Intent intent = new Intent(CuentaPadreActivity.this, GestionCuentaHijos.class);
            startActivity(intent);
        });*/

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

       /* findViewById(R.id.btnAvisoLegalPriv).setOnClickListener(v ->
        {
            Intent intent = new Intent(CuentaPadreActivity.this, AvisoLegalActivity.class);
            startActivity(intent);
        });*/

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
     * Metodo donde obtengo los tados de los usuario para luego mostralo en la lista de RecyclerView
     */
    private void cargarPerfiles()
    {
        // Limpiamos la lista de los perfiles por si hay usuarios nuevos o se a dado de baja (eliminado) uno
        listaPerfiles.clear();

        // El resultado se guarda en el 'Cursor', que permite recorrer los resultados fila por fila.
        Cursor consulta = dbHelper.getReadableDatabase() // Obtiene una instancia en modo solo lectura de la bbdd a traves de DatabaseHelper.
                .rawQuery("SELECT id, nombre, fotoPerfil FROM Usuario", null);
        // Se ejecuta una consulta SQL "raw" (cruda) que selecciona los campos id, nombre y fotoPerfil
        // de la tabla "Usuario". El segundo parámetro (null) indica que no se están utilizando argumentos con reemplazo (?).

        // Comrpobamos que hayamos obtenido al menos un dato y movemos el cursor a la primera fila.
        if (consulta != null && consulta.moveToFirst())
        {
            // Variable para comprobar el primero que esta en la lista, que es el padre (administrador)
            boolean primerRegistroSaltado = false;

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
                    int id = consulta.getInt(consulta.getColumnIndexOrThrow("id"));
                    String nombre = consulta.getString(consulta.getColumnIndexOrThrow("nombre"));

                    Bitmap imagen = null;
                    int fotoIndex = consulta.getColumnIndexOrThrow("fotoPerfil");
                    if (!consulta.isNull(fotoIndex))
                    {
                        byte[] imagenBytes = consulta.getBlob(fotoIndex);
                        if (imagenBytes != null && imagenBytes.length > 0)
                        {
                            imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        }
                    }

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombre);
                    map.put("foto", imagen);
                    listaPerfiles.add(map);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            while (consulta.moveToNext());

            consulta.close();
        }

        perfilAdapter.notifyDataSetChanged();
    }


    /**
     * En este metodo centramos los ítems de una lista horizontal de usuarios en un RecyclerView (Que esta en el xml).
     *.
     *  Este método establece el adaptador del RecyclerView, calcula si los ítems caben dentro del ancho
     *  del RecyclerView y ajusta el padding lateral para centrarlos si es necesario. Además, aplica un
     *  espaciado (decoración) entre los ítems, excepto para el último.
     *  .
     * @param recyclerView El RecyclerView donde se mostrarán los ítems.
     * @param adapter El adaptador que proporciona los ítems al RecyclerView.
     */
    private void centrarItemsListaUsuario(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        // Establecemos el adaptador del RecyclerView
        recyclerView.setAdapter(adapter);

        // Aquí se define el ancho del Item en dp, sumando margenes internos
        final int itemWidthDp = 64 + 6 + 6; // 64dp del ítem + 6dp de margen izquierdo + 6dp de margen derecho

        // Aquí se convierte el ancho del ítem y el margen de dp a píxelex
        final int itemWidthPx = dpToPx(itemWidthDp);
        final int itemMarginPx = dpToPx(6); // Margen lateral entre ítems (usuarios)

        // Se ejecuta esta lógica después de que el RecyclerView se haya dibujado en pantalla
        recyclerView.post(() -> {
            // Obtenemos la cantidad de ítems
            int itemCount = adapter.getItemCount();

            // Obtiene el ancho disponible del RecylrerView
            int recyclerViewWidth = recyclerView.getWidth();

            // Calculamos el ancho total que ocuparán los ítems (usuarios)
            int totalItemsWidth = itemWidthPx * itemCount;

            // Si los ítems (usuarios) ocupan menos que el ancho del RecyclerView, y centrarlos con padding lateral.
            if (totalItemsWidth < recyclerViewWidth)
            {
                int sidePadding = (recyclerViewWidth - totalItemsWidth) / 2;
                recyclerView.setPadding(sidePadding, recyclerView.getPaddingTop(), sidePadding, recyclerView.getPaddingBottom());
            }
            else
            {
                // Si no caben, simplemente aplicamos el margen estándar a los lados.
                recyclerView.setPadding(itemMarginPx, recyclerView.getPaddingTop(), itemMarginPx, recyclerView.getPaddingBottom());
            }
        });

        // Agregamos un decorador para añadir margen derecho a todos los ítems, expeto el último
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view); // Posición del ítems (usuarios)
                int itemCount = state.getItemCount(); // Numero total de ítems (usuarios)

                // Si no es el último items (usuario), se le añade un margen derecho
                if (position != itemCount - 1)
                {
                    outRect.right = itemMarginPx;
                }
            }
        });
    }


    /**
     * En este metodo convertimos un valor en dp (density-independent pixels) a píxeles (px) reales según
     * la densidad de pantalla del dispositivo.
     *.
     * Esto es útil para mantener dimensiones coherentes en diferentes tamaños y resoluciones de pantalla.
     *
     * @param dp Valor en dp que se desea convertir a píxeles.
     * @return Valor convertido a píxeles (px).
     */
    private int dpToPx(int dp)
    {
        // Usa la función applyDimension para convertir dp a px basado en la densidad de pantalla actual
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,        // Indica que la unidad a convertir es dp (DIP)
                dp,                                 // Valor en dp que se quiere convertir
                getResources().getDisplayMetrics()  // Obtiene las métricas de pantalla (densidad, tamaño, etc.)
        );
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
        finish(); // Opcional, para cerrar esta activity
        return true;
    }

}
