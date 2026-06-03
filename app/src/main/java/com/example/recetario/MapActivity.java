package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

    // Componentes del BottomSheet para Detalles del Punto
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView txtDetalleNombre, txtDetalleTipo, txtDetalleDireccion, txtDetalleDescripcion, txtDetalleHorario, txtDetalleTelefono, txtDetalleMateriales;
    private View layoutDetalleDescripcion, layoutDetalleHorario, layoutDetalleTelefono, layoutDetalleWhatsapp, layoutDetalleMateriales, layoutDetalleImagen;
    private ImageView imgDetallePunto;
    private Button btnCerrarDetalle;

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

        } catch (Exception e) {
            Log.e(TAG, "Error fatal en onCreate: ", e);
            Toast.makeText(this, "Error al iniciar mapas", Toast.LENGTH_LONG).show();
        }
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
            imgDetallePunto = findViewById(R.id.imgDetallePunto);

            layoutDetalleImagen = findViewById(R.id.layoutDetalleImagen);
            layoutDetalleDescripcion = findViewById(R.id.layoutDetalleDescripcion);
            layoutDetalleHorario = findViewById(R.id.layoutDetalleHorario);
            layoutDetalleTelefono = findViewById(R.id.layoutDetalleTelefono);
            layoutDetalleWhatsapp = findViewById(R.id.layoutDetalleWhatsapp);
            layoutDetalleMateriales = findViewById(R.id.layoutDetalleMateriales);

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
                            LatLng posicion = new LatLng(punto.getLat(), punto.getLng());

                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(punto.getNombre())
                                    .snippet("Tipo: " + punto.getTipo()));

                            if (m != null) {
                                m.setTag(punto);
                            }
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

    private void mostrarDetallesPunto(Punto.PuntoResponseDto punto) {
        if (punto == null || bottomSheetBehavior == null) return;

        txtDetalleNombre.setText(punto.getNombre());
        txtDetalleTipo.setText(punto.getTipo());
        txtDetalleDireccion.setText(punto.getDireccion());

        if (punto.getImagenes() != null && !punto.getImagenes().isEmpty() && punto.getImagenes().get(0) != null) {
            try {
                String stringBinario = punto.getImagenes().get(0);
                if (stringBinario.contains(",")) {
                    stringBinario = stringBinario.split(",")[1];
                }
                byte[] decodedString = Base64.decode(stringBinario, Base64.DEFAULT);
                Bitmap bitmapDecodificado = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmapDecodificado != null && imgDetallePunto != null) {
                    imgDetallePunto.setImageBitmap(bitmapDecodificado);
                    layoutDetalleImagen.setVisibility(View.VISIBLE);
                } else {
                    layoutDetalleImagen.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                layoutDetalleImagen.setVisibility(View.GONE);
            }
        } else {
            layoutDetalleImagen.setVisibility(View.GONE);
        }

        if (punto.getDescripcion() != null && !punto.getDescripcion().trim().isEmpty() && layoutDetalleDescripcion != null) {
            txtDetalleDescripcion.setText(punto.getDescripcion());
            layoutDetalleDescripcion.setVisibility(View.VISIBLE);
        } else if (layoutDetalleDescripcion != null) {
            layoutDetalleDescripcion.setVisibility(View.GONE);
        }

        if (punto.getHorario() != null && !punto.getHorario().trim().isEmpty() && layoutDetalleHorario != null) {
            txtDetalleHorario.setText(punto.getHorario());
            layoutDetalleHorario.setVisibility(View.VISIBLE);
        } else if (layoutDetalleHorario != null) {
            layoutDetalleHorario.setVisibility(View.GONE);
        }

        if (punto.getTelefono() != null && !punto.getTelefono().trim().isEmpty() && layoutDetalleTelefono != null) {
            txtDetalleTelefono.setText(punto.getTelefono());
            layoutDetalleTelefono.setVisibility(View.VISIBLE);
        } else if (layoutDetalleTelefono != null) {
            layoutDetalleTelefono.setVisibility(View.GONE);
        }

        if (punto.getWhatsapp() != null && !punto.getWhatsapp().trim().isEmpty() && layoutDetalleWhatsapp != null) {
            layoutDetalleWhatsapp.setVisibility(View.VISIBLE);
            layoutDetalleWhatsapp.setOnClickListener(v -> {
                try {
                    String url = "https://api.whatsapp.com/send?phone=" + punto.getWhatsapp();
                    Intent intentWpp = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intentWpp);
                } catch (Exception e) {
                    Toast.makeText(this, "No se puede abrir WhatsApp", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (layoutDetalleWhatsapp != null) {
            layoutDetalleWhatsapp.setVisibility(View.GONE);
        }

        if (punto.getMateriales() != null && !punto.getMateriales().isEmpty() && layoutDetalleMateriales != null) {
            String materialesFormateados = android.text.TextUtils.join(", ", punto.getMateriales());
            txtDetalleMateriales.setText(materialesFormateados);
            layoutDetalleMateriales.setVisibility(View.VISIBLE);
        } else if (layoutDetalleMateriales != null) {
            layoutDetalleMateriales.setVisibility(View.GONE);
        }

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void cargarPuntosReciclaje() { cargarPuntosPorTipo("Reciclaje"); }
    private void cargarPuntosEsterilizacion() { cargarPuntosPorTipo("Esterilización"); }
    private void cargarPuntosAceite() { cargarPuntosPorTipo("Punto de Aceite"); }

    private void cargarTodosLosPuntos() {
        if (mMap != null) {
            mMap.clear();
            cargarPuntosPorTipo("todos");
        }
    }
}