package com.example.recetario;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Punto {

    // 1. DTO que se envía al Servidor (PuntoRequestDto)
    public static class PuntoRequestDto {
        private String nombre;
        private String tipo;
        private String descripcion;
        private String direccion;
        private Double lat;
        private Double lng;
        private String horario;
        private String telefono;
        private String whatsapp;
        private List<String> materiales;
        private List<RecompensaDto> recompensas;
        private String usuarioId;
        private List<String> imagenes;
        private List<RedDto> redes;

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
        public String getHorario() { return horario; }
        public void setHorario(String horario) { this.horario = horario; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getWhatsapp() { return whatsapp; }
        public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
        public List<String> getMateriales() { return materiales; }
        public void setMateriales(List<String> materiales) { this.materiales = materiales; }
        public List<RecompensaDto> getRecompensas() { return recompensas; }
        public void setRecompensas(List<RecompensaDto> recompensas) { this.recompensas = recompensas; }
        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
        public List<String> getImagenes() { return imagenes; }
        public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }
        public List<RedDto> getRedes() { return redes; }
        public void setRedes(List<RedDto> redes) { this.redes = redes; }
    }

    // 2. DTO que el Servidor responde (PuntoResponseDto)
    public static class PuntoResponseDto {

        @SerializedName("id") // Asegura el mapeo correcto del ID
        private String id;

        private String nombre;
        private String tipo;
        private String descripcion;
        private String direccion;
        private Double lat;
        private Double lng;
        private String horario;
        private String telefono;
        private String whatsapp;
        private List<String> materiales;
        private List<RecompensaDto> recompensas;
        private String usuarioId;
        private List<String> imagenes;
        private List<RedDto> redes;

        // --- GETTERS Y SETTERS ---

        // ¡IMPORTANTE!: Este setter permite que Retrofit/Gson asigne el ID
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
        public String getDireccion() { return direccion; }
        public Double getLat() { return lat; }
        public Double getLng() { return lng; }
        public String getHorario() { return horario; }
        public String getTelefono() { return telefono; }
        public String getWhatsapp() { return whatsapp; }
        public List<String> getMateriales() { return materiales; }
        public List<RecompensaDto> getRecompensas() { return recompensas; }
        public String getUsuarioId() { return usuarioId; }
        public List<String> getImagenes() { return imagenes; }
        public List<RedDto> getRedes() { return redes; }
    }

    // 3. Sub-Objeto RecompensaDto
    public static class RecompensaDto {
        private String nombre;
        private String descripcion;
        private Integer stock;
        private String estado;

        public RecompensaDto(String nombre, String descripcion, Integer stock, String estado) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.stock = stock;
            this.estado = estado;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }

    // 4. Sub-Objeto RedDto
    public static class RedDto {
        private String nombre;
        private String enlace;

        public RedDto(String nombre, String enlace) {
            this.nombre = nombre;
            this.enlace = enlace;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEnlace() { return enlace; }
        public void setEnlace(String enlace) { this.enlace = enlace; }
    }
}