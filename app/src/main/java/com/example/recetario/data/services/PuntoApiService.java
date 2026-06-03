package com.example.recetario;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PuntoApiService {

    @POST("api/puntos")
    Call<Punto.PuntoResponseDto> crearPunto(@Body Punto.PuntoRequestDto puntoRequest);

    // 🌟 NUEVO: Llama al endpoint de filtrado del backend
    @GET("api/puntos/tipo/{tipo}")
    Call<List<Punto.PuntoResponseDto>> buscarPorTipo(@Path("tipo") String tipo);
    @GET("api/puntos/usuario/{usuarioId}")
    Call<List<Punto.PuntoResponseDto>> buscaPuntosUser(@Path("usuarioId") String tipo);

    @PUT("api/puntos/usuario/{id}")
    Call<Punto.PuntoResponseDto> actualizarPunto(
            @Path("id") String puntoId,
            @Query("usuarioId") String usuarioId,
            @Body Punto.PuntoRequestDto puntoRequest
    );

    @DELETE("api/puntos/{id}")
    Call<Punto.PuntoResponseDto> eliminarPunto(
            @Path("id") String puntoId);

}