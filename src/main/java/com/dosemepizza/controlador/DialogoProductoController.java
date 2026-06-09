package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.RegistroDuplicadoException;
import com.dosemepizza.modelo.Producto;
import com.dosemepizza.servicios.ProductoService;
import com.dosemepizza.util.FormateadorCampos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

public class DialogoProductoController {

    @FXML private TextField campoCodigo;
    @FXML private TextField campoNombre;
    @FXML private TextArea campoDescripcion;
    @FXML private TextField campoPrecio;
    @FXML private TextField campoRestricciones;
    @FXML private TextField campoCantidad;
    @FXML private ImageView vistaFoto;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonGuardar;

    private final ProductoService productoService = new ProductoService();
    private Producto productoEnEdicion;
    private byte[] fotoActual;
    private boolean guardado;

    @FXML
    private void initialize() {
        FormateadorCampos.limitarTexto(campoCodigo, FormateadorCampos.MAX_CODIGO_PRODUCTO);
        FormateadorCampos.limitarTexto(campoNombre, FormateadorCampos.MAX_NOMBRE_PRODUCTO);
        FormateadorCampos.limitarTexto(campoDescripcion, FormateadorCampos.MAX_DESCRIPCION);
        FormateadorCampos.limitarTexto(campoRestricciones, FormateadorCampos.MAX_RESTRICCIONES);
        FormateadorCampos.soloDecimales(campoPrecio);
        FormateadorCampos.soloDigitos(campoCantidad, 6);
    }

    public void cargarProducto(Producto producto) {
        this.productoEnEdicion = producto;
        if (producto != null) {
            campoCodigo.setText(producto.getCodigo());
            campoCodigo.setDisable(true);
            campoNombre.setText(producto.getNombre());
            campoDescripcion.setText(producto.getDescripcion());
            campoPrecio.setText(String.valueOf(producto.getPrecio()));
            campoRestricciones.setText(producto.getRestricciones());
            campoCantidad.setText(String.valueOf(producto.getCantidad()));
            if (producto.getFoto() != null && producto.getFoto().length > 0) {
                fotoActual = producto.getFoto();
                vistaFoto.setImage(new Image(new ByteArrayInputStream(fotoActual)));
            }
        }
    }

    public boolean fueGuardado() {
        return guardado;
    }

    @FXML
    private void manejarSeleccionarFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar foto");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(botonGuardar.getScene().getWindow());
        if (archivo == null) return;

        try {
            fotoActual = Files.readAllBytes(archivo.toPath());
            vistaFoto.setImage(new Image(new ByteArrayInputStream(fotoActual)));
        } catch (Exception ex) {
            mostrarError("No se pudo cargar la imagen");
        }
    }

    @FXML
    private void manejarGuardar() {
        etiquetaMensaje.setText("");
        Producto producto = productoEnEdicion != null ? productoEnEdicion : new Producto();
        producto.setCodigo(campoCodigo.getText().trim());
        producto.setNombre(campoNombre.getText().trim());
        producto.setDescripcion(campoDescripcion.getText().trim());
        producto.setRestricciones(campoRestricciones.getText().trim());
        producto.setFoto(fotoActual);

        try {
            producto.setPrecio(parsearDouble(campoPrecio.getText()));
            producto.setCantidad(parsearInt(campoCantidad.getText()));

            if (productoEnEdicion == null) {
                productoService.registrarProducto(producto);
            } else {
                productoService.editarProducto(producto);
            }
            guardado = true;
            cerrar();
        } catch (DatosInvalidosException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (RegistroDuplicadoException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (NumberFormatException ex) {
            etiquetaMensaje.setText("El precio y la cantidad deben ser numericos");
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo guardar el producto");
        }
    }

    @FXML
    private void manejarCancelar() {
        cerrar();
    }

    private double parsearDouble(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        return Double.parseDouble(texto.trim());
    }

    private int parsearInt(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        return Integer.parseInt(texto.trim());
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