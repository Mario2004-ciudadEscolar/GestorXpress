package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.gestorxpress.database.DatabaseHelper;
import com.example.gestorxpress.database.VerBBDDActivity;
import com.example.gestorxpress.ui.Cuenta.CuentaActivity;
import com.example.gestorxpress.ui.slideshow.SlideshowFragment;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import com.example.gestorxpress.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
// prueba comentario
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * La parte que esta comentado, son cosas que tenemos que cambiar y mejorar.
         */
        // Recuperar el correo del intent
        //String correo = getIntent().getStringExtra("correo");
        //Log.d("DEBUG_CORREO", "Correo recibido en MainActivity: " + correo);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        //int idUsuario = dbHelper.obtenerIdUsuarioPorCorreo(correo);
        //Log.d("DEBUG_ID", "ID de usuario obtenido: " + idUsuario);

        // Guardar el idUsuario en un Bundle para pasarlo a los fragmentos
        /*Bundle bundle = new Bundle();
        bundle.putInt("idUsuario", idUsuario);

        // Establecer el bundle como argumento inicial para los fragmentos que lo necesiten
        getSupportFragmentManager().setFragmentResult("datosUsuario", bundle);*/


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setSupportActionBar(binding.appBarMain.toolbar); <-- He cambiado esto

        setSupportActionBar(findViewById(R.id.toolbar)); // <-- Por esto

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_Crear_Tarea, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        /*binding.appBarMain.fab.setOnClickListener(view -> {
            navController.navigate(R.id.nav_gallery); //  crear tarea
        });*/ // <-- He cambiado esto.

        /*findViewById(R.id.fab).setOnClickListener(view -> {
            Bundle args = new Bundle(); // usa un nombre diferente evita errores
            args.putInt("idUsuario", idUsuario);
            navController.navigate(R.id.nav_Crear_Tarea, args);
        });*/


//v1
//        findViewById(R.id.fab).setOnClickListener(view -> {
//            navController.navigate(R.id.nav_Crear_Tarea);
//        });


        // Acciones personalizadas de menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout)
            {
                // Llamamos al método cerrarSesion para cerrar la sesión
                boolean exito = dbHelper.cerrarSesion();

                // Si la sesión se cerró correctamente, redirigimos al login
                if (exito)
                {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
                return true;
            }
            else if (id == R.id.nav_Cuenta)
            {
                startActivity(new Intent(MainActivity.this, CuentaActivity.class));
                return true;
            }
            else if (id == R.id.nav_slideshow)
            {
                startActivity(new Intent(MainActivity.this, SlideshowFragment.class));
                return true;
            }

            NavigationUI.onNavDestinationSelected(item, navController);
            drawer.closeDrawers();
            return true;
        });
    }


    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
