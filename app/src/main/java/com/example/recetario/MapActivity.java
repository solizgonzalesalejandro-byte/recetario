package com.example.recetario;   // cambia al nombre de tu paquete

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.recetario.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Coordenadas aproximadas del centro de Cochabamba
    private static final LatLng COCHABAMBA = new LatLng(-17.3895, -66.1568);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtener el fragment del mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);   // Cuando el mapa esté listo, llama a onMapReady
        }
    }

    // Este método se ejecuta cuando el mapa ya cargó
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Recibir la categoría enviada desde el Menu
        String categoria = getIntent().getStringExtra("categoria");

        // Configurar UI básica
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COCHABAMBA, 13f));

        // Lógica para cargar puntos (Simulación de lo que harás con Firebase o Local)
        if ("Reciclaje".equals(categoria)) {
            cargarPuntosReciclaje();
        }
        // ... repetir para otras categorías
    }

    private void cargarPuntosReciclaje() {
        // Ejemplo usando tu clase PuntoEcologico
        LatLng puntoEjemplo = new LatLng(-17.3935, -66.1468);
        mMap.addMarker(new MarkerOptions()
                .position(puntoEjemplo)
                .title("Punto de Reciclaje Norte")
                .snippet("Reciben plásticos y cartón"));
    }
}