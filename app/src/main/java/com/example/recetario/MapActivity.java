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

    // Control de Modo Selección para Editar Formulario
    private boolean esModoSeleccion = false;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView txtDetalleNombre, txtDetalleTipo, txtDetalleDireccion, txtDetalleDescripcion, txtDetalleHorario, txtDetalleTelefono, txtDetalleMateriales;
    private View layoutDetalleDescripcion, layoutDetalleHorario, layoutDetalleTelefono, layoutDetalleWhatsapp, layoutDetalleMateriales, layoutDetalleRedes, scrollDetalleImagenes;
    private LinearLayout containerImagenesPunto, containerRedesSociales;
    private Button btnCerrarDetalle;

    // 🌟 VARIABLES GLOBALES PARA CONTROLAR RESEÑAS
    private Punto.PuntoResponseDto puntoSeleccionadoActual;
    private TextView txtResumenCalificacion;
    private TextView txtEscribirResenaLink;
    private View layoutDetalleResenas;

    private List<Punto.PuntoResponseDto> listaPuntosTotales = new java.util.ArrayList<>();
    private AutoCompleteTextView buscadorMisPuntos;
    private ArrayAdapter<String> adapterBuscador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_map);

            // 🗺️ DETECTAR SI LLEGA DESDE EL FORMULARIO DE EDICIÓN
            esModoSeleccion = getIntent().getBooleanExtra("modo_seleccion", false);

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

            // Deshabilitar elementos visuales molestos si solo venimos a seleccionar un punto
            if (esModoSeleccion) {
                if (cardBurbujaUsuarioMapa != null) cardBurbujaUsuarioMapa.setVisibility(View.GONE);
                Toast.makeText(this, "📍 Mantén presionado el mapa y pulsa el recuadro informativo para elegir la ubicación", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fatal en onCreate: ", e);
            Toast.makeText(this, "Error al iniciar mapas", Toast.LENGTH_LONG).show();
        }
    }

    private void inicializarBuscadorLocal() {
        buscadorMisPuntos = findViewById(R.id.autoCompleteMisPuntos);
        adapterBuscador = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new java.util.ArrayList<>());
        buscadorMisPuntos.setAdapter(adapterBuscador);

        buscadorMisPuntos.setOnItemClickListener((parent, view, position, id) -> {
            String nombreSeleccionado = (String) parent.getItemAtPosition(position);
            for (Punto.PuntoResponseDto p : listaPuntosTotales) {
                if (p.getNombre().equals(nombreSeleccionado)) {
                    LatLng pos = new LatLng(p.getLat(), p.getLng());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
                    if (!esModoSeleccion) {
                        mostrarDetallesPunto(p);
                    }
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null && buscadorMisPuntos != null) {
                        imm.hideSoftInputFromWindow(buscadorMisPuntos.getWindowToken(), 0);
                    }
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

            // 🌟 VINCULACIÓN DE VISTAS DE RESEÑAS
            layoutDetalleResenas = findViewById(R.id.layoutDetalleResenas);
            txtResumenCalificacion = findViewById(R.id.txtResumenCalificacion);
            txtEscribirResenaLink = findViewById(R.id.txtEscribirResenaLink);

            // Click en todo el contenedor o en "Ver Todas" -> Abre la lista completa
            if (layoutDetalleResenas != null) {
                layoutDetalleResenas.setOnClickListener(v -> abrirPantallaResenas());
            }

            // Click exclusivo en "✍️ Evaluar" para registrar reseña
            if (txtEscribirResenaLink != null) {
                txtEscribirResenaLink.setOnClickListener(v -> abrirPantallaResenas());
            }

            btnCerrarDetalle = findViewById(R.id.btnCerrarDetalle);
            if (btnCerrarDetalle != null) {
                btnCerrarDetalle.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando BottomSheet: " + e.getMessage());
        }
    }

    // 🌟 MÉTODO AUXILIAR PARA HACER EL INTENT HACIA TU RESENASPUNTOACTIVITY
    private void abrirPantallaResenas() {
        if (puntoSeleccionadoActual != null && puntoSeleccionadoActual.getId() != null) {
            Intent intent = new Intent(MapActivity.this, ResenasPuntoActivity.class);
            intent.putExtra("PUNTO_ID", puntoSeleccionadoActual.getId());
            intent.putExtra("PUNTO_NOMBRE", puntoSeleccionadoActual.getNombre());
            startActivity(intent);
        } else {
            Toast.makeText(MapActivity.this, "El punto no tiene un identificador válido.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!esModoSeleccion) {
            evaluarEstadoSesionBurbuja();
        }
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
                                .snippet(esModoSeleccion ? "Pulsar aquí para confirmar ubicación" : "Tocame aquí para Agregar Punto"));

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

        // Centrar mapa si el formulario nos provee coordenadas anteriores de edición
        double latPrevia = getIntent().getDoubleExtra("LAT_ACTUAL", 0.0);
        double lngPrevia = getIntent().getDoubleExtra("LNG_ACTUAL", 0.0);
        if (latPrevia != 0.0 && lngPrevia != 0.0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latPrevia, lngPrevia), 16f));
            marcadorTemporal = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latPrevia, lngPrevia))
                    .title("Ubicación Actual")
                    .snippet(esModoSeleccion ? "Pulsar aquí para confirmar esta misma" : ""));
            if (marcadorTemporal != null) marcadorTemporal.showInfoWindow();
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COCHABAMBA, 13f));
        }

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

        // 🚀 INTERCEPTOR CRÍTICO: Al hacer click al InfoWindow del marcador
        mMap.setOnInfoWindowClickListener(marker -> {
            if (esModoSeleccion) {
                // 1. Devolver datos de ubicación y dirección al FormularioEditarPuntoActivity
                Intent returnIntent = new Intent();
                returnIntent.putExtra("LATITUD_SELECCIONADA", marker.getPosition().latitude);
                returnIntent.putExtra("LONGITUD_SELECCIONADA", marker.getPosition().longitude);
                returnIntent.putExtra("DIRECCION_SELECCIONADA", marker.getTitle().replace("📍 ", ""));
                setResult(RESULT_OK, returnIntent);
                finish(); // Finaliza y regresa al formulario
            } else {
                // Flujo estándar antiguo para registrar un punto nuevo
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
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            if (esModoSeleccion) {
                marker.showInfoWindow();
                return true;
            }
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
                .snippet(esModoSeleccion ? "Pulsar aquí para confirmar ubicación" : "Pulsar aquí para registrar punto"));

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

    private void mostrarDetallesPunto(Punto.PuntoResponseDto punto) {
        if (punto == null || bottomSheetBehavior == null) return;

        txtDetalleNombre.setText(punto.getNombre());
        txtDetalleTipo.setText(punto.getTipo());
        txtDetalleDireccion.setText(punto.getDireccion());

        // Almacenamos el objeto seleccionado globalmente
        this.puntoSeleccionadoActual = punto;

        // 🌟 CONTROL DE VISIBILIDAD DE REVISIÓN PARA USUARIO LOGUEADO
        if (txtEscribirResenaLink != null) {
            if (isLoggedIn) {
                txtEscribirResenaLink.setVisibility(View.VISIBLE); // Visible si inició sesión
            } else {
                txtEscribirResenaLink.setVisibility(View.GONE);    // Invisible si es invitado
            }
        }

        // 🌟 CONSULTAR CALIFICACIONES UTILIZANDO TU CLIENTE RETROFIT DE RESEÑAS
        RetrofitClient.getResenasApiService().obtenerReseniasPorPuntoId(punto.getId()).enqueue(new retrofit2.Callback<Resenia.ReseniasPuntoResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, retrofit2.Response<Resenia.ReseniasPuntoResponseDto> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResenias() != null) {
                    List<Resenia.ReseniaResponseDto> resenias = response.body().getResenias();
                    if (!resenias.isEmpty()) {
                        double suma = 0;
                        for (Resenia.ReseniaResponseDto r : resenias) {
                            if (r.getPuntaje() != null) suma += r.getPuntaje();
                        }
                        double promedio = suma / resenias.size();
                        String promedioFormateado = String.format(Locale.US, "%.1f", promedio);
                        if (txtResumenCalificacion != null) {
                            txtResumenCalificacion.setText("⭐ " + promedioFormateado + " (" + resenias.size() + " opiniones)");
                        }
                    } else {
                        if (txtResumenCalificacion != null) txtResumenCalificacion.setText("⭐ Sin calificaciones");
                    }
                } else {
                    if (txtResumenCalificacion != null) txtResumenCalificacion.setText("⭐ Sin calificaciones");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, Throwable t) {
                if (txtResumenCalificacion != null) txtResumenCalificacion.setText("⭐ Error al cargar");
            }
        });

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

        if (punto.getDescripcion() != null && !punto.getDescripcion().trim().isEmpty()) {
            txtDetalleDescripcion.setText(punto.getDescripcion());
            layoutDetalleDescripcion.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleDescripcion.setVisibility(View.GONE);
        }

        if (punto.getHorario() != null && !punto.getHorario().trim().isEmpty()) {
            txtDetalleHorario.setText(punto.getHorario());
            layoutDetalleHorario.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleHorario.setVisibility(View.GONE);
        }

        if (punto.getTelefono() != null && !punto.getTelefono().trim().isEmpty()) {
            txtDetalleTelefono.setText(punto.getTelefono());
            layoutDetalleTelefono.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleTelefono.setVisibility(View.GONE);
        }

        if (punto.getWhatsapp() != null && !punto.getWhatsapp().trim().isEmpty()) {
            layoutDetalleWhatsapp.setVisibility(View.VISIBLE);
            layoutDetalleWhatsapp.setOnClickListener(v -> abrirEnlaceExterno("https://api.whatsapp.com/send?phone=" + punto.getWhatsapp()));
        } else {
            layoutDetalleWhatsapp.setVisibility(View.GONE);
        }

        if (punto.getMateriales() != null && !punto.getMateriales().isEmpty()) {
            String lista = android.text.TextUtils.join(", ", punto.getMateriales());
            txtDetalleMateriales.setText(lista);
            layoutDetalleMateriales.setVisibility(View.VISIBLE);
        } else {
            layoutDetalleMateriales.setVisibility(View.GONE);
        }

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

    private void abrirImagenPantallaCompleta(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageViewGrande = new ImageView(this);
        imageViewGrande.setImageBitmap(bitmap);
        imageViewGrande.setScaleType(ImageView.ScaleType.FIT_CENTER);
        builder.setView(imageViewGrande);

        AlertDialog dialog = builder.create();
        imageViewGrande.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void cargarPuntosReciclaje() { cargarPuntosPorTipo("Reciclaje"); }
    private void cargarPuntosEsterilizacion() { cargarPuntosPorTipo("Esterilización"); }
    private void cargarPuntosAceite() { cargarPuntosPorTipo("Punto de Aceite"); }

    private Marker agregarMarcadorConTexto(Punto.PuntoResponseDto punto) {
        if (mMap == null) return null;

        Marker m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(punto.getLat(), punto.getLng()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        IconGenerator labelGen = new IconGenerator(this);
        labelGen.setBackground(null);
        labelGen.setTextAppearance(R.style.EstiloTextoEtiqueta);
        Bitmap labelBitmap = labelGen.makeIcon(punto.getNombre());

        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(labelBitmap))
                .position(new LatLng(punto.getLat(), punto.getLng()), 200f, 100f));

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

                    List<String> nombres = new java.util.ArrayList<>();
                    for (Punto.PuntoResponseDto p : listaPuntosTotales) {
                        nombres.add(p.getNombre());
                    }
                    adapterBuscador = new ArrayAdapter<>(MapActivity.this, android.R.layout.simple_dropdown_item_1line, nombres);
                    buscadorMisPuntos.setAdapter(adapterBuscador);

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