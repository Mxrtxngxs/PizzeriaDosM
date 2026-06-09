package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.servicios.ProductoService;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidacionInventarioController {

    @FXML private TableView<FilaInventario> tablaInventario;
    @FXML private TableColumn<FilaInventario, String> columnaProducto;
    @FXML private TableColumn<FilaInventario, Integer> columnaSistema;
    @FXML private TableColumn<FilaInventario, Integer> columnaReal;
    @FXML private TableColumn<FilaInventario, Integer> columnaDiferencia;
    @FXML private TableColumn<FilaInventario, String> columnaEstado;

    private final ProductoService productoService = new ProductoService();
    private final ObservableList<FilaInventario> filas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        columnaProducto.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombre()));
        columnaSistema.setCellValueFactory(dato -> dato.getValue().cantidadSistemaProperty().asObject());
        columnaReal.setCellValueFactory(dato -> dato.getValue().cantidadRealProperty().asObject());
        columnaDiferencia.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getDiferencia()));
        columnaEstado.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getEstado()));

        columnaReal.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        columnaReal.setOnEditCommit(evento -> {
            FilaInventario fila = evento.getRowValue();
            fila.setCantidadReal(evento.getNewValue() != null ? evento.getNewValue() : 0);
            tablaInventario.refresh();
        });

        tablaInventario.setEditable(true);
        tablaInventario.setItems(filas);

        cargarInventario();
    }

    @FXML
    private void manejarGuardarAjustes() {
        Map<Integer, Integer> ajustes = new HashMap<>();
        for (FilaInventario fila : filas) {
            if (fila.getCantidadReal() != fila.getCantidadSistema()) {
                ajustes.put(fila.getIdProducto(), fila.getCantidadReal());
            }
        }

        if (ajustes.isEmpty()) {
            mostrarInfo("No hay diferencias para ajustar");
            return;
        }

        try {
            for (Map.Entry<Integer, Integer> entrada : ajustes.entrySet()) {
                productoService.actualizarCantidad(entrada.getKey(), entrada.getValue());
            }
            cargarInventario();
            mostrarInfo("Inventario actualizado");
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo actualizar el inventario");
        }
    }

    private void cargarInventario() {
        try {
            List<Producto> productos = productoService.listar();
            filas.clear();
            for (Producto producto : productos) {
                filas.add(new FilaInventario(producto));
            }
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo cargar el inventario");
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

    public static class FilaInventario {
        private final int idProducto;
        private final String nombre;
        private final SimpleIntegerProperty cantidadSistema;
        private final SimpleIntegerProperty cantidadReal;

        public FilaInventario(Producto producto) {
            this.idProducto = producto.getIdProducto();
            this.nombre = producto.getNombre();
            this.cantidadSistema = new SimpleIntegerProperty(producto.getCantidad());
            this.cantidadReal = new SimpleIntegerProperty(producto.getCantidad());
        }

        public int getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        public int getCantidadSistema() { return cantidadSistema.get(); }
        public int getCantidadReal() { return cantidadReal.get(); }
        public void setCantidadReal(int valor) { cantidadReal.set(valor); }
        public SimpleIntegerProperty cantidadSistemaProperty() { return cantidadSistema; }
        public SimpleIntegerProperty cantidadRealProperty() { return cantidadReal; }

        public int getDiferencia() {
            return getCantidadReal() - getCantidadSistema();
        }

        public String getEstado() {
            int diferencia = getDiferencia();
            if (diferencia == 0) return "Correcto";
            if (diferencia > 0) return "Sobrante";
            return "Faltante";
        }
    }
}