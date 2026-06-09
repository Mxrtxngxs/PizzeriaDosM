package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.PedidoNoEditableException;
import com.dosemepizza.excepciones.StockInsuficienteException;
import com.dosemepizza.modelo.DetallePedido;
import com.dosemepizza.modelo.Pedido;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.servicios.PedidoService;
import com.dosemepizza.servicios.ProductoService;
import com.dosemepizza.servicios.UsuarioService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class DialogoPedidoController {

    @FXML private ComboBox<Usuario> comboCliente;
    @FXML private ComboBox<Producto> comboProducto;
    @FXML private Spinner<Integer> spinnerCantidad;
    @FXML private TableView<DetallePedido> tablaDetalles;
    @FXML private TableColumn<DetallePedido, String> columnaProducto;
    @FXML private TableColumn<DetallePedido, Integer> columnaCantidad;
    @FXML private TableColumn<DetallePedido, Double> columnaPrecio;
    @FXML private TableColumn<DetallePedido, Double> columnaSubtotal;
    @FXML private Label etiquetaTotal;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonGuardar;

    private final PedidoService pedidoService = new PedidoService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final ProductoService productoService = new ProductoService();
    private final ObservableList<DetallePedido> detalles = FXCollections.observableArrayList();

    private Pedido pedidoEnEdicion;
    private boolean guardado;

    @FXML
    private void initialize() {
        columnaProducto.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getProducto() != null ? dato.getValue().getProducto().getNombre() : ""));
        columnaCantidad.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getCantidad()));
        columnaPrecio.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getPrecioUnitario()));
        columnaSubtotal.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getSubtotal()));

        tablaDetalles.setItems(detalles);
        spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        cargarClientesYProductos();
    }

    public void cargarPedido(Pedido pedido) {
        this.pedidoEnEdicion = pedido;
        if (pedido != null) {
            comboCliente.setValue(pedido.getCliente());
            comboCliente.setDisable(true);
            detalles.setAll(pedido.getDetalles());
            actualizarTotal();
        }
    }

    public boolean fueGuardado() {
        return guardado;
    }

    @FXML
    private void manejarAgregarProducto() {
        Producto producto = comboProducto.getValue();
        Integer cantidad = spinnerCantidad.getValue();
        if (producto == null || cantidad == null || cantidad <= 0) {
            etiquetaMensaje.setText("Seleccione un producto y una cantidad valida");
            return;
        }

        for (DetallePedido existente : detalles) {
            if (existente.getIdProducto() == producto.getIdProducto()) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                tablaDetalles.refresh();
                actualizarTotal();
                return;
            }
        }

        DetallePedido detalle = new DetallePedido(producto, cantidad);
        detalles.add(detalle);
        actualizarTotal();
        etiquetaMensaje.setText("");
    }

    @FXML
    private void manejarQuitarProducto() {
        DetallePedido seleccionado = tablaDetalles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            etiquetaMensaje.setText("Seleccione un producto del pedido para quitar");
            return;
        }
        detalles.remove(seleccionado);
        actualizarTotal();
    }

    @FXML
    private void manejarGuardar() {
        etiquetaMensaje.setText("");
        Usuario cliente = comboCliente.getValue();
        if (cliente == null) {
            etiquetaMensaje.setText("Seleccione un cliente");
            return;
        }
        if (detalles.isEmpty()) {
            etiquetaMensaje.setText("Agregue al menos un producto");
            return;
        }

        Pedido pedido = pedidoEnEdicion != null ? pedidoEnEdicion : new Pedido();
        pedido.setCliente(cliente);
        pedido.setIdCliente(cliente.getIdUsuario());
        pedido.setDetalles(new ArrayList<>(detalles));

        try {
            if (pedidoEnEdicion == null) {
                pedidoService.registrarPedido(pedido);
            } else {
                pedidoService.actualizarPedido(pedido);
            }
            guardado = true;
            cerrar();
        } catch (DatosInvalidosException | StockInsuficienteException | PedidoNoEditableException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo guardar el pedido");
        }
    }

    @FXML
    private void manejarCancelar() {
        cerrar();
    }

    private void cargarClientesYProductos() {
        try {
            List<Usuario> clientes = usuarioService.listarClientes();
            comboCliente.setItems(FXCollections.observableArrayList(clientes));

            List<Producto> productos = productoService.listar();
            comboProducto.setItems(FXCollections.observableArrayList(productos));
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron cargar clientes o productos");
        }
    }

    private void actualizarTotal() {
        double total = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();
        etiquetaTotal.setText(String.format("$ %.2f", total));
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