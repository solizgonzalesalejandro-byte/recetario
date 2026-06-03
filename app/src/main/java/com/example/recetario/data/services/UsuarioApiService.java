package com.example.recetario;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UsuarioApiService {

    @POST("api/usuarios/login")
    Call<Usuario.UsuarioResponseDto> login(@Body Usuario.LoginRequestDto loginRequest);

    @POST("api/usuarios")
    Call<Usuario.UsuarioResponseDto> crearUsuario(@Body Usuario.UsuarioRequestDto usuarioRequest);
}