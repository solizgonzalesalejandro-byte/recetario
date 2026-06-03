package com.example.recetario;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PuntoApiService {

    @POST("api/puntos")
    Call<Punto.PuntoResponseDto> crearPunto(@Body Punto.PuntoRequestDto puntoRequest);

    // 🌟 NUEVO: Llama al endpoint de filtrado del backend
    @GET("api/puntos/tipo/{tipo}")
    Call<List<Punto.PuntoResponseDto>> buscarPorTipo(@Path("tipo") String tipo);
    @GET("api/puntos/usuario/{usuarioId}")
    Call<List<Punto.PuntoResponseDto>> buscaPuntosUser(@Path("usuarioId") String tipo);
}