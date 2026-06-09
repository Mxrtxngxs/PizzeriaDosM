package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.modelo.enums.EstatusPedido;
import com.dosemepizza.modelo.Pedido;
import com.dosemepizza.servicios.PedidoService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PedidosController {

    private static final String FXML_DIALOGO_PEDIDO = "/views/DialogoPedido.fxml";
    private static final String FXML_DIALOGO_CAMBIAR_ESTATUS = "/views/DialogoCambiarEstatus.fxml";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn<Pedido, String> columnaCliente;
    @FXML private TableColumn<Pedido, String> columnaFecha;
    @FXML private TableColumn<Pedido, Double> columnaTotal;
    @FXML private TableColumn<Pedido, String> columnaEstatus;
    @FXML private DatePicker selectorFechaDesde;
    @FXML private DatePicker selectorFechaHasta;
    @FXML private ComboBox<EstatusPedido> comboEstatus;

    private final PedidoService pedidoService = new PedidoService();
    private final ObservableList<Pedido> pedidos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        columnaCliente.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getCliente() != null ? dato.getValue().getCliente().getNombreCompleto() : ""));
        columnaFecha.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getFechaPedido() != null ? dato.getValue().getFechaPedido().format(FORMATO_FECHA) : ""));
        columnaTotal.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getTotal()));
        columnaEstatus.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getEstatus().name()));

        tablaPedidos.setItems(pedidos);
        comboEstatus.setItems(FXCollections.observableArrayList(EstatusPedido.values()));

        cargarPedidos();
    }

    @FXML
    private void manejarBuscarPorFecha() {
        LocalDate desde = selectorFechaDesde.getValue();
        LocalDate hasta = selectorFechaHasta.getValue();
        try {
            pedidos.setAll(pedidoService.buscarPorFecha(desde, hasta));
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron buscar los pedidos");
        }
    }

    @FXML
    private void manejarBuscarPorEstatus() {
        EstatusPedido estatus = comboEstatus.getValue();
        try {
            pedidos.setAll(pedidoService.buscarPorEstatus(estatus));
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron buscar los pedidos");
        }
    }

    @FXML
    private void manejarLimpiarFiltros() {
        selectorFechaDesde.setValue(null);
        selectorFechaHasta.setValue(null);
        comboEstatus.setValue(null);
        cargarPedidos();
    }

    @FXML
    private void manejarNuevoPedido() {
        abrirDialogoPedido(null);
    }

    @FXML
    private void manejarEditar() {
        Pedido seleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un pedido para editar");
            return;
        }
        abrirDialogoPedido(seleccionado);
    }

    @FXML
    private void manejarCambiarEstatus() {
        Pedido seleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un pedido");
            return;
        }
        abrirDialogoEstatus(seleccionado);
    }

    @FXML
    private void manejarExportarCsv() {
        mostrarInfo("Funcion de exportar CSV pendiente de implementar con OpenCSV");
    }

    @FXML
    private void manejarExportarPdf() {
        mostrarInfo("Funcion de exportar PDF pendiente de implementar con iText");
    }

    private void abrirDialogoPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIALOGO_PEDIDO));
            Parent raiz = loader.load();
            DialogoPedidoController controller = loader.getController();
            controller.cargarPedido(pedido);

            Stage escenario = new Stage();
            escenario.setTitle(pedido == null ? "Nuevo pedido" : "Editar pedido");
            escenario.initModality(Modality.APPLICATION_MODAL);
            escenario.setScene(new Scene(raiz));
            escenario.showAndWait();

            if (controller.fueGuardado()) {
                cargarPedidos();
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el dialogo");
            ex.printStackTrace();
        }
    }

    private void abrirDialogoEstatus(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIALOGO_CAMBIAR_ESTATUS));
            Parent raiz = loader.load();
            DialogoCambiarEstatusController controller = loader.getController();
            controller.cargarPedido(pedido);

            Stage escenario = new Stage();
            escenario.setTitle("Cambiar estatus");
            escenario.initModality(Modality.APPLICATION_MODAL);
            escenario.setScene(new Scene(raiz));
            escenario.setResizable(false);
            escenario.showAndWait();

            if (controller.fueGuardado()) {
                cargarPedidos();
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el dialogo");
            ex.printStackTrace();
        }
    }

    private void cargarPedidos() {
        try {
            pedidos.setAll(pedidoService.listar());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron cargar los pedidos");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}