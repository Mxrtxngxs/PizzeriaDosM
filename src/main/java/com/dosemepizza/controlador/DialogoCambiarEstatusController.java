package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.PedidoNoEditableException;
import com.dosemepizza.modelo.enums.EstatusPedido;
import com.dosemepizza.modelo.Pedido;
import com.dosemepizza.servicios.PedidoService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogoCambiarEstatusController {

    @FXML private Label etiquetaPedido;
    @FXML private Label etiquetaEstatusActual;
    @FXML private ComboBox<EstatusPedido> comboNuevoEstatus;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonGuardar;

    private final PedidoService pedidoService = new PedidoService();
    private Pedido pedido;
    private boolean guardado;

    @FXML
    private void initialize() {
        comboNuevoEstatus.setItems(FXCollections.observableArrayList(
                EstatusPedido.ENTREGADO, EstatusPedido.CANCELADO));
    }

    public void cargarPedido(Pedido pedido) {
        this.pedido = pedido;
        etiquetaPedido.setText("Pedido # " + pedido.getIdPedido());
        etiquetaEstatusActual.setText(pedido.getEstatus().name());
    }

    public boolean fueGuardado() {
        return guardado;
    }

    @FXML
    private void manejarGuardar() {
        etiquetaMensaje.setText("");
        EstatusPedido nuevo = comboNuevoEstatus.getValue();
        if (nuevo == null) {
            etiquetaMensaje.setText("Seleccione un nuevo estatus");
            return;
        }

        try {
            pedidoService.cambiarEstatus(pedido.getIdPedido(), nuevo);
            guardado = true;
            cerrar();
        } catch (DatosInvalidosException | PedidoNoEditableException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo cambiar el estatus");
        }
    }

    @FXML
    private void manejarCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage escenario = (Stage) botonGuardar.getScene().getWindow();
        escenario.close();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}