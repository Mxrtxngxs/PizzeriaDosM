package com.dosemepizza.servicios;

import com.dosemepizza.dao.PedidoDAO;
import com.dosemepizza.dao.ProductoDAO;
import com.dosemepizza.dao.UsuarioDAO;
import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.PedidoNoEditableException;
import com.dosemepizza.excepciones.StockInsuficienteException;
import com.dosemepizza.modelo.DetallePedido;
import com.dosemepizza.modelo.enums.EstatusPedido;
import com.dosemepizza.modelo.Pedido;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.modelo.Usuario;

import java.time.LocalDate;
import java.util.List;

public class PedidoService {

    private final PedidoDAO pedidoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ProductoDAO productoDAO;

    public PedidoService() {
        this.pedidoDAO = new PedidoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.productoDAO = new ProductoDAO();
    }

    public int registrarPedido(Pedido pedido)
            throws DatosInvalidosException, StockInsuficienteException, AccesoADatosException {

        validar(pedido);

        Usuario cliente = usuarioDAO.buscarPorId(pedido.getIdCliente());
        if (cliente == null) {
            throw new DatosInvalidosException("El cliente del pedido no existe");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = productoDAO.buscarPorId(detalle.getIdProducto());
            if (producto == null) {
                throw new DatosInvalidosException("Uno de los productos del pedido no existe");
            }
            if (producto.getCantidad() < detalle.getCantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para el producto " + producto.getNombre());
            }
            detalle.setPrecioUnitario(producto.getPrecio());
        }

        pedido.setEstatus(EstatusPedido.EN_PROCESO);
        pedido.recalcularTotal();
        return pedidoDAO.registrar(pedido);
    }

    public void actualizarPedido(Pedido pedido)
            throws DatosInvalidosException, PedidoNoEditableException, StockInsuficienteException, AccesoADatosException {

        if (pedido == null || pedido.getIdPedido() <= 0) {
            throw new DatosInvalidosException("El pedido que intenta editar no existe");
        }
        validar(pedido);

        Pedido existente = pedidoDAO.buscarPorId(pedido.getIdPedido());
        if (existente == null) {
            throw new DatosInvalidosException("El pedido no existe en el sistema");
        }
        if (existente.getEstatus() != EstatusPedido.EN_PROCESO) {
            throw new PedidoNoEditableException("Solo se pueden editar pedidos en proceso");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = productoDAO.buscarPorId(detalle.getIdProducto());
            if (producto == null) {
                throw new DatosInvalidosException("Uno de los productos del pedido no existe");
            }
            if (producto.getCantidad() < detalle.getCantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para el producto " + producto.getNombre());
            }
            if (detalle.getPrecioUnitario() <= 0) {
                detalle.setPrecioUnitario(producto.getPrecio());
            }
        }

        pedido.recalcularTotal();
        pedidoDAO.actualizarDetalles(pedido);
    }

    public void cambiarEstatus(int idPedido, EstatusPedido nuevoEstatus)
            throws DatosInvalidosException, PedidoNoEditableException, AccesoADatosException {

        if (nuevoEstatus == null) {
            throw new DatosInvalidosException("El estatus es obligatorio");
        }

        Pedido existente = pedidoDAO.buscarPorId(idPedido);
        if (existente == null) {
            throw new DatosInvalidosException("El pedido no existe en el sistema");
        }
        if (existente.getEstatus() != EstatusPedido.EN_PROCESO) {
            throw new PedidoNoEditableException("Solo los pedidos en proceso pueden cambiar de estatus");
        }
        if (nuevoEstatus == EstatusPedido.EN_PROCESO) {
            throw new PedidoNoEditableException("El pedido ya esta en proceso");
        }

        pedidoDAO.cambiarEstatus(idPedido, nuevoEstatus);
    }

    public Pedido buscarPorId(int idPedido) throws AccesoADatosException {
        return pedidoDAO.buscarPorId(idPedido);
    }

    public List<Pedido> listar() throws AccesoADatosException {
        return pedidoDAO.listar();
    }

    public List<Pedido> buscarPorCliente(int idCliente) throws DatosInvalidosException, AccesoADatosException {
        if (idCliente <= 0) {
            throw new DatosInvalidosException("Debe seleccionar un cliente valido");
        }
        return pedidoDAO.buscarPorCliente(idCliente);
    }

    public List<Pedido> buscarPorFecha(LocalDate desde, LocalDate hasta)
            throws DatosInvalidosException, AccesoADatosException {

        if (desde == null || hasta == null) {
            throw new DatosInvalidosException("Las fechas son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new DatosInvalidosException("La fecha final no puede ser anterior a la inicial");
        }
        return pedidoDAO.buscarPorFecha(desde, hasta);
    }

    public List<Pedido> buscarPorEstatus(EstatusPedido estatus)
            throws DatosInvalidosException, AccesoADatosException {

        if (estatus == null) {
            throw new DatosInvalidosException("El estatus es obligatorio");
        }
        return pedidoDAO.buscarPorEstatus(estatus);
    }

    private void validar(Pedido pedido) throws DatosInvalidosException {
        if (pedido == null) {
            throw new DatosInvalidosException("El pedido no puede estar vacio");
        }
        if (pedido.getIdCliente() <= 0) {
            throw new DatosInvalidosException("Debe seleccionar un cliente para el pedido");
        }
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new DatosInvalidosException("El pedido debe tener al menos un producto");
        }
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getCantidad() <= 0) {
                throw new DatosInvalidosException("La cantidad de cada producto debe ser mayor a cero");
            }
        }
    }
}