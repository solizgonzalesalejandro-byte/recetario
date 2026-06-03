package com.example.recetario;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import retrofit2.Call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

public class DetallePuntoPropioActivity extends AppCompatActivity {

    private TextView detalleNombre, detalleDescripcion, detalleDireccion, detalleCoordenadas, detalleHorario, detalleTelefono;
    private Button btnEditarPunto, btnAbrirMaps, btnAbrirWhatsapp;
    private ImageButton btnAtrasDetalle;
    private LinearLayout filaDireccion, filaCoordenadas, filaHorario, filaTelefono, contenedorRedes, contenedorRecompensas;
    private MaterialCardView cardRedes, cardRecompensas, cardGaleria;
    private ChipGroup grupoChipsTipos, grupoChipsMateriales;
    private RecyclerView rvGaleriaFotos;
    private GaleriaAdapter galeriaAdapter;
    private Punto.PuntoResponseDto punto;

    private Button btnEliminarPunto;

    // 1. Agrega este Launcher arriba de tu onCreate (junto a tus variables globales)
    // 1. Reemplaza tu launcher por este (Simplificado para evitar el cierre en cadena)
    private final ActivityResultLauncher<Intent> editarFormularioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 🌟 Aquí cambiamos Activity.RESULT_OK por solo RESULT_OK
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String jsonActualizado = result.getData().getStringExtra("PUNTO_EDITADO_JSON");
                    if (jsonActualizado != null && !jsonActualizado.isEmpty()) {
                        this.punto = new Gson().fromJson(jsonActualizado, Punto.PuntoResponseDto.class);
                        construirVistaDinamica();

                        Intent intentActualizacionPerfil = new Intent();
                        // 🌟 Aquí también cambiamos Activity.RESULT_OK por solo RESULT_OK
                        setResult(RESULT_OK, intentActualizacionPerfil);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_punto_propio);

        initViews();

        btnAtrasDetalle.setOnClickListener(v -> finish());

        // Recuperar el objeto JSON enviado desde PerfilActivity
        String puntoJson = getIntent().getStringExtra("PUNTO_JSON");
        if (puntoJson != null && !puntoJson.isEmpty()) {
            punto = new Gson().fromJson(puntoJson, Punto.PuntoResponseDto.class);
        }

