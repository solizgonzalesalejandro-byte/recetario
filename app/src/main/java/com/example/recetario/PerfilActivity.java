package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton; // 🌟 Importación requerida
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    // Contenedores de Estado
    private LinearLayout layoutUsuarioLogueado, layoutUsuarioInvitado;

    // Componentes de datos del usuario
    private TextView profileName, profileEmail, txtCantPuntos;
    private Button btnCerrarSesion, btnIrLogin, btnIrRegistro;
    private ImageButton btnAtrasPerfil; // 🌟 Declaración del botón atrás

    // Contenedores dinámicos para listas
    private LinearLayout containerMisPuntos, containerFavoritos, containerResenas;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_perfil);

        sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);

        initViews();
        evaluarEstadoYConfigurarUI();
    }

    private void initViews() {
        // Estructuras principales de control visual
        layoutUsuarioLogueado = findViewById(R.id.layoutUsuarioLogueado);
        layoutUsuarioInvitado = findViewById(R.id.layoutUsuarioInvitado);

        // Campos de información
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        txtCantPuntos = findViewById(R.id.txtCantPuntos);

        // Listas dinámicas
        containerMisPuntos = findViewById(R.id.containerMisPuntos);
        containerFavoritos = findViewById(R.id.containerFavoritos);
        containerResenas = findViewById(R.id.containerResenas);

        // Botones de acción
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // 🌟 Enlace y programación de retroceso directo al mapa
        btnAtrasPerfil = findViewById(R.id.btnAtrasPerfil);
        if (btnAtrasPerfil != null) {
            btnAtrasPerfil.setOnClickListener(v -> finish());
        }
    }

    private void evaluarEstadoYConfigurarUI() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String usuarioId = sharedPreferences.getString("usuarioId", "ID_ANONIMO");

        if (usuarioId.equals("ID_ANONIMO") || !isLoggedIn) {
            // MODO INVITADO
            layoutUsuarioLogueado.setVisibility(View.GONE);
            layoutUsuarioInvitado.setVisibility(View.VISIBLE);

            btnIrLogin.setOnClickListener(v -> {
                startActivity(new Intent(PerfilActivity.this, LoginActivity.class));
            });

            btnIrRegistro.setOnClickListener(v -> {
                startActivity(new Intent(PerfilActivity.this, RegistroActivity.class));
            });

        } else {
            // MODO LOGUEADO
            layoutUsuarioLogueado.setVisibility(View.VISIBLE);
            layoutUsuarioInvitado.setVisibility(View.GONE);

            String nombre = sharedPreferences.getString("nombreUsuario", "Eco Héroe");
            String correo = sharedPreferences.getString("correoUsuario", "correo@cochaeco.com");

            profileName.setText(nombre);
            profileEmail.setText(correo);

            txtCantPuntos.setText("0");

            btnCerrarSesion.setOnClickListener(v -> efectuarCierreSesion());
        }
    }

    private void efectuarCierreSesion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Sesión Cerrada con éxito", Toast.LENGTH_SHORT).show();

        // Refrescamos la misma pantalla al instante pasando a modo invitado sin salir abruptamente
        evaluarEstadoYConfigurarUI();
    }
}