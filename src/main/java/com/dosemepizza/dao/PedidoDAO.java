package com.dosemepizza.dao;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.modelo.DetallePedido;
import com.dosemepizza.modelo.enums.EstatusPedido;
import com.dosemepizza.modelo.Pedido;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private static final String SQL_REGISTRAR_PEDIDO = "INSERT INTO pedidos (id_cliente, fecha_pedido, total, estatus) VALUES (?, ?, ?, ?)";
    private static final String SQL_REGISTRAR_DETALLE = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
    private static final String SQL_BORRAR_DETALLES = "DELETE FROM detalle_pedido WHERE id_pedido = ?";
    private static final String SQL_ACTUALIZAR_TOTAL = "UPDATE pedidos SET total = ? WHERE id_pedido = ?";
    private static final String SQL_CAMBIAR_ESTATUS = "UPDATE pedidos SET estatus = ? WHERE id_pedido = ?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.total, p.estatus, u.nombre, u.apellidos, u.telefono FROM pedidos p INNER JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.id_pedido = ?";
    private static final String SQL_LISTAR_DETALLES = "SELECT dp.id_detalle, dp.id_pedido, dp.id_producto, dp.cantidad, dp.precio_unitario, p.codigo, p.nombre, p.descripcion, p.restricciones FROM detalle_pedido dp INNER JOIN productos p ON dp.id_producto = p.id_producto WHERE dp.id_pedido = ?";
    private static final String SQL_LISTAR = "SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.total, p.estatus, u.nombre, u.apellidos, u.telefono FROM pedidos p INNER JOIN usuarios u ON p.id_cliente = u.id_usuario ORDER BY p.fecha_pedido DESC";
    private static final String SQL_BUSCAR_POR_CLIENTE = "SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.total, p.estatus, u.nombre, u.apellidos, u.telefono FROM pedidos p INNER JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.id_cliente = ? ORDER BY p.fecha_pedido DESC";
    private static final String SQL_BUSCAR_POR_FECHA = "SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.total, p.estatus, u.nombre, u.apellidos, u.telefono FROM pedidos p INNER JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.fecha_pedido BETWEEN ? AND ? ORDER BY p.fecha_pedido DESC";
    private static final String SQL_BUSCAR_POR_ESTATUS = "SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.total, p.estatus, u.nombre, u.apellidos, u.telefono FROM pedidos p INNER JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.estatus = ? ORDER BY p.fecha_pedido DESC";

    public int registrar(Pedido pedido) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener()) {
            connection.setAutoCommit(false);
            try {
                pedido.recalcularTotal();

                int idPedido;
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setInt(1, pedido.getIdCliente());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(pedido.getFechaPedido()));
                    preparedStatement.setDouble(3, pedido.getTotal());
                    preparedStatement.setString(4, pedido.getEstatus().name());
                    preparedStatement.executeUpdate();

                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (!resultSet.next()) {
                            throw new SQLException("No se genero el id del pedido");
                        }
                        idPedido = resultSet.getInt(1);
                        pedido.setIdPedido(idPedido);
                    }
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR_DETALLE)) {
                    for (DetallePedido detalle : pedido.getDetalles()) {
                        detalle.setIdPedido(idPedido);
                        preparedStatement.setInt(1, idPedido);
                        preparedStatement.setInt(2, detalle.getIdProducto());
                        preparedStatement.setInt(3, detalle.getCantidad());
                        preparedStatement.setDouble(4, detalle.getPrecioUnitario());
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                }

                connection.commit();
                return idPedido;

            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al registrar el pedido", ex);
        }
    }

    public void actualizarDetalles(Pedido pedido) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener()) {
            connection.setAutoCommit(false);
            try {
                pedido.recalcularTotal();

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_BORRAR_DETALLES)) {
                    preparedStatement.setInt(1, pedido.getIdPedido());
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR_DETALLE)) {
                    for (DetallePedido detalle : pedido.getDetalles()) {
                        detalle.setIdPedido(pedido.getIdPedido());
                        preparedStatement.setInt(1, pedido.getIdPedido());
                        preparedStatement.setInt(2, detalle.getIdProducto());
                        preparedStatement.setInt(3, detalle.getCantidad());
                        preparedStatement.setDouble(4, detalle.getPrecioUnitario());
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_ACTUALIZAR_TOTAL)) {
                    preparedStatement.setDouble(1, pedido.getTotal());
                    preparedStatement.setInt(2, pedido.getIdPedido());
                    preparedStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al actualizar el pedido", ex);
        }
    }

    public void cambiarEstatus(int idPedido, EstatusPedido nuevoEstatus) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_CAMBIAR_ESTATUS)) {

            preparedStatement.setString(1, nuevoEstatus.name());
            preparedStatement.setInt(2, idPedido);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al cambiar el estatus del pedido", ex);
        }
    }

    public Pedido buscarPorId(int idPedido) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            preparedStatement.setInt(1, idPedido);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Pedido pedido = mapear(resultSet);
                    pedido.setDetalles(cargarDetalles(connection, idPedido));
                    return pedido;
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el pedido", ex);
        }
    }

    public List<Pedido> listar() throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_LISTAR);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<Pedido> lista = new ArrayList<>();
            while (resultSet.next()) {
                lista.add(mapear(resultSet));
            }
            return lista;

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al listar pedidos", ex);
        }
    }

    public List<Pedido> buscarPorCliente(int idCliente) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_CLIENTE)) {

            preparedStatement.setInt(1, idCliente);
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar pedidos por cliente", ex);
        }
    }

    public List<Pedido> buscarPorFecha(LocalDate desde, LocalDate hasta) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_FECHA)) {

            preparedStatement.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(hasta.atTime(23, 59, 59)));
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar pedidos por fecha", ex);
        }
    }

    public List<Pedido> buscarPorEstatus(EstatusPedido estatus) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_ESTATUS)) {

            preparedStatement.setString(1, estatus.name());
            return ejecutar(preparedStatement);

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar pedidos por estatus", ex);
        }
    }

    private List<Pedido> ejecutar(PreparedStatement preparedStatement) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                lista.add(mapear(resultSet));
            }
        }
        return lista;
    }

    private List<DetallePedido> cargarDetalles(Connection connection, int idPedido) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_LISTAR_DETALLES)) {
            preparedStatement.setInt(1, idPedido);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<DetallePedido> detalles = new ArrayList<>();
                while (resultSet.next()) {
                    DetallePedido detalle = new DetallePedido();
                    detalle.setIdDetalle(resultSet.getInt("id_detalle"));
                    detalle.setIdPedido(resultSet.getInt("id_pedido"));
                    detalle.setIdProducto(resultSet.getInt("id_producto"));
                    detalle.setCantidad(resultSet.getInt("cantidad"));
                    detalle.setPrecioUnitario(resultSet.getDouble("precio_unitario"));

                    Producto producto = new Producto();
                    producto.setIdProducto(resultSet.getInt("id_producto"));
                    producto.setCodigo(resultSet.getString("codigo"));
                    producto.setNombre(resultSet.getString("nombre"));
                    producto.setDescripcion(resultSet.getString("descripcion"));
                    producto.setRestricciones(resultSet.getString("restricciones"));
                    producto.setPrecio(detalle.getPrecioUnitario());
                    detalle.setProducto(producto);

                    detalles.add(detalle);
                }
                return detalles;
            }
        }
    }

    private Pedido mapear(ResultSet resultSet) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(resultSet.getInt("id_pedido"));
        pedido.setIdCliente(resultSet.getInt("id_cliente"));
        Timestamp fecha = resultSet.getTimestamp("fecha_pedido");
        if (fecha != null) {
            pedido.setFechaPedido(fecha.toLocalDateTime());
        }
        pedido.setTotal(resultSet.getDouble("total"));
        pedido.setEstatus(EstatusPedido.valueOf(resultSet.getString("estatus")));

        Usuario cliente = new Usuario();
        cliente.setIdUsuario(resultSet.getInt("id_cliente"));
        cliente.setNombre(resultSet.getString("nombre"));
        cliente.setApellidos(resultSet.getString("apellidos"));
        cliente.setTelefono(resultSet.getString("telefono"));
        pedido.setCliente(cliente);

        return pedido;
    }
}