        if (punto != null) {
            construirVistaDinamica();

            // Acción del botón Editar
            // 2. Modifica el listener del botón dentro de tu onCreate:
            // Reemplaza la acción de tu botón editar en el onCreate del Detalle:
            btnEditarPunto.setOnClickListener(v -> {
                Intent intentEditar = new Intent(DetallePuntoPropioActivity.this, EditarPuntoLimpioActivity.class);
                String jsonEnviar = new Gson().toJson(punto);
                intentEditar.putExtra("PUNTO_A_EDITAR_JSON", jsonEnviar);
                editarFormularioLauncher.launch(intentEditar);
            });

            btnEliminarPunto.setOnClickListener(v -> {
                // Cuadro de diálogo de confirmación antes de borrar
                new androidx.appcompat.app.AlertDialog.Builder(DetallePuntoPropioActivity.this)
                        .setTitle("¿Eliminar punto?")
                        .setMessage("Esta acción eliminará de forma permanente tu aporte ecológico.")
                        .setPositiveButton("Eliminar", (dialog, which) -> ejecutarEliminacion())
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }
    }

    private void initViews() {
        detalleNombre = findViewById(R.id.detalleNombre);
        detalleDescripcion = findViewById(R.id.detalleDescripcion);
        detalleDireccion = findViewById(R.id.detalleDireccion);
        detalleCoordenadas = findViewById(R.id.detalleCoordenadas);
        detalleHorario = findViewById(R.id.detalleHorario);
        detalleTelefono = findViewById(R.id.detalleTelefono);
        btnEditarPunto = findViewById(R.id.btnEditarPunto);
        btnAtrasDetalle = findViewById(R.id.btnAtrasDetalle);
        filaDireccion = findViewById(R.id.filaDireccion);
        filaCoordenadas = findViewById(R.id.filaCoordenadas);
        filaHorario = findViewById(R.id.filaHorario);
        filaTelefono = findViewById(R.id.filaTelefono);
        btnAbrirMaps = findViewById(R.id.btnAbrirMaps);
        btnAbrirWhatsapp = findViewById(R.id.btnAbrirWhatsapp);
        grupoChipsTipos = findViewById(R.id.grupoChipsTipos);
        grupoChipsMateriales = findViewById(R.id.grupoChipsMateriales);
        contenedorRedes = findViewById(R.id.contenedorRedes);
        contenedorRecompensas = findViewById(R.id.contenedorRecompensas);
        cardRedes = findViewById(R.id.cardRedes);
        cardRecompensas = findViewById(R.id.cardRecompensas);
        cardGaleria = findViewById(R.id.cardGaleria);
        rvGaleriaFotos = findViewById(R.id.rvGaleriaFotos);
        btnEliminarPunto = findViewById(R.id.btnEliminarPunto);
    }

    private void construirVistaDinamica() {
        // --- 1. Identidad ---
        setCampoConFila(detalleNombre, punto.getNombre(), null);
        setCampoConFila(detalleDescripcion, punto.getDescripcion(), null);

        // Chips de Categoría
        if (punto.getTipo() != null && !punto.getTipo().trim().isEmpty()) {
            addChip(grupoChipsTipos, punto.getTipo());
        }

        // --- 2. Ubicación y Contacto ---
        setCampoConFila(detalleDireccion, punto.getDireccion(), filaDireccion);
        if (punto.getDireccion() != null && !punto.getDireccion().trim().isEmpty()) {
            btnAbrirMaps.setOnClickListener(v -> abrirUbicacionEnMaps(punto.getDireccion()));
        }

        if (punto.getLat() != null && punto.getLng() != null) {
            filaCoordenadas.setVisibility(View.VISIBLE);
            detalleCoordenadas.setText("Lat: " + punto.getLat() + "\nLng: " + punto.getLng());
        } else {
            filaCoordenadas.setVisibility(View.GONE);
        }

        setCampoConFila(detalleHorario, punto.getHorario(), filaHorario);
        setCampoConFila(detalleTelefono, punto.getTelefono(), filaTelefono);

        if (punto.getWhatsapp() != null && !punto.getWhatsapp().isEmpty()) {
            btnAbrirWhatsapp.setVisibility(View.VISIBLE);
            btnAbrirWhatsapp.setOnClickListener(v -> abrirWhatsappChat(punto.getWhatsapp()));
        } else {
            btnAbrirWhatsapp.setVisibility(View.GONE);
        }

        // --- 3. Materiales Aceptados (Chips) ---
        grupoChipsMateriales.removeAllViews();
        if (punto.getMateriales() != null && !punto.getMateriales().isEmpty()) {
            for (String material : punto.getMateriales()) {
                addChip(grupoChipsMateriales, "♻️ " + material);
            }
        }

        // --- 4. Redes Sociales ---
        construirSeccionListas(punto.getRedes(), cardRedes, contenedorRedes, "REDES");

        // --- 5. Recompensas ---
        construirSeccionListas(punto.getRecompensas(), cardRecompensas, contenedorRecompensas, "RECOMPENSAS");

        // --- 6. Galería de Fotos ---
        if (punto.getImagenes() != null && !punto.getImagenes().isEmpty()) {
            cardGaleria.setVisibility(View.VISIBLE);
            configurarRecyclerViewGaleria(punto.getImagenes());
        } else {
            cardGaleria.setVisibility(View.GONE);
        }
    }

    private void setCampoConFila(TextView tv, String valor, View filaContainer) {
        if (valor != null && !valor.trim().isEmpty()) {
            tv.setText(valor);
            if (filaContainer != null) filaContainer.setVisibility(View.VISIBLE);
        } else {
            if (filaContainer != null) filaContainer.setVisibility(View.GONE);
        }
    }

    // 🌟 CORREGIDO: Ya no usa R.color.color_chips_background. Evita el "cannot find symbol"
    private void addChip(ChipGroup chipGroup, String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#EFEFEF")));
        chip.setTextColor(Color.parseColor("#2D3436"));
        chipGroup.addView(chip);
    }

    private void construirSeccionListas(List<?> lista, MaterialCardView card, LinearLayout contenedor, String tipoLista) {
        contenedor.removeAllViews();
        if (lista != null && !lista.isEmpty()) {
            card.setVisibility(View.VISIBLE);
            for (Object obj : lista) {
                if (tipoLista.equals("REDES")) {
                    Punto.RedDto red = (Punto.RedDto) obj;
                    addFilaConIconoYTexto(contenedor, "🌐 " + red.getNombre(), red.getEnlace(), "#2D3436");
                } else if (tipoLista.equals("RECOMPENSAS")) {
                    Punto.RecompensaDto recompensa = (Punto.RecompensaDto) obj;
                    addFilaConIconoYTexto(contenedor, "🎁 " + recompensa.getNombre(), recompensa.getDescripcion() + "\nStock: " + recompensa.getStock(), "#84B026");
                }
            }
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void addFilaConIconoYTexto(LinearLayout contenedor, String titulo, String subTitulo, String colorTitulo) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.VERTICAL);
        fila.setPadding(0, 0, 0, 16);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(titulo);
        tvTitulo.setTextSize(16);
        tvTitulo.setTextColor(Color.parseColor(colorTitulo));
        tvTitulo.setTransformationMethod(null);

        TextView tvSubTitulo = new TextView(this);
        tvSubTitulo.setText(subTitulo);
        tvSubTitulo.setTextSize(14);
        tvSubTitulo.setTextColor(Color.parseColor("#636E72"));
        tvSubTitulo.setPadding(0, 4, 0, 0);

        fila.addView(tvTitulo);
        fila.addView(tvSubTitulo);
        contenedor.addView(fila);
    }

    private void configurarRecyclerViewGaleria(List<String> listaImagenes) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvGaleriaFotos.setLayoutManager(layoutManager);
        galeriaAdapter = new GaleriaAdapter(listaImagenes);
        rvGaleriaFotos.setAdapter(galeriaAdapter);
    }

