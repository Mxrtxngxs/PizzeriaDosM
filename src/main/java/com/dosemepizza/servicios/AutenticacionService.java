package com.dosemepizza.servicios;

import com.dosemepizza.dao.EmpleadoDAO;
import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.AutenticacionException;
import com.dosemepizza.excepciones.CredencialesInvalidasException;
import com.dosemepizza.modelo.Empleado;
import com.dosemepizza.util.Encriptador;

public class AutenticacionService {

    private final EmpleadoDAO empleadoDAO;

    public AutenticacionService() {
        this.empleadoDAO = new EmpleadoDAO();
    }

    public Empleado iniciarSesion(String username, String passwordPlana)
            throws AutenticacionException, CredencialesInvalidasException {

        if (username == null || username.trim().isEmpty()) {
            throw new CredencialesInvalidasException("El usuario es obligatorio");
        }
        if (passwordPlana == null || passwordPlana.isEmpty()) {
            throw new CredencialesInvalidasException("La contrasena es obligatoria");
        }

        Empleado empleado;
        try {
            empleado = empleadoDAO.buscarPorUsername(username.trim());
        } catch (AccesoADatosException ex) {
            throw new AutenticacionException("Error al conectar con la base de datos");
        }

        if (empleado == null) {
            throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
        }

        if (!Encriptador.verificar(passwordPlana, empleado.getPasswordHash())) {
            throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
        }

        return empleado;
    }
}