package com.example.recetario.data.models

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String
)

data class UsuarioResponse(
    val id: String?,
    val nombre: String,
    val correo: String,
    val rol: String
)