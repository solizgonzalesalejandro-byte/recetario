package com.example.recetario;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioEditarPuntoActivity extends AppCompatActivity {

    private Spinner fSpinnerTipo;
    private TextInputEditText fNombre, fDireccion, fHorario, fDescripcion, fTelefono, fWhatsApp;
    private Button fBtnGuardar, fBtnFoto;
    private TextView fTxtEstadoFoto;
    private LinearLayout layoutCamposReciclaje, containerRedes, containerMateriales, containerRecompensas;
    private Button btnAgregarRed, btnAgregarMaterial, btnAgregarRecompensa;

    private Punto.PuntoResponseDto puntoOriginal;
    private Double latitudActual = 0.0;
    private Double longitudActual = 0.0;

    private final ActivityResultLauncher<Intent> mapaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    latitudActual = result.getData().getDoubleExtra("LATITUD_SELECCIONADA", latitudActual);
                    longitudActual = result.getData().getDoubleExtra("LONGITUD_SELECCIONADA", longitudActual);
                    String nuevaDireccion = result.getData().getStringExtra("DIRECCION_SELECCIONADA");

                    if (nuevaDireccion != null) {
                        fDireccion.setText(nuevaDireccion);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_encuesta_punto);

        initViews();
        configurarSpinnerTipos();

        String jsonPunto = getIntent().getStringExtra("PUNTO_JSON_EDITAR");
        if (jsonPunto != null && !jsonPunto.isEmpty()) {
            puntoOriginal = new Gson().fromJson(jsonPunto, Punto.PuntoResponseDto.class);
            precargarDatosEnPantalla();
        } else {
            Toast.makeText(this, "Error: No se recibieron datos para editar", Toast.LENGTH_SHORT).show();
            finish();
        }

        fDireccion.setOnClickListener(v -> {
            Intent intentMapa = new Intent(FormularioEditarPuntoActivity.this, MapActivity.class);
            intentMapa.putExtra("modo_seleccion", true);
            intentMapa.putExtra("LAT_ACTUAL", latitudActual);
            intentMapa.putExtra("LNG_ACTUAL", longitudActual);
            mapaLauncher.launch(intentMapa);
        });

        fBtnGuardar.setOnClickListener(v -> ejecutarActualizacionPunto());
    }

    private void initViews() {
        fSpinnerTipo = findViewById(R.id.fSpinnerTipo);
        fNombre = findViewById(R.id.fNombre);
        fDireccion = findViewById(R.id.fDireccion);
        fHorario = findViewById(R.id.fHorario);
        fDescripcion = findViewById(R.id.fDescripcion);
        fTelefono = findViewById(R.id.fTelefono);
        fWhatsApp = findViewById(R.id.fWhatsApp);
        fBtnGuardar = findViewById(R.id.fBtnGuardar);
        fBtnFoto = findViewById(R.id.fBtnFoto);
        fTxtEstadoFoto = findViewById(R.id.fTxtEstadoFoto);
        layoutCamposReciclaje = findViewById(R.id.layoutCamposReciclaje);
        containerRedes = findViewById(R.id.containerRedes);
        containerMateriales = findViewById(R.id.containerMateriales);
        containerRecompensas = findViewById(R.id.containerRecompensas);
        btnAgregarRed = findViewById(R.id.btnAgregarRed);
        btnAgregarMaterial = findViewById(R.id.btnAgregarMaterial);
        btnAgregarRecompensa = findViewById(R.id.btnAgregarRecompensa);

        fBtnGuardar.setText("ACTUALIZAR DATOS");
        fDireccion.setFocusable(false);
        fDireccion.setClickable(true);
    }

    private void configurarSpinnerTipos() {
        String[] opcionesTipo = {"Punto de Reciclaje", "Centro de Acopio", "Eco Tienda", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesTipo);
        fSpinnerTipo.setAdapter(adapter);
    }

    private void precargarDatosEnPantalla() {
        if (puntoOriginal == null) return;

        fNombre.setText(puntoOriginal.getNombre());
        fDireccion.setText(puntoOriginal.getDireccion());
        fHorario.setText(puntoOriginal.getHorario());
        fDescripcion.setText(puntoOriginal.getDescripcion());
        fTelefono.setText(puntoOriginal.getTelefono());
        fWhatsApp.setText(puntoOriginal.getWhatsapp());

        latitudActual = puntoOriginal.getLat();
        longitudActual = puntoOriginal.getLng();

        if (puntoOriginal.getTipo() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) fSpinnerTipo.getAdapter();
            int position = adapter.getPosition(puntoOriginal.getTipo());
            if (position >= 0) fSpinnerTipo.setSelection(position);
        }

        if (puntoOriginal.getTipo() != null && puntoOriginal.getTipo().toLowerCase().contains("reciclaje")) {
            layoutCamposReciclaje.setVisibility(View.VISIBLE);
        }
    }

    private void ejecutarActualizacionPunto() {
        String nuevoNombre = fNombre.getText().toString().trim();
        String nuevaDireccion = fDireccion.getText().toString().trim();
        String nuevoHorario = fHorario.getText().toString().trim();
        String nuevaDescripcion = fDescripcion.getText().toString().trim();
        String nuevoTelefono = fTelefono.getText().toString().trim();
        String nuevoWhatsApp = fWhatsApp.getText().toString().trim();
        String nuevoTipo = fSpinnerTipo.getSelectedItem().toString();

        if (nuevoNombre.isEmpty() || nuevaDireccion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        Punto.PuntoRequestDto requestBody = new Punto.PuntoRequestDto();
        requestBody.setNombre(nuevoNombre);
        requestBody.setDireccion(nuevaDireccion);
        requestBody.setHorario(nuevoHorario);
        requestBody.setDescripcion(nuevaDescripcion);
        requestBody.setTelefono(nuevoTelefono);
        requestBody.setWhatsapp(nuevoWhatsApp);
        requestBody.setTipo(nuevoTipo);
        requestBody.setLat(latitudActual);
        requestBody.setLng(longitudActual);

        if (puntoOriginal != null) {
            requestBody.setRedes(puntoOriginal.getRedes());
            requestBody.setMateriales(puntoOriginal.getMateriales());
            requestBody.setRecompensas(puntoOriginal.getRecompensas());
            requestBody.setImagenes(puntoOriginal.getImagenes());
        }

        String idPunto = (puntoOriginal != null) ? puntoOriginal.getId() : "";

        // Sincronizado exactamente con PerfilActivity:
        SharedPreferences prefs = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
        String idUsuario = prefs.getString("usuarioId", "ID_ANONIMO");

        if (idPunto.isEmpty() || idUsuario.equals("ID_ANONIMO")) {
            Toast.makeText(this, "Error: No se pudo identificar el punto o la sesión del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Enviando actualización al servidor...", Toast.LENGTH_SHORT).show();

        PuntoApiService apiService = RetrofitClient.getPuntoApiService();
        apiService.actualizarPunto(idPunto, idUsuario, requestBody).enqueue(new Callback<Punto.PuntoResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Response<Punto.PuntoResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(FormularioEditarPuntoActivity.this, "¡Punto actualizado correctamente!", Toast.LENGTH_SHORT).show();

                    Intent returnIntent = new Intent();
                    String nuevoJson = new Gson().toJson(response.body());
                    returnIntent.putExtra("PUNTO_EDITADO_JSON", nuevoJson);
                    setResult(Activity.RESULT_OK, returnIntent);

                    finish();
                } else {
                    Toast.makeText(FormularioEditarPuntoActivity.this, "Error de servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            // 🌟 CORREGIDO: Cambiado PuntoRequestDto por PuntoResponseDto para solucionar el "name clash"
            @Override
            public void onFailure(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Throwable t) {
                Toast.makeText(FormularioEditarPuntoActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}