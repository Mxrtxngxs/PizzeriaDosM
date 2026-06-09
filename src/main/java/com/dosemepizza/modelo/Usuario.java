package com.dosemepizza.modelo;

import com.dosemepizza.modelo.enums.TipoUsuario;

import java.time.LocalDateTime;

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String calle;
    private String numero;
    private String codigoPostal;
    private String ciudad;
    private TipoUsuario tipo;
    private boolean estatus;
    private LocalDateTime fechaRegistro;

    public Usuario() {
        this.estatus = true;
        this.tipo = TipoUsuario.CLIENTE;
    }

    public Usuario(String nombre, String apellidos, String telefono, String email,
                   String calle, String numero, String codigoPostal, String ciudad,
                   TipoUsuario tipo) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.calle = calle;
        this.numero = numero;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.tipo = tipo;
        this.estatus = true;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
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

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public boolean isEstatus() {
        return estatus;
    }

    public void setEstatus(boolean estatus) {
        this.estatus = estatus;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getDireccion() {
        return calle + " " + numero + ", " + ciudad + ", CP " + codigoPostal;
    }

    public boolean esCliente() {
        return tipo == TipoUsuario.CLIENTE;
    }

    public boolean esEmpleado() {
        return tipo == TipoUsuario.EMPLEADO;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}