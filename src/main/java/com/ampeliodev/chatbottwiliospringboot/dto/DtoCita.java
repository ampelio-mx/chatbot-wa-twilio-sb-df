package com.ampeliodev.chatbottwiliospringboot.dto;

import com.ampeliodev.chatbottwiliospringboot.domain.EntidadCita;
import jakarta.persistence.Column;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DtoCita {

    private Long id;
    private java.time.LocalDate fecha;
    private java.time.LocalTime hora;
    private Boolean disponible;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String servicio;
    private List<EntidadCita> listaHorariosDisponibles;
    private String etapa;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public List<EntidadCita> getListaHorariosDisponibles() {
        return listaHorariosDisponibles;
    }

    public void setListaHorariosDisponibles(List<EntidadCita> listaHorariosDisponibles) {
        this.listaHorariosDisponibles = listaHorariosDisponibles;
    }

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        etapa = etapa;
    }
}
