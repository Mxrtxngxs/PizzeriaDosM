package com.dosemepizza.servicios;

import com.dosemepizza.dao.UsuarioDAO;
import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.UsuarioConPedidosException;
import com.dosemepizza.modelo.enums.TipoUsuario;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.util.Validador;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public int registrarCliente(Usuario usuario) throws DatosInvalidosException, AccesoADatosException {
        validar(usuario);
        usuario.setTipo(TipoUsuario.CLIENTE);
        usuario.setEstatus(true);
        return usuarioDAO.registrar(usuario);
    }

    public void editarCliente(Usuario usuario) throws DatosInvalidosException, AccesoADatosException {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            throw new DatosInvalidosException("El cliente que intenta editar no existe");
        }
        validar(usuario);
        Usuario existente = usuarioDAO.buscarPorId(usuario.getIdUsuario());
        if (existente == null) {
            throw new DatosInvalidosException("El cliente no existe en el sistema");
        }
        usuarioDAO.editar(usuario);
    }

    public void eliminarCliente(int idUsuario)
            throws DatosInvalidosException, UsuarioConPedidosException, AccesoADatosException {

        Usuario existente = usuarioDAO.buscarPorId(idUsuario);
        if (existente == null) {
            throw new DatosInvalidosException("El cliente no existe en el sistema");
        }
        if (usuarioDAO.tienePedidos(idUsuario)) {
            throw new UsuarioConPedidosException("No se puede eliminar el cliente porque tiene pedidos registrados");
        }
        usuarioDAO.eliminar(idUsuario);
    }

    public Usuario buscarPorId(int idUsuario) throws AccesoADatosException {
        return usuarioDAO.buscarPorId(idUsuario);
    }

    public List<Usuario> listarClientes() throws AccesoADatosException {
        return usuarioDAO.listarClientes();
    }

    public List<Usuario> buscarPorNombre(String texto) throws DatosInvalidosException, AccesoADatosException {
        Validador.validarObligatorio(texto, "texto de busqueda");
        return usuarioDAO.buscarPorNombre(texto.trim());
    }

    public List<Usuario> buscarPorTelefono(String telefono) throws DatosInvalidosException, AccesoADatosException {
        Validador.validarObligatorio(telefono, "telefono");
        return usuarioDAO.buscarPorTelefono(telefono.trim());
    }

    public List<Usuario> buscarPorDireccion(String texto) throws DatosInvalidosException, AccesoADatosException {
        Validador.validarObligatorio(texto, "direccion");
        return usuarioDAO.buscarPorDireccion(texto.trim());
    }

    private void validar(Usuario usuario) throws DatosInvalidosException {
        if (usuario == null) {
            throw new DatosInvalidosException("El cliente no puede estar vacio");
        }
        Validador.validarObligatorio(usuario.getNombre(), "nombre");
        Validador.validarObligatorio(usuario.getApellidos(), "apellidos");
        Validador.validarTelefono(usuario.getTelefono());
        Validador.validarEmail(usuario.getEmail());
        Validador.validarObligatorio(usuario.getCalle(), "calle");
        Validador.validarObligatorio(usuario.getNumero(), "numero");
        Validador.validarCodigoPostal(usuario.getCodigoPostal());
        Validador.validarObligatorio(usuario.getCiudad(), "ciudad");
    }
}