package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton; // 🌟 Agregamos el import correcto para el botón flecha
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword;
    private Button btnRegistrar;
    private ImageButton btnAtras; // 🌟 Declaramos la variable para el botón de retroceso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registro); // Enlaza con tu XML de registro

        etNombre = findViewById(R.id.etRegNombre);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegistrar = findViewById(R.id.btnRegistrarUsuario);

        // 🌟 CORREGIDO: Buscamos el ID exacto que existe en tu layout_registro.xml
        btnAtras = findViewById(R.id.btnAtras);

        btnRegistrar.setOnClickListener(v -> registrarUsuarioEnServidor());

        // 🌟 Asignamos la acción directamente sin riesgo de puntero nulo
        if (btnAtras != null) {
            btnAtras.setOnClickListener(v -> finish());
        }
    }

    private void registrarUsuarioEnServidor() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario.UsuarioRequestDto nuevoUsuario = new Usuario.UsuarioRequestDto(nombre, correo, password);

        UsuarioApiService apiService = RetrofitClient.getUsuarioApiService();
        Call<Usuario.UsuarioResponseDto> call = apiService.crearUsuario(nuevoUsuario);

        call.enqueue(new Callback<Usuario.UsuarioResponseDto>() {
            @Override
            public void onResponse(Call<Usuario.UsuarioResponseDto> call, Response<Usuario.UsuarioResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario.UsuarioResponseDto usuarioCreado = response.body();

                    Toast.makeText(RegistroActivity.this, "¡Bienvenido " + usuarioCreado.getNombre() + ", registrado con éxito!", Toast.LENGTH_LONG).show();

                    // Guardamos el inicio de sesión automático en SharedPreferences
                    android.content.SharedPreferences sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("usuarioId", usuarioCreado.getId());
                    editor.putString("nombreUsuario", usuarioCreado.getNombre());
                    editor.putString("correoUsuario", usuarioCreado.getCorreo());
                    editor.apply();

                    // Navegamos al perfil limpio
                    Intent intent = new Intent(RegistroActivity.this, PerfilActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegistroActivity.this, "Error en registro. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario.UsuarioResponseDto> call, Throwable t) {
                Toast.makeText(RegistroActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}