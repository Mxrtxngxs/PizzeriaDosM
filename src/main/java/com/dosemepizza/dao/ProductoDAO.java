package com.dosemepizza.dao;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SQL_REGISTRAR = "INSERT INTO productos (codigo, nombre, descripcion, precio, restricciones, foto, cantidad, estatus) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
    private static final String SQL_EDITAR = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, restricciones = ?, foto = ?, cantidad = ? WHERE id_producto = ?";
    private static final String SQL_ELIMINAR = "UPDATE productos SET estatus = 0 WHERE id_producto = ?";
    private static final String SQL_TIENE_PEDIDOS = "SELECT COUNT(*) FROM detalle_pedido WHERE id_producto = ?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT * FROM productos WHERE id_producto = ? AND estatus = 1";
    private static final String SQL_BUSCAR_POR_CODIGO = "SELECT * FROM productos WHERE codigo = ? AND estatus = 1";
    private static final String SQL_BUSCAR_POR_NOMBRE = "SELECT * FROM productos WHERE estatus = 1 AND LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
    private static final String SQL_LISTAR = "SELECT * FROM productos WHERE estatus = 1 ORDER BY nombre";
    private static final String SQL_EXISTE_CODIGO = "SELECT COUNT(*) FROM productos WHERE codigo = ?";
    private static final String SQL_ACTUALIZAR_CANTIDAD = "UPDATE productos SET cantidad = ? WHERE id_producto = ?";

    public int registrar(Producto producto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_REGISTRAR, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, producto.getCodigo());
            preparedStatement.setString(2, producto.getNombre());
            preparedStatement.setString(3, producto.getDescripcion());
            preparedStatement.setDouble(4, producto.getPrecio());
            preparedStatement.setString(5, producto.getRestricciones());
            preparedStatement.setBytes(6, producto.getFoto());
            preparedStatement.setInt(7, producto.getCantidad());
            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    producto.setIdProducto(id);
                    return id;
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al registrar el producto", ex);
        }
    }

    public void editar(Producto producto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EDITAR)) {

            preparedStatement.setString(1, producto.getNombre());
            preparedStatement.setString(2, producto.getDescripcion());
            preparedStatement.setDouble(3, producto.getPrecio());
            preparedStatement.setString(4, producto.getRestricciones());
            preparedStatement.setBytes(5, producto.getFoto());
            preparedStatement.setInt(6, producto.getCantidad());
            preparedStatement.setInt(7, producto.getIdProducto());
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al editar el producto", ex);
        }
    }

    public void eliminar(int idProducto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ELIMINAR)) {

            preparedStatement.setInt(1, idProducto);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al eliminar el producto", ex);
        }
    }

    public boolean tienePedidos(int idProducto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_TIENE_PEDIDOS)) {

            preparedStatement.setInt(1, idProducto);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al verificar el uso del producto", ex);
        }
    }

    public Producto buscarPorId(int idProducto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            preparedStatement.setInt(1, idProducto);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el producto", ex);
        }
    }

    public Producto buscarPorCodigo(String codigo) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {

            preparedStatement.setString(1, codigo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar el producto por codigo", ex);
        }
    }

    public List<Producto> buscarPorNombre(String texto) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            preparedStatement.setString(1, "%" + texto + "%");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Producto> lista = new ArrayList<>();
                while (resultSet.next()) {
                    lista.add(mapear(resultSet));
                }
                return lista;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al buscar productos por nombre", ex);
        }
    }

    public List<Producto> listar() throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_LISTAR);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<Producto> lista = new ArrayList<>();
            while (resultSet.next()) {
                lista.add(mapear(resultSet));
            }
            return lista;

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al listar productos", ex);
        }
    }

    public boolean existeCodigo(String codigo) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EXISTE_CODIGO)) {

            preparedStatement.setString(1, codigo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al verificar el codigo", ex);
        }
    }

    public void actualizarCantidad(int idProducto, int nuevaCantidad) throws AccesoADatosException {
        try (Connection connection = ConexionBD.obtener();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ACTUALIZAR_CANTIDAD)) {

            preparedStatement.setInt(1, nuevaCantidad);
            preparedStatement.setInt(2, idProducto);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new AccesoADatosException("Error al actualizar la cantidad", ex);
        }
    }

    private Producto mapear(ResultSet resultSet) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(resultSet.getInt("id_producto"));
        producto.setCodigo(resultSet.getString("codigo"));
        producto.setNombre(resultSet.getString("nombre"));
        producto.setDescripcion(resultSet.getString("descripcion"));
        producto.setPrecio(resultSet.getDouble("precio"));
        producto.setRestricciones(resultSet.getString("restricciones"));
        producto.setFoto(resultSet.getBytes("foto"));
        producto.setCantidad(resultSet.getInt("cantidad"));
        producto.setEstatus(resultSet.getBoolean("estatus"));
        return producto;
    }
}