    private void abrirUbicacionEnMaps(String direccion) {
        String query = Uri.encode(direccion);
        Uri uri = Uri.parse("geo:0,0?q=" + query);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirWhatsappChat(String telefono) {
        String url = "https://api.whatsapp.com/send?phone=" + telefono;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // 🌟 AGREGADO: Método encargado de procesar la eliminación física mediante la API
    // 🌟 ACTUALIZADO: Llama al endpoint @DELETE correcto pasándole el ID del punto
    private void ejecutarEliminacion() {
        if (punto == null || punto.getId() == null) {
            Toast.makeText(this, "Error: No se puede identificar el ID del punto", Toast.LENGTH_SHORT).show();
            return;
        }

        PuntoApiService apiService = RetrofitClient.getPuntoApiService();

        // 🚀 Invocamos tu nuevo método eliminarPunto de la API
        apiService.eliminarPunto(punto.getId()).enqueue(new retrofit2.Callback<Punto.PuntoResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<Punto.PuntoResponseDto> call, retrofit2.Response<Punto.PuntoResponseDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetallePuntoPropioActivity.this, "Aporte eco eliminado correctamente", Toast.LENGTH_SHORT).show();

                    // 🚀 NUEVO: Crear un Intent para avisar a PerfilActivity qué ID se borró
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("PUNTO_ELIMINADO_ID", punto.getId());
                    setResult(RESULT_OK, resultIntent);

                    finish(); // Cierra e inicia el retorno
                } else {
                    Toast.makeText(DetallePuntoPropioActivity.this, "El servidor no pudo eliminar el punto (Código: " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Punto.PuntoResponseDto> call, Throwable t) {
                Toast.makeText(DetallePuntoPropioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Adaptador Interno para la Galería de Fotos ---
    private class GaleriaAdapter extends RecyclerView.Adapter<GaleriaAdapter.GaleriaViewHolder> {
        private List<String> imagenesBase64;

        public GaleriaAdapter(List<String> imagenesBase64) {
            this.imagenesBase64 = imagenesBase64;
        }

        @NonNull
        @Override
        public GaleriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_foto_galeria, parent, false);
            return new GaleriaViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GaleriaViewHolder holder, int position) {
            String base64String = imagenesBase64.get(position);

            if (base64String != null && !base64String.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivFoto.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return imagenesBase64.size();
        }

        class GaleriaViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFoto;
            public GaleriaViewHolder(@NonNull View itemView) {
                super(itemView);
                ivFoto = itemView.findViewById(R.id.ivFotoGaleria);
            }
        }
    }
}