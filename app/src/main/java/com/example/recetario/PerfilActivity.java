package com.example.recetario;

import android.app.Activity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson; // Agregado el import faltante de Gson

import java.util.List;

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

    // 🚀 Lanzador de ActivityResult corregido para tu diseño dinámico
    // --- REEMPLAZA ESTE BLOQUE EN PerfilActivity.java ---

    private final ActivityResultLauncher<Intent> detalleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Caso A: Se eliminó un punto de manera física
                    if (result.getData() != null && result.getData().hasExtra("PUNTO_ELIMINADO_ID")) {
                        String idEliminado = result.getData().getStringExtra("PUNTO_ELIMINADO_ID");
                        if (idEliminado != null) {
                            eliminarPuntoDeLaListaLocal(idEliminado);
                        }
                    }
                    // Caso B: Se regresó de una edición exitosa (se necesita refrescar la UI)
                    else {
                        String usuarioId = sharedPreferences.getString("usuarioId", "ID_ANONIMO");
                        if (!usuarioId.equals("ID_ANONIMO")) {
                            // Sincroniza y vuelve a jalar los elementos actualizados de la API
                            cargarPuntosUsuario(usuarioId);
                        }
                    }
                }
            }
    );

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

        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(4f);
        card.setRadius(16f);
        card.setUseCompatPadding(true);

        // Dejamos que la tarjeta sea clickeable para efectos visuales (efecto ripple de Material)
        card.setClickable(true);
        card.setFocusable(true);

        // Al principio de agregarVistaPunto, añade este seguro:
        if (punto.getId() == null || punto.getId().isEmpty()) {
            punto.setId("TEMP_ID_" + System.currentTimeMillis()); // Le da un ID único provisional si viene vacío
        }
        card.setTag(punto.getId());

        TextView tv = new TextView(this);
        tv.setText("📍 " + punto.getNombre() + "\nTipo: " + punto.getTipo());
        tv.setPadding(32, 32, 32, 32);
        tv.setTextColor(Color.parseColor("#2D3436"));

        card.addView(tv);

        // 🚀 SOLUCIÓN: Cambiar 'card.setOnClickListener' por 'tv.setOnClickListener'
        tv.setOnClickListener(v -> {
            String puntoJson = new Gson().toJson(punto);

            Intent intent = new Intent(PerfilActivity.this, DetallePuntoPropioActivity.class);
            intent.putExtra("PUNTO_JSON", puntoJson);
            detalleLauncher.launch(intent);
        });

        containerMisPuntos.addView(card);
    }

    private void efectuarCierreSesion() {
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        evaluarEstadoYConfigurarUI();
    }

    // 🌟 REESTRUCTURADO: Eliminación directa en la interfaz basándose en vistas del LinearLayout
    private void eliminarPuntoDeLaListaLocal(String idEliminado) {
        if (containerMisPuntos != null) {
            // Buscamos la tarjeta en el contenedor que coincida con el tag (ID) borrado
            View tarjetaParaRemover = containerMisPuntos.findViewWithTag(idEliminado);

            if (tarjetaParaRemover != null) {
                containerMisPuntos.removeView(tarjetaParaRemover); // Remueve la tarjeta visualmente de inmediato

                // Actualizar de manera matemática el contador en la parte superior del perfil
                try {
                    int cantidadActual = Integer.parseInt(txtCantPuntos.getText().toString());
                    if (cantidadActual > 0) {
                        txtCantPuntos.setText(String.valueOf(cantidadActual - 1));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}