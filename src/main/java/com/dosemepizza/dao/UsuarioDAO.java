package com.dosemepizza.dao;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.modelo.enums.TipoUsuario;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String SQL_REGISTRAR = "INSERT INTO usuarios (nombre, apellidos, telefono, email, calle, numero, codigo_postal, ciudad, tipo, estatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CLIENTE', 1)";
    private static final String SQL_EDITAR = "UPDATE usuarios SET nombre = ?, apellidos = ?, telefono = ?, email = ?, calle = ?, numero = ?, codigo_postal = ?, ciudad = ? WHERE id_usuario = ?";
    private static final String SQL_ELIMINAR = "UPDATE usuarios SET estatus = 0 WHERE id_usuario = ?";
    private static final String SQL_TIENE_PEDIDOS = "SELECT COUNT(*) FROM pedidos WHERE id_cliente = ?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT * FROM usuarios WHERE id_usuario = ? AND estatus = 1";
    private static final String SQL_LISTAR_CLIENTES = "SELECT * FROM usuarios WHERE tipo = 'CLIENTE' AND estatus = 1 ORDER BY nombre";
    private static final String SQL_BUSCAR_POR_NOMBRE = "SELECT * FROM usuarios WHERE tipo = 'CLIENTE' AND estatus = 1 AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(apellidos) LIKE LOWER(?)) ORDER BY nombre";
    private static final String SQL_BUSCAR_POR_TELEFONO = "SELECT * FROM usuarios WHERE tipo = 'CLIENTE' AND estatus = 1 AND telefono LIKE ? ORDER BY nombre";
    private static final String SQL_BUSCAR_POR_DIRECCION = "SELECT * FROM usuarios WHERE tipo = 'CLIENTE' AND estatus = 1 AND (LOWER(calle) LIKE LOWER(?) OR LOWER(ciudad) LIKE LOWER(?)) ORDER BY nombre";

    public int registrar(Usuario usuario) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellidos());
            preparedStatement.setString(3, usuario.getTelefono());
            preparedStatement.setString(4, usuario.getEmail());
            preparedStatement.setString(5, usuario.getCalle());
            preparedStatement.setString(6, usuario.getNumero());
            preparedStatement.setString(7, usuario.getCodigoPostal());
            preparedStatement.setString(8, usuario.getCiudad());
            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    usuario.setIdUsuario(id);
                    return id;
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al registrar el cliente", ex);
        }
    }

    public void editar(Usuario usuario) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EDITAR)) {

            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellidos());
            preparedStatement.setString(3, usuario.getTelefono());
            preparedStatement.setString(4, usuario.getEmail());
            preparedStatement.setString(5, usuario.getCalle());
            preparedStatement.setString(6, usuario.getNumero());
            preparedStatement.setString(7, usuario.getCodigoPostal());
            preparedStatement.setString(8, usuario.getCiudad());
            preparedStatement.setInt(9, usuario.getIdUsuario());
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al editar el cliente", ex);
        }
    }

    public void eliminar(int idUsuario) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ELIMINAR)) {

            preparedStatement.setInt(1, idUsuario);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al eliminar el cliente", ex);
        }
    }

    public boolean tienePedidos(int idUsuario) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_TIENE_PEDIDOS)) {

            preparedStatement.setInt(1, idUsuario);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al verificar los pedidos del cliente", ex);
        }
    }

    public Usuario buscarPorId(int idUsuario) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            preparedStatement.setInt(1, idUsuario);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el cliente", ex);
        }
    }

    public List<Usuario> listarClientes() throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_LISTAR_CLIENTES);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<Usuario> lista = new ArrayList<>();
            while (resultSet.next()) {
                lista.add(mapear(resultSet));
            }
            return lista;

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al listar clientes", ex);
        }
    }

    public List<Usuario> buscarPorNombre(String texto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            String patron = "%" + texto + "%";
            preparedStatement.setString(1, patron);
            preparedStatement.setString(2, patron);
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar clientes por nombre", ex);
        }
    }

    public List<Usuario> buscarPorTelefono(String telefono) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_TELEFONO)) {

            preparedStatement.setString(1, "%" + telefono + "%");
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar clientes por telefono", ex);
        }
    }

    public List<Usuario> buscarPorDireccion(String texto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_DIRECCION)) {

            String patron = "%" + texto + "%";
            preparedStatement.setString(1, patron);
            preparedStatement.setString(2, patron);
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar clientes por direccion", ex);
        }
    }

    private List<Usuario> ejecutar(PreparedStatement preparedStatement) throws SQLException {
        List<Usuario> resultado = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                resultado.add(mapear(resultSet));
            }
        }
        return resultado;
    }

    private Usuario mapear(ResultSet resultSet) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultSet.getInt("id_usuario"));
        usuario.setNombre(resultSet.getString("nombre"));
        usuario.setApellidos(resultSet.getString("apellidos"));
        usuario.setTelefono(resultSet.getString("telefono"));
        usuario.setEmail(resultSet.getString("email"));
        usuario.setCalle(resultSet.getString("calle"));
        usuario.setNumero(resultSet.getString("numero"));
        usuario.setCodigoPostal(resultSet.getString("codigo_postal"));
        usuario.setCiudad(resultSet.getString("ciudad"));
        usuario.setTipo(TipoUsuario.valueOf(resultSet.getString("tipo")));
        usuario.setEstatus(resultSet.getBoolean("estatus"));
        Timestamp fecha = resultSet.getTimestamp("fecha_registro");
        if (fecha != null) {
            usuario.setFechaRegistro(fecha.toLocalDateTime());
        }
        return usuario;
    }
}