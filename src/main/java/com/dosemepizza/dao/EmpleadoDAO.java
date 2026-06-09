package com.dosemepizza.dao;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.modelo.Empleado;
import com.dosemepizza.modelo.enums.RolEmpleado;
import com.dosemepizza.modelo.enums.TipoUsuario;
import com.dosemepizza.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private static final String SQL_REGISTRAR_USUARIO = "INSERT INTO usuarios (nombre, apellidos, telefono, email, calle, numero, codigo_postal, ciudad, tipo, estatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'EMPLEADO', 1)";
    private static final String SQL_REGISTRAR_EMPLEADO = "INSERT INTO empleados (id_usuario, username, password_hash, rol) VALUES (?, ?, ?, ?)";
    private static final String SQL_EDITAR_USUARIO = "UPDATE usuarios SET nombre = ?, apellidos = ?, telefono = ?, email = ?, calle = ?, numero = ?, codigo_postal = ?, ciudad = ? WHERE id_usuario = ?";
    private static final String SQL_EDITAR_EMPLEADO = "UPDATE empleados SET username = ?, rol = ? WHERE id_empleado = ?";
    private static final String SQL_CAMBIAR_PASSWORD = "UPDATE empleados SET password_hash = ? WHERE id_empleado = ?";
    private static final String SQL_ELIMINAR = "UPDATE usuarios u INNER JOIN empleados e ON u.id_usuario = e.id_usuario SET u.estatus = 0 WHERE e.id_empleado = ?";
    private static final String SQL_BUSCAR_POR_USERNAME = "SELECT e.id_empleado, e.id_usuario, e.username, e.password_hash, e.rol, u.nombre, u.apellidos, u.telefono, u.email, u.calle, u.numero, u.codigo_postal, u.ciudad, u.tipo, u.estatus, u.fecha_registro FROM empleados e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE e.username = ? AND u.estatus = 1";
    private static final String SQL_BUSCAR_POR_ID = "SELECT e.id_empleado, e.id_usuario, e.username, e.password_hash, e.rol, u.nombre, u.apellidos, u.telefono, u.email, u.calle, u.numero, u.codigo_postal, u.ciudad, u.tipo, u.estatus, u.fecha_registro FROM empleados e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE e.id_empleado = ? AND u.estatus = 1";
    private static final String SQL_LISTAR = "SELECT e.id_empleado, e.id_usuario, e.username, e.password_hash, e.rol, u.nombre, u.apellidos, u.telefono, u.email, u.calle, u.numero, u.codigo_postal, u.ciudad, u.tipo, u.estatus, u.fecha_registro FROM empleados e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.estatus = 1 ORDER BY u.nombre";
    private static final String SQL_EXISTE_USERNAME = "SELECT COUNT(*) FROM empleados WHERE username = ?";

    public int registrar(Empleado empleado) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener()) {
            connection.setAutoCommit(false);
            try {
                int idUsuario;
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR_USUARIO, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, empleado.getNombre());
                    preparedStatement.setString(2, empleado.getApellidos());
                    preparedStatement.setString(3, empleado.getTelefono());
                    preparedStatement.setString(4, empleado.getEmail());
                    preparedStatement.setString(5, empleado.getCalle());
                    preparedStatement.setString(6, empleado.getNumero());
                    preparedStatement.setString(7, empleado.getCodigoPostal());
                    preparedStatement.setString(8, empleado.getCiudad());
                    preparedStatement.executeUpdate();

                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (!resultSet.next()) {
                            throw new SQLException("No se genero el id del usuario");
                        }
                        idUsuario = resultSet.getInt(1);
                        empleado.setIdUsuario(idUsuario);
                    }
                }

                int idEmpleado;
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR_EMPLEADO, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, idUsuario);
                    preparedStatement.setString(2, empleado.getUsername());
                    preparedStatement.setString(3, empleado.getPasswordHash());
                    preparedStatement.setString(4, empleado.getRol().name());
                    preparedStatement.executeUpdate();

                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (!resultSet.next()) {
                            throw new SQLException("No se genero el id del empleado");
                        }
                        idEmpleado = resultSet.getInt(1);
                        empleado.setIdEmpleado(idEmpleado);
                    }
                }

                connection.commit();
                return idEmpleado;

            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al registrar el empleado", ex);
        }
    }

    public void editar(Empleado empleado) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_EDITAR_USUARIO)) {
                    preparedStatement.setString(1, empleado.getNombre());
                    preparedStatement.setString(2, empleado.getApellidos());
                    preparedStatement.setString(3, empleado.getTelefono());
                    preparedStatement.setString(4, empleado.getEmail());
                    preparedStatement.setString(5, empleado.getCalle());
                    preparedStatement.setString(6, empleado.getNumero());
                    preparedStatement.setString(7, empleado.getCodigoPostal());
                    preparedStatement.setString(8, empleado.getCiudad());
                    preparedStatement.setInt(9, empleado.getIdUsuario());
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_EDITAR_EMPLEADO)) {
                    preparedStatement.setString(1, empleado.getUsername());
                    preparedStatement.setString(2, empleado.getRol().name());
                    preparedStatement.setInt(3, empleado.getIdEmpleado());
                    preparedStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al editar el empleado", ex);
        }
    }

    public void cambiarPassword(int idEmpleado, String nuevoHash) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_CAMBIAR_PASSWORD)) {

            preparedStatement.setString(1, nuevoHash);
            preparedStatement.setInt(2, idEmpleado);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al actualizar la contrasena", ex);
        }
    }

    public void eliminar(int idEmpleado) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ELIMINAR)) {

            preparedStatement.setInt(1, idEmpleado);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al eliminar el empleado", ex);
        }
    }

    public Empleado buscarPorUsername(String username) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_USERNAME)) {

            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el empleado por username", ex);
        }
    }

    public Empleado buscarPorId(int idEmpleado) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            preparedStatement.setInt(1, idEmpleado);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el empleado", ex);
        }
    }

    public List<Empleado> listar() throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_LISTAR);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<Empleado> lista = new ArrayList<>();
            while (resultSet.next()) {
                lista.add(mapear(resultSet));
            }
            return lista;

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al listar empleados", ex);
        }
    }

    public boolean existeUsername(String username) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EXISTE_USERNAME)) {

            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al verificar el username", ex);
        }
    }

    private Empleado mapear(ResultSet resultSet) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(resultSet.getInt("id_empleado"));
        empleado.setIdUsuario(resultSet.getInt("id_usuario"));
        empleado.setUsername(resultSet.getString("username"));
        empleado.setPasswordHash(resultSet.getString("password_hash"));
        empleado.setRol(RolEmpleado.valueOf(resultSet.getString("rol")));
        empleado.setNombre(resultSet.getString("nombre"));
        empleado.setApellidos(resultSet.getString("apellidos"));
        empleado.setTelefono(resultSet.getString("telefono"));
        empleado.setEmail(resultSet.getString("email"));
        empleado.setCalle(resultSet.getString("calle"));
        empleado.setNumero(resultSet.getString("numero"));
        empleado.setCodigoPostal(resultSet.getString("codigo_postal"));
        empleado.setCiudad(resultSet.getString("ciudad"));
        empleado.setTipo(TipoUsuario.valueOf(resultSet.getString("tipo")));
        empleado.setEstatus(resultSet.getBoolean("estatus"));
        Timestamp fecha = resultSet.getTimestamp("fecha_registro");
        if (fecha != null) {
            empleado.setFechaRegistro(fecha.toLocalDateTime());
        }
        return empleado;
    }
}