package com.dosemepizza.controlador;

import com.dosemepizza.modelo.Empleado;
import com.dosemepizza.util.Sesion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MenuPrincipalController {

    private static final String FXML_USUARIOS = "/views/Usuarios.fxml";
    private static final String FXML_PRODUCTOS = "/views/Productos.fxml";
    private static final String FXML_PEDIDOS = "/views/Pedidos.fxml";
    private static final String FXML_VALIDACION_INVENTARIO = "/views/ValidacionInventario.fxml";
    private static final String FXML_ACERCA_DE = "/views/DialogoAcercaDe.fxml";
    private static final String FXML_LOGIN = "/views/Login.fxml";

    @FXML private BorderPane contenedorPrincipal;
    @FXML private Label etiquetaEmpleado;
    @FXML private MenuItem menuUsuarios;
    @FXML private MenuItem menuValidacionInventario;

    @FXML
    private void initialize() {
        Empleado empleado = Sesion.getEmpleadoActivo();
        if (empleado != null) {
            etiquetaEmpleado.setText(empleado.getNombreCompleto() + " (" + empleado.getRol() + ")");
            if (!empleado.esAdministrador()) {
                menuUsuarios.setDisable(true);
                menuValidacionInventario.setDisable(true);
            }
        }
    }

    @FXML
    private void abrirUsuarios() {
        cargarVista(FXML_USUARIOS);
    }

    @FXML
    private void abrirProductos() {
        cargarVista(FXML_PRODUCTOS);
    }

    @FXML
    private void abrirPedidos() {
        cargarVista(FXML_PEDIDOS);
    }

    @FXML
    private void abrirValidacionInventario() {
        cargarVista(FXML_VALIDACION_INVENTARIO);
    }

    @FXML
    private void mostrarAcercaDe() {
        try {
            Parent raiz = FXMLLoader.load(getClass().getResource(FXML_ACERCA_DE));
            Stage escenario = new Stage();
            escenario.setTitle("Acerca de");
            escenario.setScene(new Scene(raiz));
            escenario.setResizable(false);
            escenario.showAndWait();
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el dialogo");
            ex.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion() {
        try {
            Sesion.cerrar();
            Parent raiz = FXMLLoader.load(getClass().getResource(FXML_LOGIN));
            Stage escenario = (Stage) contenedorPrincipal.getScene().getWindow();
            escenario.setScene(new Scene(raiz));
            escenario.setTitle("Doseme Pizza - Inicio de sesion");
            escenario.setResizable(false);
            escenario.centerOnScreen();
        } catch (Exception ex) {
            mostrarError("No se pudo cerrar la sesion");
            ex.printStackTrace();
        }
    }

    private void cargarVista(String rutaFxml) {
        try {
            Node contenido = FXMLLoader.load(getClass().getResource(rutaFxml));
            contenedorPrincipal.setCenter(contenido);
        } catch (Exception ex) {
            mostrarError("No se pudo cargar la vista");
            ex.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}