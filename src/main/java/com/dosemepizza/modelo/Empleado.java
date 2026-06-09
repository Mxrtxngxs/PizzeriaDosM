package com.dosemepizza.modelo;

import com.dosemepizza.modelo.enums.RolEmpleado;
import com.dosemepizza.modelo.enums.TipoUsuario;

public class Empleado extends Usuario {

    private int idEmpleado;
    private String username;
    private String passwordHash;
    private RolEmpleado rol;

    public Empleado() {
        super();
        setTipo(TipoUsuario.EMPLEADO);
        this.rol = RolEmpleado.CAJERO;
    }

    public Empleado(String nombre, String apellidos, String telefono, String email,
                    String calle, String numero, String codigoPostal, String ciudad,
                    String username, String passwordHash, RolEmpleado rol) {
        super(nombre, apellidos, telefono, email, calle, numero, codigoPostal, ciudad,
                TipoUsuario.EMPLEADO);
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolEmpleado getRol() {
        return rol;
    }

    public void setRol(RolEmpleado rol) {
        this.rol = rol;
    }

    public boolean esAdministrador() {
        return rol == RolEmpleado.ADMINISTRADOR;
    }

    public boolean esCajero() {
        return rol == RolEmpleado.CAJERO;
    }
}