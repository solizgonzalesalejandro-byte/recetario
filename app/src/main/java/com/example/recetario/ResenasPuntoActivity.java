package com.example.recetario;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ResenasPuntoActivity extends AppCompatActivity {

    private String idPunto;
    private String nombrePunto;

    private ImageButton btnAtrasResenas;
    private RecyclerView rvResenas;
    private TextView txtResenasVacio;
    private FloatingActionButton fabEscribirResena;

    private ResenasAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resenas_punto);

        sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
        // 🌟 Recuperamos el estado de la sesión
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        idPunto = getIntent().getStringExtra("PUNTO_ID");
        nombrePunto = getIntent().getStringExtra("PUNTO_NOMBRE");

        btnAtrasResenas = findViewById(R.id.btnAtrasResenas);
        rvResenas = findViewById(R.id.rvResenas);
        txtResenasVacio = findViewById(R.id.txtResenasVacio);
        fabEscribirResena = findViewById(R.id.fabEscribirResena);

        rvResenas.setLayoutManager(new LinearLayoutManager(this));
        btnAtrasResenas.setOnClickListener(v -> finish());

        // 🔒 Manejar el envío de nuevas opiniones con validación de sesión
        fabEscribirResena.setOnClickListener(v -> {
            if (isLoggedIn) {
                mostrarDialogoCrearResena();
            } else {
                // Mensaje amigable advirtiendo que necesita una cuenta activa
                Toast.makeText(this, "⚠️ ¡Debes iniciar sesión para dejar una reseña!", Toast.LENGTH_LONG).show();

                // OPCIONAL: Si quieres mandarlo directo a la pantalla de perfil/login descomenta la línea de abajo:
                // startActivity(new Intent(ResenasPuntoActivity.this, PerfilActivity.class));
            }
        });

        obtenerResenasDelServidor();
    }
    private void obtenerResenasDelServidor() {
        ResenasApiService apiService = RetrofitClient.getResenasApiService();

        apiService.obtenerReseniasPorPuntoId(idPunto).enqueue(new retrofit2.Callback<Resenia.ReseniasPuntoResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, retrofit2.Response<Resenia.ReseniasPuntoResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Resenia.ReseniaResponseDto> lista = response.body().getResenias();

                    if (lista == null || lista.isEmpty()) {
                        mostrarEstadoVacio();
                    } else {
                        txtResenasVacio.setVisibility(View.GONE);
                        rvResenas.setVisibility(View.VISIBLE);

                        adapter = new ResenasAdapter(lista);
                        rvResenas.setAdapter(adapter);
                    }
                } else {
                    mostrarEstadoVacio();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, Throwable t) {
                Toast.makeText(ResenasPuntoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCrearResena() {
        // 1. Inflar el diseño personalizado
        View dialogoVista = LayoutInflater.from(this).inflate(R.layout.dialog_escribir_resenia, null);
        RatingBar ratingBar = dialogoVista.findViewById(R.id.ratingBarResena);
        EditText etComentario = dialogoVista.findViewById(R.id.etComentarioResena);

        // 2. Construir la ventana flotante estilizada
        new MaterialAlertDialogBuilder(this)
                .setTitle("Evaluar: " + nombrePunto)
                .setView(dialogoVista)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Publicar", (dialog, which) -> {
                    int estrellas = (int) ratingBar.getRating();
                    String comentarioText = etComentario.getText().toString().trim();

                    if (estrellas == 0) {
                        Toast.makeText(this, "Por favor selecciona al menos 1 estrella", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Obtener el ID del usuario en sesión (o usar un valor por defecto si falla)
                    String usuarioIdActivo = sharedPreferences.getString("usuarioId", "anonimo_cocha_eco");

                    // 4. Enviar payload al servidor
                    enviarResenaAlServidor(usuarioIdActivo, estrellas, comentarioText);
                })
                .show();
    }

    private void enviarResenaAlServidor(String userId, int puntaje, String comentario) {
        Resenia.ReseniaRequestDto requestDto = new Resenia.ReseniaRequestDto(userId, puntaje, comentario);
        ResenasApiService apiService = RetrofitClient.getResenasApiService();

        apiService.agregarResenia(idPunto, requestDto).enqueue(new retrofit2.Callback<Resenia.ReseniasPuntoResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, retrofit2.Response<Resenia.ReseniasPuntoResponseDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ResenasPuntoActivity.this, "¡Gracias por tu opinión! 🌱", Toast.LENGTH_SHORT).show();
                    // Refrescar automáticamente la lista para mostrar el nuevo comentario
                    obtenerResenasDelServidor();
                } else {
                    Toast.makeText(ResenasPuntoActivity.this, "No se pudo guardar la reseña.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Resenia.ReseniasPuntoResponseDto> call, Throwable t) {
                Toast.makeText(ResenasPuntoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarEstadoVacio() {
        txtResenasVacio.setVisibility(View.VISIBLE);
        rvResenas.setVisibility(View.GONE);
    }
}