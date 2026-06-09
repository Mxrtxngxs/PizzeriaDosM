package com.dosemepizza.servicios;

import com.dosemepizza.dao.EmpleadoDAO;
import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.CredencialesInvalidasException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.RegistroDuplicadoException;
import com.dosemepizza.modelo.Empleado;
import com.dosemepizza.modelo.enums.TipoUsuario;
import com.dosemepizza.util.Encriptador;
import com.dosemepizza.util.Sesion;
import com.dosemepizza.util.Validador;

import java.util.List;

public class EmpleadoService {

    private final EmpleadoDAO empleadoDAO;

    public EmpleadoService() {
        this.empleadoDAO = new EmpleadoDAO();
    }

    public int registrarEmpleado(Empleado empleado, String passwordPlana)
            throws DatosInvalidosException, RegistroDuplicadoException, AccesoADatosException {

        validar(empleado);
        Validador.validarUsername(empleado.getUsername());
        Validador.validarPassword(passwordPlana);
        if (empleado.getRol() == null) {
            throw new DatosInvalidosException("El rol del empleado es obligatorio");
        }

        if (empleadoDAO.existeUsername(empleado.getUsername())) {
            throw new RegistroDuplicadoException("El username ya esta registrado en el sistema");
        }

        empleado.setTipo(TipoUsuario.EMPLEADO);
        empleado.setPasswordHash(Encriptador.encriptar(passwordPlana));
        empleado.setEstatus(true);
        return empleadoDAO.registrar(empleado);
    }

    public void editarEmpleado(Empleado empleado)
            throws DatosInvalidosException, RegistroDuplicadoException, AccesoADatosException {

        if (empleado == null || empleado.getIdEmpleado() <= 0) {
            throw new DatosInvalidosException("El empleado que intenta editar no existe");
        }
        validar(empleado);
        Validador.validarUsername(empleado.getUsername());
        if (empleado.getRol() == null) {
            throw new DatosInvalidosException("El rol del empleado es obligatorio");
        }

        Empleado existente = empleadoDAO.buscarPorId(empleado.getIdEmpleado());
        if (existente == null) {
            throw new DatosInvalidosException("El empleado no existe en el sistema");
        }

        if (!existente.getUsername().equals(empleado.getUsername())
                && empleadoDAO.existeUsername(empleado.getUsername())) {
            throw new RegistroDuplicadoException("El username ya esta registrado en el sistema");
        }

        empleadoDAO.editar(empleado);
    }

    public void cambiarPassword(int idEmpleado, String passwordActual, String passwordNueva)
            throws DatosInvalidosException, CredencialesInvalidasException, AccesoADatosException {

        Validador.validarObligatorio(passwordActual, "contrasena actual");
        Validador.validarPassword(passwordNueva);

        Empleado empleado = empleadoDAO.buscarPorId(idEmpleado);
        if (empleado == null) {
            throw new DatosInvalidosException("El empleado no existe en el sistema");
        }
        if (!Encriptador.verificar(passwordActual, empleado.getPasswordHash())) {
            throw new CredencialesInvalidasException("La contrasena actual no es correcta");
        }

        empleadoDAO.cambiarPassword(idEmpleado, Encriptador.encriptar(passwordNueva));
    }

    public void eliminarEmpleado(int idEmpleado) throws DatosInvalidosException, AccesoADatosException {
        Empleado existente = empleadoDAO.buscarPorId(idEmpleado);
        if (existente == null) {
            throw new DatosInvalidosException("El empleado no existe en el sistema");
        }

        Empleado activo = Sesion.getEmpleadoActivo();
        if (activo != null && activo.getIdEmpleado() == idEmpleado) {
            throw new DatosInvalidosException("No puede eliminar la cuenta con la que esta trabajando");
        }

        empleadoDAO.eliminar(idEmpleado);
    }

    public Empleado buscarPorId(int idEmpleado) throws AccesoADatosException {
        return empleadoDAO.buscarPorId(idEmpleado);
    }

    public List<Empleado> listar() throws AccesoADatosException {
        return empleadoDAO.listar();
    }

    private void validar(Empleado empleado) throws DatosInvalidosException {
        if (empleado == null) {
            throw new DatosInvalidosException("El empleado no puede estar vacio");
        }
        Validador.validarObligatorio(empleado.getNombre(), "nombre");
        Validador.validarObligatorio(empleado.getApellidos(), "apellidos");
        Validador.validarTelefono(empleado.getTelefono());
        Validador.validarEmail(empleado.getEmail());
        Validador.validarObligatorio(empleado.getCalle(), "calle");
        Validador.validarObligatorio(empleado.getNumero(), "numero");
        Validador.validarCodigoPostal(empleado.getCodigoPostal());
        Validador.validarObligatorio(empleado.getCiudad(), "ciudad");
    }
}