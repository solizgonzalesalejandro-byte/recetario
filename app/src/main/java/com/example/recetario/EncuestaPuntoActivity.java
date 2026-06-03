package com.example.recetario;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EncuestaPuntoActivity extends AppCompatActivity {

    private static final String TAG = "EncuestaPuntoActivity";

    // Vistas principales
    private Spinner spinnerTipo;
    private TextInputEditText etNombre, etDireccion, etDescripcion, etTelefono, etWhatsApp, etHorario;
    private LinearLayout layoutCamposReciclaje;
    private Button btnFoto, btnGuardar, btnAgregarRed, btnAgregarMaterial, btnAgregarRecompensa;
    private TextView txtEstadoFoto;

    // Contenedores visuales para las listas
    private LinearLayout containerRedes, containerMateriales, containerRecompensas;

    // Estructuras de datos dinámicas (Listas definitivas del formulario)
    private final List<String> listaMateriales = new ArrayList<>();
    private final List<Punto.RecompensaDto> listaRecompensas = new ArrayList<>();
    private final List<Punto.RedDto> listaRedes = new ArrayList<>();

    // 🌟 CAMBIO: Lista dinámica para almacenar múltiples fotos codificadas en Base64
    private final List<String> listaImagenesBase64 = new ArrayList<>();

    private double latitud = 0.0, longitud = 0.0;
    private String usuarioIdLogueado = "";

    // Launchers para permisos y actividades multimedia
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private Uri fotoCamaraUri = null;
    private String permisoSolicitadoActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_encuesta_punto);

        initViews();
        initMultimediaLaunchers();
        recuperarDatosSesion();
        recuperarDatosMapa();
        configurarSpinnerYLogicaCampos();
        configurarBotonesDialogo();

        btnGuardar.setOnClickListener(v -> procesarRegistroPunto());
        btnFoto.setOnClickListener(v -> mostrarDialogoSeleccionMultimedia());
    }

    private void initViews() {
        spinnerTipo = findViewById(R.id.fSpinnerTipo);
        etNombre = findViewById(R.id.fNombre);
        etDireccion = findViewById(R.id.fDireccion);
        etDescripcion = findViewById(R.id.fDescripcion);
        etTelefono = findViewById(R.id.fTelefono);
        etWhatsApp = findViewById(R.id.fWhatsApp);
        etHorario = findViewById(R.id.fHorario);

        layoutCamposReciclaje = findViewById(R.id.layoutCamposReciclaje);
        containerRedes = findViewById(R.id.containerRedes);
        containerMateriales = findViewById(R.id.containerMateriales);
        containerRecompensas = findViewById(R.id.containerRecompensas);

        btnAgregarRed = findViewById(R.id.btnAgregarRed);
        btnAgregarMaterial = findViewById(R.id.btnAgregarMaterial);
        btnAgregarRecompensa = findViewById(R.id.btnAgregarRecompensa);

        btnFoto = findViewById(R.id.fBtnFoto);
        btnGuardar = findViewById(R.id.fBtnGuardar);
        txtEstadoFoto = findViewById(R.id.fTxtEstadoFoto);
    }

    private void configurarSpinnerYLogicaCampos() {
        String[] opcionesTipos = {"Reciclaje", "Esterilización", "Punto de Aceite"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesTipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (opcionesTipos[position].equals("Reciclaje")) {
                    layoutCamposReciclaje.setVisibility(View.VISIBLE);
                } else {
                    layoutCamposReciclaje.setVisibility(View.GONE);
                    listaMateriales.clear();
                    listaRecompensas.clear();
                    containerMateriales.removeAllViews();
                    containerRecompensas.removeAllViews();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void configurarBotonesDialogo() {
        // Dialog para Materiales
        btnAgregarMaterial.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Añadir Material");
            final EditText input = new EditText(this);
            input.setHint("Ej: Botellas PET, Cartón");
            builder.setView(input);

            builder.setPositiveButton("Añadir", (dialog, which) -> {
                String mat = input.getText().toString().trim();
                if (!mat.isEmpty()) {
                    listaMateriales.add(mat);
                    agregarVistaTextoItem(containerMateriales, mat);
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Dialog para Redes Sociales
        btnAgregarRed.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nueva Red Social / Web");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 20);

            final EditText etNombreRed = new EditText(this);
            etNombreRed.setHint("Nombre (Ej: Facebook, Web)");
            layout.addView(etNombreRed);

            final EditText etUrlRed = new EditText(this);
            etUrlRed.setHint("Enlace (Ej: https://...)");
            layout.addView(etUrlRed);

            builder.setView(layout);
            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String nom = etNombreRed.getText().toString().trim();
                String url = etUrlRed.getText().toString().trim();
                if (!nom.isEmpty() && !url.isEmpty()) {
                    listaRedes.add(new Punto.RedDto(nom, url));
                    agregarVistaTextoItem(containerRedes, nom + " (" + url + ")");
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Dialog para Recompensas
        btnAgregarRecompensa.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nueva Recompensa");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 20);

            final EditText etNomRec = new EditText(this);
            etNomRec.setHint("Nombre (Ej: EcoBolsa)");
            layout.addView(etNomRec);

            final EditText etDescRec = new EditText(this);
            etDescRec.setHint("Descripción (Ej: Canjeable por 20 puntos)");
            layout.addView(etDescRec);

            final EditText etStockRec = new EditText(this);
            etStockRec.setHint("Stock Inicial (Ej: 50)");
            etStockRec.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etStockRec);

            builder.setView(layout);
            builder.setPositiveButton("Agregar", (dialog, which) -> {
                String nom = etNomRec.getText().toString().trim();
                String desc = etDescRec.getText().toString().trim();
                String stockStr = etStockRec.getText().toString().trim();

                if (!nom.isEmpty() && !desc.isEmpty() && !stockStr.isEmpty()) {
                    int stock = Integer.parseInt(stockStr);
                    listaRecompensas.add(new Punto.RecompensaDto(nom, desc, stock, "DISPONIBLE"));
                    agregarVistaTextoItem(containerRecompensas, nom + " - Cant: " + stock);
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }

    private void agregarVistaTextoItem(LinearLayout contenedor, String textoAMostrar) {
        TextView tv = new TextView(this);
        tv.setText("• " + textoAMostrar);
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.background_dark));
        tv.setPadding(10, 6, 10, 6);
        contenedor.addView(tv);
    }

    // --- 📸 SECCIÓN MULTIMEDIA (CÁMARA Y GALERÍA) ACTUALIZADA PARA MULTI-FOTOS ---

    private void mostrarDialogoSeleccionMultimedia() {
        String[] opciones = {"Tomar Foto (Cámara)", "Elegir de la Galería", "Limpiar todas las fotos"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen (Puedes agregar varias)");
        builder.setItems(opciones, (dialog, miOpcion) -> {
            if (miOpcion == 0) {
                verificarPermisoCamaraYAbrir();
            } else if (miOpcion == 1) {
                verificarPermisoGaleriaYAbrir();
            } else if (miOpcion == 2) {
                // Opción extra por si el usuario quiere reiniciar la lista de imágenes
                listaImagenesBase64.clear();
                txtEstadoFoto.setText("No se ha seleccionado ninguna foto");
                txtEstadoFoto.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                Toast.makeText(this, "Lista de imágenes vaciada", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void initMultimediaLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (permisoSolicitadoActual.equals(Manifest.permission.CAMERA)) {
                    abrirCamara();
                } else {
                    abrirGaleria();
                }
            } else {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) procesarImagenDesdeUri(uri);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && fotoCamaraUri != null) {
                procesarImagenDesdeUri(fotoCamaraUri);
            }
        });
    }

    private void verificarPermisoCamaraYAbrir() {
        permisoSolicitadoActual = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void verificarPermisoGaleriaYAbrir() {
        String permisoAlmacenamiento = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        permisoSolicitadoActual = permisoAlmacenamiento;

        if (ContextCompat.checkSelfPermission(this, permisoAlmacenamiento) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            requestPermissionLauncher.launch(permisoAlmacenamiento);
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File fotoArchivo = crearArchivoImagenTemporal();
                if (fotoArchivo != null) {
                    fotoCamaraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fotoArchivo);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoCamaraUri);
                    cameraLauncher.launch(intent);
                }
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear el archivo de la cámara", ex);
            }
        }
    }

    private File crearArchivoImagenTemporal() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nombreArchivoJson = "JPEG_" + timeStamp + "_";
        File directorioAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombreArchivoJson, ".jpg", directorioAlmacenamiento);
    }

    private void procesarImagenDesdeUri(Uri imageUri) {
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // Redimensionado óptimo para evitar desbordamiento de memoria por el Base64 largo
                Bitmap.createScaledBitmap(bitmap, 800, (int)(800 * ((float)bitmap.getHeight()/bitmap.getWidth())), true)
                        .compress(Bitmap.CompressFormat.JPEG, 70, baos);

                // Convertir binario comprimido a String Base64
                String strBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                // 🌟 CAMBIO: Se añade la nueva foto a la lista en lugar de sobreescribir la anterior
                listaImagenesBase64.add(strBase64);

                // Actualizar contador en la interfaz
                txtEstadoFoto.setText("✨ ¡" + listaImagenesBase64.size() + " foto(s) acumulada(s) con éxito!");
                txtEstadoFoto.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error procesando la imagen", e);
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // --- FIN DE LA SECCIÓN MULTIMEDIA ---

    private void procesarRegistroPunto() {
        // 1. VALIDACIÓN DE SESIÓN (Crucial)
        // Recuperamos el ID actualizado (debe ser null si no hay sesión)
        recuperarDatosSesion();

        if (usuarioIdLogueado == null || usuarioIdLogueado.equals("ID_ANONIMO")) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Acción restringida")
                    .setMessage("Para registrar un punto ecológico, primero debes iniciar sesión.")
                    .setPositiveButton("Iniciar sesión", (dialog, which) -> {
                        Intent intent = new Intent(EncuestaPuntoActivity.this, LoginActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return; // Detenemos la ejecución aquí si no hay sesión
        }

        // 2. Validación de campos básicos
        String nombre = etNombre.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String direccion = etDireccion.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        btnGuardar.setEnabled(false);

        // 3. Preparación del DTO
        Punto.PuntoRequestDto nuevoPunto = new Punto.PuntoRequestDto();
        nuevoPunto.setNombre(nombre);
        nuevoPunto.setTipo(tipo);
        nuevoPunto.setDireccion(direccion.isEmpty() ? "Cochabamba, Bolivia" : direccion);
        nuevoPunto.setLat(latitud);
        nuevoPunto.setLng(longitud);
        nuevoPunto.setDescripcion(etDescripcion.getText().toString().trim());
        nuevoPunto.setTelefono(etTelefono.getText().toString().trim());
        nuevoPunto.setWhatsapp(etWhatsApp.getText().toString().trim());
        nuevoPunto.setHorario(etHorario.getText().toString().trim());

        // Asignación explícita del ID validado
        nuevoPunto.setUsuarioId(usuarioIdLogueado);

        nuevoPunto.setRedes(listaRedes.isEmpty() ? null : listaRedes);
        nuevoPunto.setMateriales(tipo.equals("Reciclaje") && !listaMateriales.isEmpty() ? listaMateriales : null);
        nuevoPunto.setRecompensas(tipo.equals("Reciclaje") && !listaRecompensas.isEmpty() ? listaRecompensas : null);
        nuevoPunto.setImagenes(listaImagenesBase64.isEmpty() ? null : listaImagenesBase64);

        // 4. Llamada a la API
        PuntoApiService apiService = RetrofitClient.getPuntoApiService();
        Call<Punto.PuntoResponseDto> call = apiService.crearPunto(nuevoPunto);

        call.enqueue(new Callback<Punto.PuntoResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Response<Punto.PuntoResponseDto> response) {
                btnGuardar.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    // Aquí el servidor debería devolver el punto con su nuevo ID
                    Toast.makeText(EncuestaPuntoActivity.this, "🌱 ¡Punto guardado exitosamente!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(EncuestaPuntoActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Punto.PuntoResponseDto> call, @NonNull Throwable t) {
                btnGuardar.setEnabled(true);
                Toast.makeText(EncuestaPuntoActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Asegúrate de que este método sea así de limpio:
    private void recuperarDatosSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
        // Cambiamos el valor por defecto a null para que la validación if(...) funcione
        usuarioIdLogueado = sharedPreferences.getString("usuarioId", null);
    }

    private void recuperarDatosMapa() {
        if (getIntent() != null) {
            latitud = getIntent().getDoubleExtra("latitud", 0.0);
            longitud = getIntent().getDoubleExtra("longitud", 0.0);
            String dir = getIntent().getStringExtra("direccion");
            if (dir != null) etDireccion.setText(dir);
            else etDireccion.setText("Cochabamba, Bolivia");
        }
    }
}