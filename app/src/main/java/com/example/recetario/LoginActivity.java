package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnIniciarSesion;
    private TextView txtRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        txtRegistrarse = findViewById(R.id.txtRegistrarse);

        btnIniciarSesion.setOnClickListener(v -> validarLogin());

        txtRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
            // Cerramos LoginActivity para que al registrarse vaya directo al Perfil sin dejar esta pantalla atrás
            finish();
        });

        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
    }

    private void validarLogin() {
        String correo = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Campos incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario.LoginRequestDto loginData = new Usuario.LoginRequestDto(correo, password);

        UsuarioApiService apiService = RetrofitClient.getUsuarioApiService();
        Call<Usuario.UsuarioResponseDto> call = apiService.login(loginData);

        call.enqueue(new Callback<Usuario.UsuarioResponseDto>() {
            @Override
            public void onResponse(Call<Usuario.UsuarioResponseDto> call, Response<Usuario.UsuarioResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario.UsuarioResponseDto user = response.body();
                    Toast.makeText(LoginActivity.this, "¡Hola de nuevo, " + user.getNombre() + "!", Toast.LENGTH_SHORT).show();

                    // Guardamos el estado de sesión de forma persistente
                    android.content.SharedPreferences sharedPreferences = getSharedPreferences("CochaEcoPrefs", Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("usuarioId", user.getId()); // Guardamos el ID para las peticiones de red
                    editor.putString("nombreUsuario", user.getNombre());
                    editor.putString("correoUsuario", user.getCorreo());
                    editor.apply();

                    // 🌟 CAMBIO: Redirección directa a PerfilActivity limpiando la pila
                    Intent intent = new Intent(LoginActivity.this, PerfilActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario.UsuarioResponseDto> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}