package com.example.gestorxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editNombre, editPassword;
    private Button btnLogin;

    // Credenciales hardcoded (puedes cambiarlas)
    private final String USUARIO_VALIDO = "admin";
    private final String CONTRASEÑA_VALIDA = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editNombre = findViewById(R.id.editNombre);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            String contraseña = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(contraseña)) {
                Toast.makeText(this, "Por favor, introduce usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nombre.equals(USUARIO_VALIDO) && contraseña.equals(CONTRASEÑA_VALIDA)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("usuario", nombre);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
