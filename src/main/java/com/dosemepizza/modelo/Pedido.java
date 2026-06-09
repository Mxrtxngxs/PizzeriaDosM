package com.dosemepizza.modelo;

import com.dosemepizza.modelo.enums.EstatusPedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private Usuario cliente;
    private LocalDateTime fechaPedido;
    private double total;
    private EstatusPedido estatus;
    private List<DetallePedido> detalles;

    public Pedido() {
        this.detalles = new ArrayList<>();
        this.estatus = EstatusPedido.EN_PROCESO;
        this.fechaPedido = LocalDateTime.now();
        this.total = 0.0;
    }

    public Pedido(Usuario cliente) {
        this();
        setCliente(cliente);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getIdUsuario();
        }
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public EstatusPedido getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusPedido estatus) {
        this.estatus = estatus;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
        recalcularTotal();
    }

    public void agregarDetalle(DetallePedido detalle) {
        this.detalles.add(detalle);
        recalcularTotal();
    }

    public void quitarDetalle(DetallePedido detalle) {
        this.detalles.remove(detalle);
        recalcularTotal();
    }

    public void recalcularTotal() {
        double suma = 0.0;
        for (DetallePedido d : detalles) {
            suma += d.getSubtotal();
        }
        this.total = suma;
    }
}