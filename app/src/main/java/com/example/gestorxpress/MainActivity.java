package com.example.gestorxpress;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gestorxpress.ui.GestionPerfiles.SelectorPerfilActivity;
import android.util.Log;
import android.widget.ImageButton;

import com.example.gestorxpress.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private AppBarConfiguration mAppBarConfiguration;

    // Instancia a la clase DatabaseHelper
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Crear canal de notificaciones (solo una vez)
        crearCanalDeNotificaciones();

        // Metodo donde revisamos las tareas creadas y programamos las alarmas que estan por acercar
        revisarTareasYProgramarAlarmas();

        // Instancia a la bbdd
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(findViewById(R.id.toolbar));

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_Crear_Tarea, R.id.nav_slideshow, R.id.nav_Cuenta)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Acciones personalizadas de menú
        navigationView.setNavigationItemSelectedListener(item ->
        {
            int id = item.getItemId();

            /**
             * Llamada para cerrar sesión
             */
            if (id == R.id.nav_logout)
            {
                // Llamamos al método cerrarSesion para cerrar la sesión
                boolean exito = dbHelper.cerrarSesion();

                // Si la sesión se cerró correctamente, redirigimos al login
                if (exito)
                {
                    Intent intent = new Intent(MainActivity.this, SelectorPerfilActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finaliza la actividad actual
                }
                else
                {
                    // Si hubo un error cerrando la sesión, mostramos un mensaje
                    Log.e("MainActivity", "Error al cerrar sesión.");
                }
                return true;

            }
            else if (id == R.id.nav_compartir)
            {
                // Create share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Descarga GestorXpress");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Descarga GestorXpress desde GitHub!\nhttps://github.com/Mario2004-ciudadEscolar/ciudadEscolar_GestorXpress");
                
                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Compartir enlace de descarga"));
                drawer.closeDrawers();
                return true;
            }
            NavigationUI.onNavDestinationSelected(item, navController);
            drawer.closeDrawers();
            return true;
        });

        ImageButton btnFiltro = findViewById(R.id.btn_toolbar_filtro);
        btnFiltro.setOnClickListener(v ->
        {
            Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null)
            {
                List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
                if (!fragments.isEmpty() && fragments.get(0) instanceof HomeFragment)
                {
                    ((HomeFragment) fragments.get(0)).mostrarMenuFiltro(v);
                }
            }
        });

    }


    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     *
     * @param fecha La hora que debe programarse el reloz
     * @param mensaje Titulo de la alarma
     */
    private void programarAlarmaEnReloj(Calendar fecha, String mensaje)
    {
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minuto = fecha.get(Calendar.MINUTE);

        Intent intent = new Intent(android.provider.AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(android.provider.AlarmClock.EXTRA_HOUR, hora);
        intent.putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minuto);
        intent.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, mensaje);
        intent.putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true);

        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    /**
     * Este método revisa las tareas futuras del usuario actual en segundo plano
     * y programa alarmas (notificaciones) para el inicio y fin de cada una.
     *.
     * Utiliza un hilo secundario para evitar bloquear la interfaz de usuario.
     * Las fechas de inicio y fin lo obtenemos mediante la base de datos y
     * lo usamos para programar recordatorios (alarmas) con mensajes personalizados.
     */
    private void revisarTareasYProgramarAlarmas()
    {
        // Inicia un nuevo hilo para ejecutar la operación sin bloquear el interfaz
        new Thread(() ->
        {
            // Instancia a la bbdd
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

            // Obtenemos el ID del usuario actualmente logueado
            int usuarioId = dbHelper.obtenerIdUsuario();

            // Si se obtuvo un ID válido (es decir, hay sesión activa)
            if (usuarioId != -1)
            {
                // Obtiene una lista de tareas futuras para ese usuario
                List<Map<String, String>> tareas = dbHelper.getTareasFuturas(usuarioId);

                // Formato con el que están guardadas las fechas en la base de datos
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                // Recorremos cada tarea creada
                for (Map<String, String> tarea : tareas)
                {
                    try
                    {
                        // Crea una instancia del calendario para la fecha de inicio
                        Calendar calInicio = Calendar.getInstance();
                        // Parsea y asigna la fecha de inicio de la tarea
                        calInicio.setTime(sdf.parse(tarea.get("inicio")));

                        // Crea una instancia del calendario para la fecha de fin
                        Calendar calFin = Calendar.getInstance();
                        // Parsea y asigna la fecha de fin de la tarea
                        calFin.setTime(sdf.parse(tarea.get("fin")));

                        // Programa una alarma para la fecha de inicio de la tarea
                        programarAlarmaEnReloj(calInicio, "Inicio: " + tarea.get("titulo"));
                        // Programa una alarma para la fecha de fin de la tarea
                        programarAlarmaEnReloj(calFin, "Fin: " + tarea.get("titulo"));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start(); // Ejecuta el hilo
    }

    private void crearCanalDeNotificaciones()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence nombre = "Canal de tareas";
            String descripcion = "Notificaciones de recordatorios de tareas";
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel canal = new NotificationChannel("canal_tareas", nombre, importancia);
            canal.setDescription(descripcion);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }


}
