package com.ampeliodev.chatbottwiliospringboot.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
@Entity
@Table(name="cita")
public class EntidadCita implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name = "fecha")
    private java.time.LocalDate fecha;
    @Column(name="hora")
    private java.time.LocalTime hora;
    @Column(name="disponible")
    private Boolean disponible;
    @Column(name="nombre")
    private String nombre;
    @Column(name="apellido")
    private String apellido;
    @Column(name="telefono")
    private String telefono;
    @Column(name="email")
    private String email;
    @Column(name="servicio")
    private String servicio;

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
}
