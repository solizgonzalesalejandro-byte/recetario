package com.example.recetario;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPuntoLimpioActivity extends AppCompatActivity {

    private Spinner fSpinnerTipo;
    private TextInputEditText fNombre, fDireccion, fHorario, fDescripcion, fTelefono, fWhatsApp;
    private Button fBtnGuardar, fBtnFoto, btnAgregarRed, btnAgregarMaterial, btnAgregarRecompensa;
    private TextView fTxtEstadoFoto;
    private LinearLayout layoutCamposReciclaje, containerRedes, containerMateriales, containerRecompensas;

    private Punto.PuntoResponseDto puntoOriginal;
    private Double latitudActual = 0.0;
    private Double longitudActual = 0.0;
    private List<String> fotosBase64Mantenidas = new ArrayList<>();

    private final ActivityResultLauncher<Intent> mapaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    latitudActual = result.getData().getDoubleExtra("LATITUD_SELECCIONADA", latitudActual);
                    longitudActual = result.getData().getDoubleExtra("LONGITUD_SELECCIONADA", longitudActual);
                    String nuevaDireccion = result.getData().getStringExtra("DIRECCION_SELECCIONADA");
                    if (nuevaDireccion != null) fDireccion.setText(nuevaDireccion);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_punto_limpio);

        initViews();
        configurarSpinnerTipos();

        String jsonPunto = getIntent().getStringExtra("PUNTO_A_EDITAR_JSON");
        if (jsonPunto != null && !jsonPunto.isEmpty()) {
            puntoOriginal = new Gson().fromJson(jsonPunto, Punto.PuntoResponseDto.class);
            poblarDatosOriginales();
        }

        // Mostrar u ocultar bloque de reciclaje dinámicamente según el Spinner
        fSpinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = fSpinnerTipo.getSelectedItem().toString();
                if (seleccion.equalsIgnoreCase("Punto de Reciclaje") || seleccion.equalsIgnoreCase("Centro de Acopio")) {
                    layoutCamposReciclaje.setVisibility(View.VISIBLE);
                } else {
                    layoutCamposReciclaje.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configurar botones dinámicos de añadir filas
        btnAgregarRed.setOnClickListener(v -> agregarFilaRed("", ""));
        btnAgregarMaterial.setOnClickListener(v -> agregarFilaMaterial(""));
        btnAgregarRecompensa.setOnClickListener(v -> agregarFilaRecompensa("", "", 1));

        fDireccion.setOnClickListener(v -> {
            Intent intentMapa = new Intent(this, MapActivity.class);
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
    }

    private void configurarSpinnerTipos() {
        String[] opcionesTipo = {"Punto de Reciclaje", "Centro de Acopio", "Eco Tienda", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesTipo);
        fSpinnerTipo.setAdapter(adapter);
    }

    private void poblarDatosOriginales() {
        if (puntoOriginal == null) return;

        fNombre.setText(puntoOriginal.getNombre());
        fDireccion.setText(puntoOriginal.getDireccion());
        fHorario.setText(puntoOriginal.getHorario());
        fDescripcion.setText(puntoOriginal.getDescripcion());
        fTelefono.setText(puntoOriginal.getTelefono());
        fWhatsApp.setText(puntoOriginal.getWhatsapp());
        latitudActual = puntoOriginal.getLat();
        longitudActual = puntoOriginal.getLng();

        if (puntoOriginal.getImagenes() != null) {
            fotosBase64Mantenidas.addAll(puntoOriginal.getImagenes());
            fTxtEstadoFoto.setText(fotosBase64Mantenidas.size() + " foto(s) cargada(s).");
        }

        // Posicionar spinner
        if (puntoOriginal.getTipo() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) fSpinnerTipo.getAdapter();
            int pos = adapter.getPosition(puntoOriginal.getTipo());
            if (pos >= 0) fSpinnerTipo.setSelection(pos);
        }

        // Pintar Redes Sociales dinámicas existentes
        if (puntoOriginal.getRedes() != null) {
            for (Punto.RedDto red : puntoOriginal.getRedes()) {
                agregarFilaRed(red.getNombre(), red.getEnlace());
            }
        }

        // Pintar Materiales dinámicos existentes
        if (puntoOriginal.getMateriales() != null) {
            for (String material : puntoOriginal.getMateriales()) {
                agregarFilaMaterial(material);
            }
        }

        // Pintar Recompensas existentes
        if (puntoOriginal.getRecompensas() != null) {
            for (Punto.RecompensaDto rec : puntoOriginal.getRecompensas()) {
                agregarFilaRecompensa(rec.getNombre(), rec.getDescripcion(), rec.getStock());
            }
        }
    }

    // --- MANEJO DE FILAS DINÁMICAS (INTUITIVO CON BOTÓN ELIMINAR) ---

    private void agregarFilaRed(String nombre, String enlace) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 4, 0, 4);

        EditText etNombre = new EditText(this);
        etNombre.setHint("Ej: Facebook");
        etNombre.setText(nombre);
        etNombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        EditText etEnlace = new EditText(this);
        etEnlace.setHint("Url de la página");
        etEnlace.setText(enlace);
        etEnlace.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

        Button btnBorrar = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnBorrar.setText("X");
        btnBorrar.setTextColor(Color.RED);
        btnBorrar.setOnClickListener(v -> containerRedes.removeView(fila));

        fila.addView(etNombre);
        fila.addView(etEnlace);
        fila.addView(btnBorrar);
        containerRedes.addView(fila);
    }

    private void agregarFilaMaterial(String material) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 4, 0, 4);

        EditText etMaterial = new EditText(this);
        etMaterial.setHint("Ej: Plástico PET, Vidrio");
        etMaterial.setText(material);
        etMaterial.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button btnBorrar = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnBorrar.setText("X");
        btnBorrar.setTextColor(Color.RED);
        btnBorrar.setOnClickListener(v -> containerMateriales.removeView(fila));

        fila.addView(etMaterial);
        fila.addView(btnBorrar);
        containerMateriales.addView(fila);
    }

    private void agregarFilaRecompensa(String nombre, String desc, int stock) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.VERTICAL);
        fila.setPadding(8, 8, 8, 8);
        fila.setBackgroundColor(Color.parseColor("#FFFFFF"));

        EditText etNombre = new EditText(this);
        etNombre.setHint("Premio (Ej: Planta, Descuento)");
        etNombre.setText(nombre);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Condición (Ej: Por 50 tapas)");
        etDesc.setText(desc);

        EditText etStock = new EditText(this);
        etStock.setHint("Stock");
        etStock.setText(String.valueOf(stock));
        etStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        Button btnBorrar = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnBorrar.setText("Eliminar esta Recompensa");
        btnBorrar.setTextColor(Color.RED);
        btnBorrar.setOnClickListener(v -> containerRecompensas.removeView(fila));

        fila.addView(etNombre);
        fila.addView(etDesc);
        fila.addView(etStock);
        fila.addView(btnBorrar);
        containerRecompensas.addView(fila);
    }

    // --- RECOLECCIÓN Y ENVÍO ---

    // --- DENTRO DE EditarPuntoLimpioActivity.java ---

    private void ejecutarActualizacionPunto() {
        String nombreTxt = fNombre.getText().toString().trim();
        String direccionTxt = fDireccion.getText().toString().trim();

        if (nombreTxt.isEmpty() || direccionTxt.isEmpty()) {
            Toast.makeText(this, "Completa los datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Punto.PuntoRequestDto requestBody = new Punto.PuntoRequestDto();
        requestBody.setNombre(nombreTxt);
        requestBody.setDireccion(direccionTxt);
        requestBody.setHorario(fHorario.getText().toString().trim());
        requestBody.setDescripcion(fDescripcion.getText().toString().trim());
        requestBody.setTelefono(fTelefono.getText().toString().trim());
        requestBody.setWhatsapp(fWhatsApp.getText().toString().trim());
        requestBody.setTipo(fSpinnerTipo.getSelectedItem().toString());
        requestBody.setLat(latitudActual);
        requestBody.setLng(longitudActual);
        requestBody.setImagenes(fotosBase64Mantenidas);

        // 1. Recolectar Redes Sociales Dinámicas
        List<Punto.RedDto> listaRedes = new ArrayList<>();
        for (int i = 0; i < containerRedes.getChildCount(); i++) {
            LinearLayout f = (LinearLayout) containerRedes.getChildAt(i);
            String n = ((EditText) f.getChildAt(0)).getText().toString().trim();
            String e = ((EditText) f.getChildAt(1)).getText().toString().trim();
            if (!n.isEmpty()) listaRedes.add(new Punto.RedDto(n, e));
        }
        requestBody.setRedes(listaRedes);

        // 2. Recolectar Materiales Dinámicos
        List<String> listaMateriales = new ArrayList<>();
        for (int i = 0; i < containerMateriales.getChildCount(); i++) {
            LinearLayout f = (LinearLayout) containerMateriales.getChildAt(i);
            String m = ((EditText) f.getChildAt(0)).getText().toString().trim();
            if (!m.isEmpty()) listaMateriales.add(m);
        }
        requestBody.setMateriales(listaMateriales);

        // 3. Recolectar Recompensas
        List<Punto.RecompensaDto> listaRecompensas = new ArrayList<>();
        for (int i = 0; i < containerRecompensas.getChildCount(); i++) {
            LinearLayout f = (LinearLayout) containerRecompensas.getChildAt(i);
            String n = ((EditText) f.getChildAt(0)).getText().toString().trim();
            String d = ((EditText) f.getChildAt(1)).getText().toString().trim();
            String sStr = ((EditText) f.getChildAt(2)).getText().toString().trim();
            int s = sStr.isEmpty() ? 0 : Integer.parseInt(sStr);
            if (!n.isEmpty()) listaRecompensas.add(new Punto.RecompensaDto(n, d, s, "ACTIVO"));
        }
        requestBody.setRecompensas(listaRecompensas);

        // ... (Todo el inicio del método donde recolectas las redes, materiales y recompensas se mantiene igual)

        SharedPreferences prefs = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
        String idUsuario = prefs.getString("usuarioId", "ID_ANONIMO");

        // 1. Recuperamos el ID del punto original
        String idPunto = (puntoOriginal != null) ? puntoOriginal.getId() : null;

        // 🛑 VALIDACIÓN CRÍTICA: Si no hay ID de punto, ¡NO SE ACTUALIZA!
        if (idPunto == null || idPunto.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ Error: No se puede actualizar un punto sin un ID válido.", Toast.LENGTH_LONG).show();
            return; // Rompe la ejecución aquí mismo, la petición a la API nunca se envía
        }

        // 2. Seteamos el id de usuario directamente en el objeto que va al backend
        requestBody.setUsuarioId(idUsuario);

        // 3. Si pasó la validación, procedemos con la petición segura a Retrofit
        PuntoApiService apiService = RetrofitClient.getPuntoApiService();
        apiService.actualizarPunto(idPunto, idUsuario, requestBody).enqueue(new Callback<Punto.PuntoResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Response<Punto.PuntoResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditarPuntoLimpioActivity.this, "¡Cambios guardados con éxito!", Toast.LENGTH_SHORT).show();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("PUNTO_EDITADO_JSON", new Gson().toJson(response.body()));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(EditarPuntoLimpioActivity.this, "Error de Servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Throwable t) {
                Toast.makeText(EditarPuntoLimpioActivity.this, "Error de Red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}