package com.example.recetario;

public class PuntoEcologico {
    // Atributos (es mejor ponerlos private por seguridad)
    private String nombre;
    private String tipo;
    private String descripcion;
    private String contacto;
    private String enlaceWhatsapp; // Agregado según tus requerimientos
    private double latitud;
    private double longitud;
    private String fotoUrl;

    // 1. Constructor vacío (necesario si luego usas Firebase o bases de datos)
    public PuntoEcologico() {
    }

    // 2. Constructor con todos los parámetros
    public PuntoEcologico(String nombre, String tipo, String descripcion, String contacto,
                          String enlaceWhatsapp, double latitud, double longitud, String fotoUrl) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.contacto = contacto;
        this.enlaceWhatsapp = enlaceWhatsapp;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fotoUrl = fotoUrl;
    }

    // 3. Métodos Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getEnlaceWhatsapp() { return enlaceWhatsapp; }
    public void setEnlaceWhatsapp(String enlaceWhatsapp) { this.enlaceWhatsapp = enlaceWhatsapp; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    // Método extra útil para depuración (imprime los datos en consola)
    @Override
    public String toString() {
        return "Punto: " + nombre + " (" + tipo + ")";
    }
}
