package com.example.recetario.data.services

import com.example.recetario.data.models.UsuarioRequest
import com.example.recetario.data.models.UsuarioResponse
import retrofit2.Response
import retrofit2.http.*

interface UsuarioApiService {

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioResponse>>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(
        @Path("id") id: String
    ): Response<UsuarioResponse>

    @GET("api/usuarios/correo/{correo}")
    suspend fun buscarUsuarioPorCorreo(
        @Path("correo") correo: String
    ): Response<UsuarioResponse>

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Body usuario: UsuarioRequest
    ): Response<UsuarioResponse>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: String,
        @Body usuario: UsuarioRequest
    ): Response<UsuarioResponse>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: String
    ): Response<Unit>
}