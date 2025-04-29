package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.gestorxpress.databinding.ActivityMainBinding;
import com.example.gestorxpress.ui.Tarea.CrearTareaFragment;
import com.example.gestorxpress.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recuperar el idUsuario del Intent
        Intent intent = getIntent();
        idUsuario = intent.getIntExtra("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_Crear_Tarea, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Cargar HomeFragment por defecto con idUsuario
        Bundle bundle = new Bundle();
        bundle.putInt("idUsuario", idUsuario);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, homeFragment)
                .commit();

        // Menú personalizado
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                finish();
                return true;
            } else if (id == R.id.nav_compartir) {
                startActivity(new Intent(MainActivity.this, CompartirActivity.class));
                return true;
            }

            NavigationUI.onNavDestinationSelected(item, navController);
            drawer.closeDrawers();
            return true;
        });

        // Botón flotante para abrir CrearTareaFragment
        FloatingActionButton fab = binding.appBarMain.fab;

        fab.setOnClickListener(view -> {
            CrearTareaFragment crearTareaFragment = new CrearTareaFragment();
            Bundle args = new Bundle();
            args.putInt("idUsuario", idUsuario);
            crearTareaFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, crearTareaFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
