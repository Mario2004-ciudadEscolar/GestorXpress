package com.example.gestorxpress.ui.GestionPerfiles;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorxpress.R;
import com.example.gestorxpress.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Autor: Alfonso Chenche y Mario Herrero
 * Verión: 1.0
 */
public class SelectorPerfilActivity extends AppCompatActivity
{

    // Lista de elementos (Donde va ir la lista de todos los usuarios)
    private RecyclerView recyclerView;

    // Clase DatabaseHelper (Donde realizamos todas las interacciones con la bbdd SLQite)
    private DatabaseHelper dbHelper;

    //Clase PerfilAdapterSinClase (es un adaptador personalizado para un RecyclerView
    // que muestra una lista de perfiles (Usuarios que hay en la bbdd))
    private PerfilAdapterSinClase perfilAdapter;

    // Lista donde obtenemos y mostramos los datos de los perfiles (usuarios)
    private List<HashMap<String, Object>> listaPerfiles;

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
        setContentView(R.layout.activity_selector_perfil); // Instancias al xml

        // RecyclerView donde se muestran los perfiles
        recyclerView = findViewById(R.id.recyclerViewPerfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Instancia a la clase DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        //Inicializamos la lista
        listaPerfiles = new ArrayList<>();

        // Se crea una instancia a la clase PerfilAdapterSinClase, ya este adaptador obtiene una lista
        // de perfiles y una función que se llama cuando le haces click en el perfil.
        perfilAdapter = new PerfilAdapterSinClase(listaPerfiles, perfil ->
        {
            // Obtiene el id del perfil que ha seleccionado
            // 'perfil' es un Map (clave-valor), por eso se usa get("id").
            int id = (int) perfil.get("id");

            // Navegamos al Activity EditarBorrarCuenta, cuando seleccionamos el perfil
            Intent intent = new Intent(this, LoginSoloContrasenaActivity.class);

            // Mandamos como Id del usuario como dato extra al Intent (EditarBorrarCuenta)
            intent.putExtra("usuarioId", id);

            // Iniciamos el MainActivity
            startActivity(intent);
        });

        findViewById(R.id.iconoAgregar).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });


        // Llamada a un metodo donde centramos el RecyclerView con la listas de los usuarios
        centrarItemsListaUsuario(recyclerView, perfilAdapter);

        // Llamada al metodo donde cargamos los perfiles al xml (Los datos que se ve en la aplicación)
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
            do
            {
                try
                {
                    // Con los datos que obtenemos en la consulta que hemos hecho,
                    // lo guardamos en las variables
                    int id = consulta.getInt(consulta.getColumnIndexOrThrow("id"));
                    String nombre = consulta.getString(consulta.getColumnIndexOrThrow("nombre"));

                    Bitmap imagen = null;
                    int fotoIndex = consulta.getColumnIndexOrThrow("fotoPerfil");
                    if (!consulta.isNull(fotoIndex)) {
                        byte[] imagenBytes = consulta.getBlob(fotoIndex);
                        if (imagenBytes != null && imagenBytes.length > 0) {
                            imagen = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                        }
                    }

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombre);
                    map.put("foto", imagen);
                    listaPerfiles.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (consulta.moveToNext());

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

}
