package com.example.gestorxpress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tarea {
    private String titulo;
    private String descripcion;
    private Date fechaHora;
    private String prioridad;
    private String estado;

    public Tarea(String titulo, String descripcion, Date fechaHora, String prioridad, String estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.prioridad = prioridad;
        this.estado = estado;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Date getFechaHora() { return fechaHora; }
    public String getPrioridad() { return prioridad; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return titulo + " | " + prioridad + " | " + estado + "\n" + descripcion + "\nFecha: " + sdf.format(fechaHora);
    }

}
