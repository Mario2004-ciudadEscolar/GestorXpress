package com.example.gestorxpress.ui.Tarea;

public class Tarea {

    private int id; // Agrega este atributo
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;
    private String fechaLimite;
    private int idUsuario;

    // Constructor nuevo que incluye el id
    public Tarea(int id, int idUsuario, String titulo, String descripcion, String prioridad, String estado, String fechaLimite) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
    }

    // Constructor anterior (opcional si aún lo usas)
    public Tarea(String titulo, String descripcion, String prioridad, String estado, String fechaLimite, int idUsuario) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaLimite = fechaLimite;
        this.idUsuario = idUsuario;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    @Override
    public String toString() {
        return titulo;
    }
}

