package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AccesoADatosException;
import com.dosemepizza.excepciones.DatosInvalidosException;
import com.dosemepizza.modelo.Usuario;
import com.dosemepizza.servicios.UsuarioService;
import com.dosemepizza.util.FormateadorCampos;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogoUsuarioController {

    @FXML private TextField campoNombre;
    @FXML private TextField campoApellidos;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoEmail;
    @FXML private TextField campoCalle;
    @FXML private TextField campoNumero;
    @FXML private TextField campoCodigoPostal;
    @FXML private TextField campoCiudad;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonGuardar;

    private final UsuarioService usuarioService = new UsuarioService();
    private Usuario usuarioEnEdicion;
    private boolean guardado;

    @FXML
    private void initialize() {
        FormateadorCampos.limitarTexto(campoNombre, FormateadorCampos.MAX_NOMBRE);
        FormateadorCampos.limitarTexto(campoApellidos, FormateadorCampos.MAX_APELLIDOS);
        FormateadorCampos.soloDigitos(campoTelefono, FormateadorCampos.MAX_TELEFONO);
        FormateadorCampos.limitarTexto(campoEmail, FormateadorCampos.MAX_EMAIL);
        FormateadorCampos.limitarTexto(campoCalle, FormateadorCampos.MAX_CALLE);
        FormateadorCampos.limitarTexto(campoNumero, FormateadorCampos.MAX_NUMERO);
        FormateadorCampos.soloDigitos(campoCodigoPostal, FormateadorCampos.MAX_CODIGO_POSTAL);
        FormateadorCampos.limitarTexto(campoCiudad, FormateadorCampos.MAX_CIUDAD);
    }

    public void cargarUsuario(Usuario usuario) {
        this.usuarioEnEdicion = usuario;
        if (usuario != null) {
            campoNombre.setText(usuario.getNombre());
            campoApellidos.setText(usuario.getApellidos());
            campoTelefono.setText(usuario.getTelefono());
            campoEmail.setText(usuario.getEmail());
            campoCalle.setText(usuario.getCalle());
            campoNumero.setText(usuario.getNumero());
            campoCodigoPostal.setText(usuario.getCodigoPostal());
            campoCiudad.setText(usuario.getCiudad());
        }
    }

    public boolean fueGuardado() {
        return guardado;
    }

    @FXML
    private void manejarGuardar() {
        etiquetaMensaje.setText("");
        Usuario usuario = usuarioEnEdicion != null ? usuarioEnEdicion : new Usuario();
        usuario.setNombre(campoNombre.getText().trim());
        usuario.setApellidos(campoApellidos.getText().trim());
        usuario.setTelefono(campoTelefono.getText().trim());
        usuario.setEmail(campoEmail.getText().trim());
        usuario.setCalle(campoCalle.getText().trim());
        usuario.setNumero(campoNumero.getText().trim());
        usuario.setCodigoPostal(campoCodigoPostal.getText().trim());
        usuario.setCiudad(campoCiudad.getText().trim());

        try {
            if (usuarioEnEdicion == null) {
                usuarioService.registrarCliente(usuario);
            } else {
                usuarioService.editarCliente(usuario);
            }
            guardado = true;
            cerrar();
        } catch (DatosInvalidosException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (AccesoADatosException ex) {
            mostrarError("No se pudo guardar el cliente");
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