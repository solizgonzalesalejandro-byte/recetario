package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.List; // ESTO SOLUCIONA EL ERROR "cannot find symbol List"

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private LinearLayout layoutUsuarioLogueado, layoutUsuarioInvitado;
    private TextView profileName, profileEmail, txtCantPuntos;
    private Button btnCerrarSesion, btnIrLogin, btnIrRegistro;
    private ImageButton btnAtrasPerfil;
    private LinearLayout containerMisPuntos; // Contenedor principal de puntos

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
        layoutUsuarioLogueado = findViewById(R.id.layoutUsuarioLogueado);
        layoutUsuarioInvitado = findViewById(R.id.layoutUsuarioInvitado);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        txtCantPuntos = findViewById(R.id.txtCantPuntos);
        containerMisPuntos = findViewById(R.id.containerMisPuntos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        btnAtrasPerfil = findViewById(R.id.btnAtrasPerfil);

        if (btnAtrasPerfil != null) {
            btnAtrasPerfil.setOnClickListener(v -> finish());
        }
    }

    private void evaluarEstadoYConfigurarUI() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String usuarioId = sharedPreferences.getString("usuarioId", "ID_ANONIMO");

        if (!isLoggedIn || usuarioId.equals("ID_ANONIMO")) {
            layoutUsuarioLogueado.setVisibility(View.GONE);
            layoutUsuarioInvitado.setVisibility(View.VISIBLE);

            btnIrLogin.setOnClickListener(v -> startActivity(new Intent(PerfilActivity.this, LoginActivity.class)));
            btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(PerfilActivity.this, RegistroActivity.class)));
        } else {
            layoutUsuarioLogueado.setVisibility(View.VISIBLE);
            layoutUsuarioInvitado.setVisibility(View.GONE);

            profileName.setText(sharedPreferences.getString("nombreUsuario", "Eco Héroe"));
            profileEmail.setText(sharedPreferences.getString("correoUsuario", "usuario@correo.com"));

            // Cargar los puntos del usuario mediante la API
            cargarPuntosUsuario(usuarioId);

            btnCerrarSesion.setOnClickListener(v -> efectuarCierreSesion());
        }
    }

    private void cargarPuntosUsuario(String usuarioId) {
        PuntoApiService apiService = RetrofitClient.getPuntoApiService();

        apiService.buscaPuntosUser(usuarioId).enqueue(new Callback<List<Punto.PuntoResponseDto>>() {
            @Override
            public void onResponse(Call<List<Punto.PuntoResponseDto>> call, Response<List<Punto.PuntoResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Punto.PuntoResponseDto> listaPuntos = response.body();
                    txtCantPuntos.setText(String.valueOf(listaPuntos.size()));

                    containerMisPuntos.removeAllViews(); // Limpiar antes de agregar
                    for (Punto.PuntoResponseDto punto : listaPuntos) {
                        agregarVistaPunto(punto);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Punto.PuntoResponseDto>> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarVistaPunto(Punto.PuntoResponseDto punto) {
        // Creamos una tarjeta visual para cada punto
        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(4f);
        card.setRadius(16f);
        card.setUseCompatPadding(true);

        TextView tv = new TextView(this);
        tv.setText("📍 " + punto.getNombre() + "\nTipo: " + punto.getTipo());
        tv.setPadding(32, 32, 32, 32);
        tv.setTextColor(Color.parseColor("#2D3436"));

        card.addView(tv);
        containerMisPuntos.addView(card);
    }

    private void efectuarCierreSesion() {
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        evaluarEstadoYConfigurarUI();
    }
}