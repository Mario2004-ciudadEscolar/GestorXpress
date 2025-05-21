package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;

import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.database.VerBBDDActivity;
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

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
// prueba comentario
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        revisarTareasYProgramarAlarmas();

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
        navigationView.setNavigationItemSelectedListener(item -> {
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
                // Voy a comentar esto para poner el de "Ver la BBDD"
                //startActivity(new Intent(MainActivity.this, CompartirActivity.class));

                // Lo voy a poner de momento para ver la bbdd, luego se quitara
                startActivity(new Intent(MainActivity.this, VerBBDDActivity.class));
                drawer.closeDrawers();
                return true;
            }
            /**
             * Para visualizar la cuenta del usuario
             */
           /* else if (id == R.id.nav_Cuenta)
            {
                startActivity(new Intent(MainActivity.this, CuentaActivity.class));
                drawer.closeDrawers();
                return true;
            }*/

            NavigationUI.onNavDestinationSelected(item, navController);
            drawer.closeDrawers();
            return true;
        });

        ImageButton btnFiltro = findViewById(R.id.btn_toolbar_filtro);
        btnFiltro.setOnClickListener(v -> {
            Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null) {
                List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
                if (!fragments.isEmpty() && fragments.get(0) instanceof HomeFragment) {
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

    // cuando se cree mirara en la bbdd si hay que poner alguna alarma por que solo deja 24 horas antes
    private void programarAlarmaEnReloj(Calendar fecha, String mensaje) {
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minuto = fecha.get(Calendar.MINUTE);

        Intent intent = new Intent(android.provider.AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(android.provider.AlarmClock.EXTRA_HOUR, hora);
        intent.putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minuto);
        intent.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, mensaje);
        intent.putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    private void revisarTareasYProgramarAlarmas() {
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
            int usuarioId = dbHelper.obtenerIdUsuario();

            if (usuarioId != -1) {
                List<Map<String, String>> tareas = dbHelper.getTareasFuturas(usuarioId);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                for (Map<String, String> tarea : tareas) {
                    try {
                        Calendar calInicio = Calendar.getInstance();
                        calInicio.setTime(sdf.parse(tarea.get("inicio")));

                        Calendar calFin = Calendar.getInstance();
                        calFin.setTime(sdf.parse(tarea.get("fin")));

                        programarAlarmaEnReloj(calInicio, "Inicio: " + tarea.get("titulo"));
                        programarAlarmaEnReloj(calFin, "Fin: " + tarea.get("titulo"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
