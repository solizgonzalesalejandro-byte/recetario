package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import com.google.android.material.card.MaterialCardView;

public class Menu extends AppCompatActivity {

    // Componentes de Sesión
    private LinearLayout layoutBotonesInvitado;
    private MaterialCardView cardBurbujaUsuario;
    private TextView txtInicialUsuario;
    private Button btnMenuLogin, btnMenuRegistro;

    // 🌟 COMPONENTES DE LAS TARJETAS ECOLÓGICAS AGREGARES
    private MaterialCardView cardReciclaje, cardEsterilizacion, cardAceite, cardNuevoPunto;

    private boolean isLoggedIn = false;
    private String nombreUsuario = "";
    private String correoUsuario = "";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Siempre leemos la verdad absoluta desde SharedPreferences al volver a la pantalla
        cargarEstadoSesion();
        setupSessionUI();
    }

    private void initViews() {
        // Enlace de vistas de Sesión
        layoutBotonesInvitado = findViewById(R.id.layoutBotonesInvitado);
        cardBurbujaUsuario = findViewById(R.id.cardBurbujaUsuario);
        txtInicialUsuario = findViewById(R.id.txtInicialUsuario);
        btnMenuLogin = findViewById(R.id.btnMenuLogin);
        btnMenuRegistro = findViewById(R.id.btnMenuRegistro);

        // 🌟 ENLACE DE VISTAS DE LAS TARJETAS (CORREGIDO)
        cardReciclaje = findViewById(R.id.cardReciclaje);
        cardEsterilizacion = findViewById(R.id.cardEsterilizacion);
        cardAceite = findViewById(R.id.cardAceite);
        cardNuevoPunto = findViewById(R.id.cardNuevoPunto);
    }

    private void cargarEstadoSesion() {
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        nombreUsuario = sharedPreferences.getString("nombreUsuario", "");
        correoUsuario = sharedPreferences.getString("correoUsuario", "");
    }

    private void setupSessionUI() {
        if (isLoggedIn && nombreUsuario != null && !nombreUsuario.isEmpty()) {
            layoutBotonesInvitado.setVisibility(View.GONE);
            cardBurbujaUsuario.setVisibility(View.VISIBLE);
        } else {
            layoutBotonesInvitado.setVisibility(View.VISIBLE);
            cardBurbujaUsuario.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Eventos de sesión
        btnMenuLogin.setOnClickListener(v -> {
            startActivity(new Intent(Menu.this, LoginActivity.class));
        });

        btnMenuRegistro.setOnClickListener(v -> {
            startActivity(new Intent(Menu.this, RegistroActivity.class));
        });

        cardBurbujaUsuario.setOnClickListener(this::mostrarMenuDesplegable);

        // 🌟 EVENTOS DE REDIRECCIÓN AL MAPA CON CATEGORÍAS (CORREGIDO)
        cardReciclaje.setOnClickListener(v -> irAlMapa("Reciclaje"));

        cardEsterilizacion.setOnClickListener(v -> irAlMapa("Esterilizacion"));

        cardAceite.setOnClickListener(v -> irAlMapa("Aceite"));

        // Control para registrar nuevo punto ecológico
        // Control para registrar nuevo punto ecológico (Lógica Sincronizada)
        cardNuevoPunto.setOnClickListener(v -> {
            if (isLoggedIn) {
                // Redirecciona al mapa enviando la señal de que va a registrar un punto
                Intent intent = new Intent(Menu.this, MapActivity.class);
                intent.putExtra("modo_registro", true);
                startActivity(intent);
            } else {
                // Si es un visitante sin cuenta
                Toast.makeText(this, "¡Debes iniciar sesión para registrar un punto!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Menu.this, LoginActivity.class));
            }
        });
    }

    // 🌟 MÉTODO AUXILIAR PARA ENVIAR LA CATEGORÍA A MAPACTIVITY
    private void irAlMapa(String categoria) {
        Intent intent = new Intent(Menu.this, MapActivity.class);
        intent.putExtra("categoria", categoria);
        startActivity(intent);
    }

    private void mostrarMenuDesplegable(View view) {
        PopupMenu popup = new PopupMenu(Menu.this, view);

        popup.getMenu().add(0, 1, 0, "👤: " + nombreUsuario).setEnabled(false);
        popup.getMenu().add(0, 2, 1, "✉️: " + correoUsuario).setEnabled(false);
        popup.getMenu().add(0, 3, 2, "⚙️ Opciones futuras...");
        popup.getMenu().add(0, 4, 3, "🚪 Cerrar Sesión");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 3:
                    Toast.makeText(this, "Próximamente disponible", Toast.LENGTH_SHORT).show();
                    return true;
                case 4:
                    cerrarSesionEfectivo();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void cerrarSesionEfectivo() {
        isLoggedIn = false;
        nombreUsuario = "";
        correoUsuario = "";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        setupSessionUI();
        Toast.makeText(this, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show();
    }
}