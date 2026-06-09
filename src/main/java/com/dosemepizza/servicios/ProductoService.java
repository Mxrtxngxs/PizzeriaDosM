package com.dosemepizza.servicios;

import com.dosemepizza.dao.ProductoDAO;
import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.ProductoEnUsoException;
import com.dosemepizza.excepciones.RegistroDuplicadoException;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.util.Validador;

import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }

    public int registrarProducto(Producto producto)
            throws DatosInvalidosException, RegistroDuplicadoException, AccesoADatosException {

        validar(producto);
        Validador.validarCodigoProducto(producto.getCodigo());

        if (productoDAO.existeCodigo(producto.getCodigo())) {
            throw new RegistroDuplicadoException("El codigo del producto ya esta registrado");
        }

        producto.setEstatus(true);
        return productoDAO.registrar(producto);
    }

    public void editarProducto(Producto producto) throws DatosInvalidosException, AccesoADatosException {
        if (producto == null || producto.getIdProducto() <= 0) {
            throw new DatosInvalidosException("El producto que intenta editar no existe");
        }
        validar(producto);

        Producto existente = productoDAO.buscarPorId(producto.getIdProducto());
        if (existente == null) {
            throw new DatosInvalidosException("El producto no existe en el sistema");
        }
        producto.setCodigo(existente.getCodigo());

        productoDAO.editar(producto);
    }

    public void eliminarProducto(int idProducto)
            throws DatosInvalidosException, ProductoEnUsoException, AccesoADatosException {

        Producto existente = productoDAO.buscarPorId(idProducto);
        if (existente == null) {
            throw new DatosInvalidosException("El producto no existe en el sistema");
        }
        if (productoDAO.tienePedidos(idProducto)) {
            throw new ProductoEnUsoException("No se puede eliminar el producto porque ya esta en pedidos");
        }
        productoDAO.eliminar(idProducto);
    }

    public Producto buscarPorId(int idProducto) throws AccesoADatosException {
        return productoDAO.buscarPorId(idProducto);
    }

    public Producto buscarPorCodigo(String codigo) throws DatosInvalidosException, AccesoADatosException {
        Validador.validarObligatorio(codigo, "codigo");
        return productoDAO.buscarPorCodigo(codigo.trim());
    }

    public List<Producto> buscarPorNombre(String texto) throws DatosInvalidosException, AccesoADatosException {
        Validador.validarObligatorio(texto, "texto de busqueda");
        return productoDAO.buscarPorNombre(texto.trim());
    }

    public List<Producto> listar() throws AccesoADatosException {
        return productoDAO.listar();
    }

    public void actualizarCantidad(int idProducto, int nuevaCantidad)
            throws DatosInvalidosException, AccesoADatosException {

        Validador.validarNoNegativo(nuevaCantidad, "cantidad");

        Producto existente = productoDAO.buscarPorId(idProducto);
        if (existente == null) {
            throw new DatosInvalidosException("El producto no existe en el sistema");
        }

        productoDAO.actualizarCantidad(idProducto, nuevaCantidad);
    }

    private void validar(Producto producto) throws DatosInvalidosException {
        if (producto == null) {
            throw new DatosInvalidosException("El producto no puede estar vacio");
        }
        Validador.validarObligatorio(producto.getNombre(), "nombre");
        Validador.validarPositivo(producto.getPrecio(), "precio");
        Validador.validarNoNegativo(producto.getCantidad(), "cantidad");
    }
}