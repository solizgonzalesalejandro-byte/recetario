package com.example.recetario;

import java.util.List;

public class Resenia {

    // 1. Mapea exactamente a ReseniasPuntoResponseDto
    public static class ReseniasPuntoResponseDto {
        private String id;
        private String puntoId;
        private List<ReseniaResponseDto> resenias;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPuntoId() { return puntoId; }
        public void setPuntoId(String puntoId) { this.puntoId = puntoId; }
        public List<ReseniaResponseDto> getResenias() { return resenias; }
        public void setResenias(List<ReseniaResponseDto> resenias) { this.resenias = resenias; }
    }

    // 2. Mapea exactamente a ReseniaResponseDto
    public static class ReseniaResponseDto {
        private String usuarioId;
        private Integer puntaje;
        private String comentario;
        private String fechaCreacion; // Llega como String ISO de LocalDateTime

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
        public Integer getPuntaje() { return puntaje; }
        public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    }

    // 3. Mapea exactamente a ReseniaRequestDto (Para agregar una nueva reseña)
    public static class ReseniaRequestDto {
        private String usuarioId;
        private Integer puntaje;
        private String comentario;

        public ReseniaRequestDto(String usuarioId, Integer puntaje, String comentario) {
            this.usuarioId = usuarioId;
            this.puntaje = puntaje;
            this.comentario = comentario;
        }

        public String getUsuarioId() { return usuarioId; }
        public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
        public Integer getPuntaje() { return puntaje; }
        public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }
    }
}