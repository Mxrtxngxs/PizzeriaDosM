package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.ProductoEnUsoException;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.servicios.ProductoService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ProductosController {

    private static final String FXML_DIALOGO_PRODUCTO = "/views/DialogoProducto.fxml";

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> columnaCodigo;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, Double> columnaPrecio;
    @FXML private TableColumn<Producto, Integer> columnaCantidad;
    @FXML private TextField campoBusqueda;
    @FXML private ComboBox<String> comboTipoBusqueda;

    private final ProductoService productoService = new ProductoService();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        columnaCodigo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCodigo()));
        columnaNombre.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombre()));
        columnaPrecio.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getPrecio()));
        columnaCantidad.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getCantidad()));

        tablaProductos.setItems(productos);
        comboTipoBusqueda.setItems(FXCollections.observableArrayList("Nombre", "Codigo"));
        comboTipoBusqueda.getSelectionModel().selectFirst();

        cargarProductos();
    }

    @FXML
    private void manejarBuscar() {
        String texto = campoBusqueda.getText();
        if (texto == null || texto.trim().isEmpty()) {
            cargarProductos();
            return;
        }

        try {
            if ("Codigo".equals(comboTipoBusqueda.getValue())) {
                Producto producto = productoService.buscarPorCodigo(texto.trim());
                productos.setAll(producto != null ? List.of(producto) : List.of());
            } else {
                productos.setAll(productoService.buscarPorNombre(texto));
            }
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron buscar los productos");
        }
    }

    @FXML
    private void manejarAgregar() {
        abrirDialogo(null);
    }

    @FXML
    private void manejarEditar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un producto para editar");
            return;
        }
        abrirDialogo(seleccionado);
    }

    @FXML
    private void manejarEliminar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un producto para eliminar");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Esta seguro de eliminar " + seleccionado.getNombre() + "?");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            productoService.eliminarProducto(seleccionado.getIdProducto());
            cargarProductos();
        } catch (ProductoEnUsoException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo eliminar el producto");
        }
    }

    @FXML
    private void manejarGenerarReporte() {
        mostrarInfo("Funcion de reporte PDF pendiente de implementar con iText");
    }

    private void abrirDialogo(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIALOGO_PRODUCTO));
            Parent raiz = loader.load();
            DialogoProductoController controller = loader.getController();
            controller.cargarProducto(producto);

            Stage escenario = new Stage();
            escenario.setTitle(producto == null ? "Nuevo producto" : "Editar producto");
            escenario.initModality(Modality.APPLICATION_MODAL);
            escenario.setScene(new Scene(raiz));
            escenario.setResizable(false);
            escenario.showAndWait();

            if (controller.fueGuardado()) {
                cargarProductos();
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el dialogo");
            ex.printStackTrace();
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoService.listar());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron cargar los productos");
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