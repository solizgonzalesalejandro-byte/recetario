package com.example.recetario;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ResenasApiService {

    // Obtener el documento contenedor con la lista de reseñas de un punto específico
    @GET("api/resenias/punto/{puntoId}")
    Call<Resenia.ReseniasPuntoResponseDto> obtenerReseniasPorPuntoId(@Path("puntoId") String puntoId);

    // Agregar una reseña individual a un punto ecológico existente
    @POST("api/resenias/punto/{puntoId}/agregar")
    Call<Resenia.ReseniasPuntoResponseDto> agregarResenia(
            @Path("puntoId") String puntoId,
            @Body Resenia.ReseniaRequestDto reseniaRequest
    );

    // Obtener el historial completo de reseñas escritas por un usuario
    @GET("api/resenias/usuario/{usuarioId}")
    Call<List<Resenia.ReseniaResponseDto>> obtenerReseniasPorUsuarioId(@Path("usuarioId") String usuarioId);
}