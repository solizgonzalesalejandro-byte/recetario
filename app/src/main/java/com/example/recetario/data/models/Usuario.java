package com.example.recetario;

public class Usuario {
    // Esta clase funciona como contenedor principal público

    // 1. Mapea exactamente a tu formato JSON de registro
    public static class UsuarioRequestDto {
        private String nombre;
        private String correo;
        private String password;

        public UsuarioRequestDto(String nombre, String correo, String password) {
            this.nombre = nombre;
            this.correo = correo;
            this.password = password;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCorreo() { return correo; }
        public void set自由Correo(String correo) { this.correo = correo; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 2. Mapea exactamente a tu UsuarioResponseDto del backend
    public static class UsuarioResponseDto {
        private String id;
        private String nombre;
        private String correo;
        private String rol;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }

        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
    }

    // 3. Mapea exactamente a tu LoginRequestDto del backend
    public static class LoginRequestDto {
        private String correo;
        private String password;

        public LoginRequestDto(String correo, String password) {
            this.correo = correo;
            this.password = password;
        }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}