package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.excepciones.UsuarioConPedidosException;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.servicios.UsuarioService;

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

public class UsuariosController {

    private static final String FXML_DIALOGO_USUARIO = "/views/DialogoUsuario.fxml";

    @FXML private TableView<Usuario> tablaClientes;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, String> columnaTelefono;
    @FXML private TableColumn<Usuario, String> columnaEmail;
    @FXML private TableColumn<Usuario, String> columnaDireccion;
    @FXML private TextField campoBusqueda;
    @FXML private ComboBox<String> comboTipoBusqueda;

    private final UsuarioService usuarioService = new UsuarioService();
    private final ObservableList<Usuario> clientes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        columnaNombre.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombreCompleto()));
        columnaTelefono.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getTelefono()));
        columnaEmail.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getEmail()));
        columnaDireccion.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getDireccion()));

        tablaClientes.setItems(clientes);
        comboTipoBusqueda.setItems(FXCollections.observableArrayList("Nombre", "Telefono", "Direccion"));
        comboTipoBusqueda.getSelectionModel().selectFirst();

        cargarClientes();
    }

    @FXML
    private void manejarBuscar() {
        String texto = campoBusqueda.getText();
        if (texto == null || texto.trim().isEmpty()) {
            cargarClientes();
            return;
        }

        try {
            List<Usuario> resultado;
            String tipo = comboTipoBusqueda.getValue();
            switch (tipo) {
                case "Telefono":
                    resultado = usuarioService.buscarPorTelefono(texto);
                    break;
                case "Direccion":
                    resultado = usuarioService.buscarPorDireccion(texto);
                    break;
                default:
                    resultado = usuarioService.buscarPorNombre(texto);
            }
            clientes.setAll(resultado);
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron buscar los clientes");
        }
    }

    @FXML
    private void manejarAgregar() {
        abrirDialogo(null);
    }

    @FXML
    private void manejarEditar() {
        Usuario seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un cliente para editar");
            return;
        }
        abrirDialogo(seleccionado);
    }

    @FXML
    private void manejarEliminar() {
        Usuario seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un cliente para eliminar");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cliente");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Esta seguro de eliminar a " + seleccionado.getNombreCompleto() + "?");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            usuarioService.eliminarCliente(seleccionado.getIdUsuario());
            cargarClientes();
        } catch (UsuarioConPedidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (DatosInvalidosException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo eliminar el cliente");
        }
    }

    private void abrirDialogo(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DIALOGO_USUARIO));
            Parent raiz = loader.load();
            DialogoUsuarioController controller = loader.getController();
            controller.cargarUsuario(usuario);

            Stage escenario = new Stage();
            escenario.setTitle(usuario == null ? "Nuevo cliente" : "Editar cliente");
            escenario.initModality(Modality.APPLICATION_MODAL);
            escenario.setScene(new Scene(raiz));
            escenario.setResizable(false);
            escenario.showAndWait();

            if (controller.fueGuardado()) {
                cargarClientes();
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el dialogo");
            ex.printStackTrace();
        }
    }

    private void cargarClientes() {
        try {
            clientes.setAll(usuarioService.listarClientes());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudieron cargar los clientes");
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
}