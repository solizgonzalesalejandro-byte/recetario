package com.example.recetario;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.ui.IconGenerator;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnFiltroTodos, btnFiltroReciclaje, btnFiltroEsterilizacion, btnFiltroAceite;
    private GoogleMap mMap;
    private Marker marcadorTemporal;
    private static final LatLng COCHABAMBA = new LatLng(-17.3895, -66.1568);
    private static final String TAG = "MapActivity";

    private MaterialCardView cardBurbujaUsuarioMapa;
    private TextView txtInicialUsuarioMapa;
    private ImageView imgIconoInvitadoMapa;

    private PuntoApiService puntoApiService;
    private SharedPreferences sharedPreferences;
    private boolean isLoggedIn = false;

    // Arriba en tu clase
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView txtDetalleNombre, txtDetalleTipo, txtDetalleDireccion, txtDetalleDescripcion, txtDetalleHorario, txtDetalleTelefono, txtDetalleMateriales;
    private View layoutDetalleDescripcion, layoutDetalleHorario, layoutDetalleTelefono, layoutDetalleWhatsapp, layoutDetalleMateriales, layoutDetalleRedes, scrollDetalleImagenes;
    private LinearLayout containerImagenesPunto, containerRedesSociales;
    private Button btnCerrarDetalle;

    private List<Punto.PuntoResponseDto> listaPuntosTotales = new java.util.ArrayList<>();
    private AutoCompleteTextView buscadorMisPuntos;
    private ArrayAdapter<String> adapterBuscador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_map);

            cardBurbujaUsuarioMapa = findViewById(R.id.cardBurbujaUsuarioMapa);
            txtInicialUsuarioMapa = findViewById(R.id.txtInicialUsuarioMapa);
            imgIconoInvitadoMapa = findViewById(R.id.imgIconoInvitadoMapa);

            sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);

            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl("http://192.168.100.251:8083/")
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();

            puntoApiService = retrofit.create(PuntoApiService.class);

            try {
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), "AIzaSyBv3lsCkgk1wT4Qeo5ADF7Mf08EhQZVIpY");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en Google Places: " + e.getMessage());
            }

            SupportMapFragment mapFragment = (SupportMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            inicializarComponentesBottomSheet();
            configurarBuscadorDeLugares();
            setupPerfilListener();

            inicializarBuscadorLocal();
        } catch (Exception e) {
            Log.e(TAG, "Error fatal en onCreate: ", e);
            Toast.makeText(this, "Error al iniciar mapas", Toast.LENGTH_LONG).show();
        }
    }
        private void inicializarBuscadorLocal() {
            buscadorMisPuntos = findViewById(R.id.autoCompleteMisPuntos);
            // Inicializamos con lista vacía
            adapterBuscador = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new java.util.ArrayList<>());
            buscadorMisPuntos.setAdapter(adapterBuscador);

            buscadorMisPuntos.setOnItemClickListener((parent, view, position, id) -> {
                String nombreSeleccionado = (String) parent.getItemAtPosition(position);
                for (Punto.PuntoResponseDto p : listaPuntosTotales) {
                    if (p.getNombre().equals(nombreSeleccionado)) {
                        LatLng pos = new LatLng(p.getLat(), p.getLng());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
                        mostrarDetallesPunto(p);
                        // Ocultar teclado
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(buscadorMisPuntos.getWindowToken(), 0);
                        break;
                    }
                }
            });
        }

    private void inicializarComponentesBottomSheet() {
        try {
            View bottomSheet = findViewById(R.id.sheetDetallePunto);
            if (bottomSheet == null) return;

            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            txtDetalleNombre = findViewById(R.id.txtDetalleNombre);
            txtDetalleTipo = findViewById(R.id.txtDetalleTipo);
            txtDetalleDireccion = findViewById(R.id.txtDetalleDireccion);
            txtDetalleDescripcion = findViewById(R.id.txtDetalleDescripcion);
            txtDetalleHorario = findViewById(R.id.txtDetalleHorario);
            txtDetalleTelefono = findViewById(R.id.txtDetalleTelefono);
            txtDetalleMateriales = findViewById(R.id.txtDetalleMateriales);

            scrollDetalleImagenes = findViewById(R.id.scrollDetalleImagenes);
            containerImagenesPunto = findViewById(R.id.containerImagenesPunto);
            containerRedesSociales = findViewById(R.id.containerRedesSociales);

            layoutDetalleDescripcion = findViewById(R.id.layoutDetalleDescripcion);
            layoutDetalleHorario = findViewById(R.id.layoutDetalleHorario);
            layoutDetalleTelefono = findViewById(R.id.layoutDetalleTelefono);
            layoutDetalleWhatsapp = findViewById(R.id.layoutDetalleWhatsapp);
            layoutDetalleMateriales = findViewById(R.id.layoutDetalleMateriales);
            layoutDetalleRedes = findViewById(R.id.layoutDetalleRedes);

            btnCerrarDetalle = findViewById(R.id.btnCerrarDetalle);
            if (btnCerrarDetalle != null) {
                btnCerrarDetalle.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando BottomSheet: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        evaluarEstadoSesionBurbuja();
    }

    private void evaluarEstadoSesionBurbuja() {
        if (sharedPreferences == null || cardBurbujaUsuarioMapa == null) return;

        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String nombreUsuario = sharedPreferences.getString("nombreUsuario", "");

        cardBurbujaUsuarioMapa.setVisibility(View.VISIBLE);

        if (isLoggedIn && !nombreUsuario.isEmpty()) {
            txtInicialUsuarioMapa.setVisibility(View.VISIBLE);
            imgIconoInvitadoMapa.setVisibility(View.GONE);

            String inicial = nombreUsuario.substring(0, 1).toUpperCase();
            txtInicialUsuarioMapa.setText(inicial);
        } else {
            txtInicialUsuarioMapa.setVisibility(View.GONE);
            imgIconoInvitadoMapa.setVisibility(View.VISIBLE);
        }
    }

    private void setupPerfilListener() {
        if (cardBurbujaUsuarioMapa != null) {
            cardBurbujaUsuarioMapa.setOnClickListener(v -> {
                Intent intentPerfil = new Intent(MapActivity.this, PerfilActivity.class);
                startActivity(intentPerfil);
            });
        }
    }

    private void configurarBuscadorDeLugares() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setCountries("BO");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng ubicacionSeleccionada = place.getLatLng();

                    if (ubicacionSeleccionada != null && mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionSeleccionada, 16f));

                        if (marcadorTemporal != null) {
                            marcadorTemporal.remove();
                        }

                        marcadorTemporal = mMap.addMarker(new MarkerOptions()
                                .position(ubicacionSeleccionada)
                                .title("📍 " + place.getName())
                                .snippet("Tocame aquí para Agregar Punto"));

                        if (marcadorTemporal != null) {
                            marcadorTemporal.showInfoWindow();
                        }
                        if (bottomSheetBehavior != null) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "Error detectado en Google Places: " + status.getStatusMessage());
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnFiltroReciclaje = findViewById(R.id.btnFiltroReciclaje);
        btnFiltroEsterilizacion = findViewById(R.id.btnFiltroEsterilizacion);
        btnFiltroAceite = findViewById(R.id.btnFiltroAceite);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COCHABAMBA, 13f));

        runOnUiThread(() -> {
            String category = getIntent().getStringExtra("categoria");
            if (category == null || category.isEmpty()) {
                cargarTodosLosPuntos();
                actualizarEstiloBotonesFiltro(btnFiltroTodos);
            } else {
                ejecutarFiltroPorCategoria(category);
            }
        });

        if (btnFiltroTodos != null) btnFiltroTodos.setOnClickListener(v -> { cargarTodosLosPuntos(); actualizarEstiloBotonesFiltro(btnFiltroTodos); });
        if (btnFiltroReciclaje != null) btnFiltroReciclaje.setOnClickListener(v -> { mMap.clear(); cargarPuntosReciclaje(); actualizarEstiloBotonesFiltro(btnFiltroReciclaje); });
        if (btnFiltroEsterilizacion != null) btnFiltroEsterilizacion.setOnClickListener(v -> { mMap.clear(); cargarPuntosEsterilizacion(); actualizarEstiloBotonesFiltro(btnFiltroEsterilizacion); });
        if (btnFiltroAceite != null) btnFiltroAceite.setOnClickListener(v -> { mMap.clear(); cargarPuntosAceite(); actualizarEstiloBotonesFiltro(btnFiltroAceite); });

        mMap.setOnMapLongClickListener(latLng -> {
            if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            manejarSeleccionMapa(latLng);
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            if (marcadorTemporal != null && marker.getId().equals(marcadorTemporal.getId())) {
                if (isLoggedIn) {
                    Intent intentFormulario = new Intent(MapActivity.this, EncuestaPuntoActivity.class);
                    intentFormulario.putExtra("latitud", marker.getPosition().latitude);
                    intentFormulario.putExtra("longitud", marker.getPosition().longitude);
                    intentFormulario.putExtra("direccion", marker.getTitle());
                    startActivity(intentFormulario);
                } else {
                    Toast.makeText(MapActivity.this, "¡Debes iniciar sesión para registrar un punto!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MapActivity.this, PerfilActivity.class));
                }
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof Punto.PuntoResponseDto) {
                Punto.PuntoResponseDto puntoData = (Punto.PuntoResponseDto) marker.getTag();
                mostrarDetallesPunto(puntoData);
                return true;
            }
            return false;
        });

        mMap.setOnMapClickListener(latLng -> {
            if (marcadorTemporal != null) {
                marcadorTemporal.remove();
                marcadorTemporal = null;
                Toast.makeText(MapActivity.this, "Selección cancelada", Toast.LENGTH_SHORT).show();
            }
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        boolean modoRegistro = getIntent().getBooleanExtra("modo_registro", false);
        if (modoRegistro) {
            Toast.makeText(this, "📍 Mantén presionado el mapa para marcar el nuevo punto ecológico", Toast.LENGTH_LONG).show();
        }
    }

    private void manejarSeleccionMapa(LatLng latLng) {
        String direccionObtenida = "Dirección en Cochabamba";
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                direccionObtenida = direcciones.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            direccionObtenida = "Punto en Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
        }

        if (marcadorTemporal != null) {
            marcadorTemporal.remove();
        }

        marcadorTemporal = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(direccionObtenida)
                .snippet("Pulsar aquí para registrar punto"));

        if (marcadorTemporal != null) {
            marcadorTemporal.showInfoWindow();
        }
    }

    private void ejecutarFiltroPorCategoria(String categoria) {
        if (mMap == null) return;
        mMap.clear();
        if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if ("Reciclaje".equals(categoria)) {
            cargarPuntosReciclaje();
            actualizarEstiloBotonesFiltro(btnFiltroReciclaje);
        } else if ("Esterilizacion".equals(categoria) || "Esterilización".equals(categoria)) {
            cargarPuntosEsterilizacion();
            actualizarEstiloBotonesFiltro(btnFiltroEsterilizacion);
        } else if ("Aceite".equals(categoria) || "Punto de Aceite".equals(categoria)) {
            cargarPuntosAceite();
            actualizarEstiloBotonesFiltro(btnFiltroAceite);
        } else {
            cargarTodosLosPuntos();
            actualizarEstiloBotonesFiltro(btnFiltroTodos);
        }
    }

    private void actualizarEstiloBotonesFiltro(Button botonActivo) {
        Button[] botones = {btnFiltroTodos, btnFiltroReciclaje, btnFiltroEsterilizacion, btnFiltroAceite};
        for (Button btn : botones) {
            if (btn == null || botonActivo == null) continue;
            if (btn.getId() == botonActivo.getId()) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#84B026")));
                btn.setTextColor(android.graphics.Color.WHITE);
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                btn.setTextColor(android.graphics.Color.parseColor("#84B026"));
            }
        }
    }

    // 🌟 MÉTODOS REESTRUCTURADOS CON CARRUSEL DE IMÁGENES, ZOOM Y ENLACES DE REDES
    private void mostrarDetallesPunto(Punto.PuntoResponseDto punto) {
        if (punto == null || bottomSheetBehavior == null) return;

        // 1. Datos básicos
        txtDetalleNombre.setText(punto.getNombre());
        txtDetalleTipo.setText(punto.getTipo());
        txtDetalleDireccion.setText(punto.getDireccion());

        // 2. Carrusel de Imágenes
        containerImagenesPunto.removeAllViews();
        if (punto.getImagenes() != null && !punto.getImagenes().isEmpty()) {
            scrollDetalleImagenes.setVisibility(View.VISIBLE);
            for (String base64Str : punto.getImagenes()) {
                if (base64Str == null || base64Str.trim().isEmpty()) continue;
                try {
                    if (base64Str.contains(",")) base64Str = base64Str.split(",")[1];
                    byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (bitmap != null) {
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(240), dpToPx(180));
                        params.setMargins(0, 0, dpToPx(10), 0);
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setImageBitmap(bitmap);
                        imageView.setOnClickListener(v -> abrirImagenPantallaCompleta(bitmap));
                        containerImagenesPunto.addView(imageView);
                    }
                } catch (Exception e) { Log.e(TAG, "Error imagen", e); }
            }
        } else {
            scrollDetalleImagenes.setVisibility(View.GONE);
        }

        // 3. Descripción
        if (punto.getDescripcion() != null && !punto.getDescripcion().trim().isEmpty()) {
            txtDetalleDescripcion.setText(punto.getDescripcion());
            layoutDetalleDescripcion.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleDescripcion.setVisibility(View.GONE);
        }

        // 4. Horario
        if (punto.getHorario() != null && !punto.getHorario().trim().isEmpty()) {
            txtDetalleHorario.setText(punto.getHorario());
            layoutDetalleHorario.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleHorario.setVisibility(View.GONE);
        }

        // 5. Teléfono
        if (punto.getTelefono() != null && !punto.getTelefono().trim().isEmpty()) {
            txtDetalleTelefono.setText(punto.getTelefono());
            layoutDetalleTelefono.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleTelefono.setVisibility(View.GONE);
        }

        // 6. WhatsApp
        if (punto.getWhatsapp() != null && !punto.getWhatsapp().trim().isEmpty()) {
            layoutDetalleWhatsapp.setVisibility(View.VISIBLE);
            layoutDetalleWhatsapp.setOnClickListener(v -> abrirEnlaceExterno("https://api.whatsapp.com/send?phone=" + punto.getWhatsapp()));
        } else {
            layoutDetalleWhatsapp.setVisibility(View.GONE);
        }

        // 7. Materiales
        if (punto.getMateriales() != null && !punto.getMateriales().isEmpty()) {
            String lista = android.text.TextUtils.join(", ", punto.getMateriales());
            txtDetalleMateriales.setText(lista);
            layoutDetalleMateriales.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleMateriales.setVisibility(View.GONE);
        }

        // 8. Redes Sociales
        containerRedesSociales.removeAllViews();
        boolean tieneRedes = false;
        if (punto.getRedes() != null && !punto.getRedes().isEmpty()) {
            for (Punto.RedDto red : punto.getRedes()) {
                if (red.getNombre() != null && red.getEnlace() != null && !red.getEnlace().isEmpty()) {
                    agregarLinkRedSocial("🌐 " + red.getNombre(), red.getEnlace());
                    tieneRedes = true;
                }
            }
        }
        layoutDetalleRedes.setVisibility(tieneRedes ? View.VISIBLE : View.GONE);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    // Método auxiliar para inflar textos clickeables de redes sociales
    private void agregarLinkRedSocial(String etiqueta, final String url) {
        TextView tvLink = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(4), 0, dpToPx(4));
        tvLink.setLayoutParams(params);
        tvLink.setText(etiqueta);
        tvLink.setTextColor(android.graphics.Color.parseColor("#1A73E8"));
        tvLink.setTextSize(14);
        tvLink.setPaintFlags(tvLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        tvLink.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        tvLink.setOnClickListener(v -> abrirEnlaceExterno(url));
        containerRedesSociales.addView(tvLink);
    }

    // Diálogo flotante nativo para ver la foto en grande (Zoom)
    private void abrirImagenPantallaCompleta(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageViewGrande = new ImageView(this);
        imageViewGrande.setImageBitmap(bitmap);
        imageViewGrande.setScaleType(ImageView.ScaleType.FIT_CENTER);
        builder.setView(imageViewGrande);

        AlertDialog dialog = builder.create();
        // Al tocar la pantalla completa, se cierra la vista grande de inmediato
        imageViewGrande.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Intent seguro para redirigir al navegador nativo o aplicación correspondiente
    private void abrirEnlaceExterno(String url) {
        if (url == null || url.trim().isEmpty()) return;
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Intent intentNavegador = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intentNavegador);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el enlace o no tienes un navegador instalado", Toast.LENGTH_SHORT).show();
        }
    }

    // Conversor de unidades DP a píxeles para el diseño programático
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void cargarPuntosReciclaje() { cargarPuntosPorTipo("Reciclaje"); }
    private void cargarPuntosEsterilizacion() { cargarPuntosPorTipo("Esterilización"); }
    private void cargarPuntosAceite() { cargarPuntosPorTipo("Punto de Aceite"); }

    // Método auxiliar para crear el marcador con el nombre flotante


    private Marker agregarMarcadorConTexto(Punto.PuntoResponseDto punto) {
        if (mMap == null) return null;

        // Marcador verde estándar
        Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(punto.getLat(), punto.getLng()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Etiqueta de texto fija (GroundOverlay)
        // Esto coloca el nombre como una imagen fija sobre el mapa
        // Requiere crear un Bitmap con el texto (lo que hace el IconGenerator)
        IconGenerator labelGen = new IconGenerator(this);
        labelGen.setBackground(null); // Sin fondo para la etiqueta
        labelGen.setTextAppearance(R.style.EstiloTextoEtiqueta);
        Bitmap labelBitmap = labelGen.makeIcon(punto.getNombre());

        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(labelBitmap))
                .position(new LatLng(punto.getLat(), punto.getLng()), 200f, 100f)); // Tamaño del texto

        if (m != null) m.setTag(punto);
        return m;
    }


    private void cargarPuntosPorTipo(String tipoBackend) {
        if (mMap == null || puntoApiService == null) return;

        puntoApiService.buscarPorTipo(tipoBackend).enqueue(new retrofit2.Callback<List<Punto.PuntoResponseDto>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<Punto.PuntoResponseDto>> call,
                                   @NonNull retrofit2.Response<List<Punto.PuntoResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Punto.PuntoResponseDto> puntos = response.body();
                    for (Punto.PuntoResponseDto punto : puntos) {
                        try {
                            if (punto == null || mMap == null) continue;

                            // Usamos el nuevo método en lugar de MarkerOptions directo
                            Marker m = agregarMarcadorConTexto(punto);
                            if (m != null) m.setTag(punto);

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando marcador", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<Punto.PuntoResponseDto>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo de conexión: " + t.getMessage());
            }
        });
    }

    private void cargarTodosLosPuntos() {
        if (mMap != null) mMap.clear();
        puntoApiService.buscarPorTipo("todos").enqueue(new retrofit2.Callback<List<Punto.PuntoResponseDto>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<Punto.PuntoResponseDto>> call,
                                   @NonNull retrofit2.Response<List<Punto.PuntoResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPuntosTotales = response.body();

                    // Actualizar buscador
                    List<String> nombres = new java.util.ArrayList<>();
                    for (Punto.PuntoResponseDto p : listaPuntosTotales) {
                        nombres.add(p.getNombre());
                    }
                    adapterBuscador = new ArrayAdapter<>(MapActivity.this, android.R.layout.simple_dropdown_item_1line, nombres);
                    buscadorMisPuntos.setAdapter(adapterBuscador);

                    // Dibujar marcadores usando el nuevo método
                    for (Punto.PuntoResponseDto punto : listaPuntosTotales) {
                        Marker m = agregarMarcadorConTexto(punto);
                        if (m != null) m.setTag(punto);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<Punto.PuntoResponseDto>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo de conexión: " + t.getMessage());
            }
        });
    }
}