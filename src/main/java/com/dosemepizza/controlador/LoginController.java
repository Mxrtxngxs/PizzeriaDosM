package com.dosemepizza.controlador;

import com.dosemepizza.excepciones.AutenticacionException;
import com.dosemepizza.excepciones.CredencialesInvalidasException;
import com.dosemepizza.modelo.Empleado;
import com.dosemepizza.servicios.AutenticacionService;
import com.dosemepizza.util.FormateadorCampos;
import com.dosemepizza.util.Sesion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    private static final String FXML_MENU_PRINCIPAL = "/views/MenuPrincipal.fxml";

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonEntrar;

    private final AutenticacionService autenticacionService = new AutenticacionService();

    @FXML
    private void initialize() {
        FormateadorCampos.limitarTexto(campoUsuario, FormateadorCampos.MAX_USERNAME);
        FormateadorCampos.limitarTexto(campoContrasena, FormateadorCampos.MAX_PASSWORD);
    }

    @FXML
    private void manejarEntrar() {
        etiquetaMensaje.setText("");

        try {
            Empleado empleado = autenticacionService.iniciarSesion(
                    campoUsuario.getText(),
                    campoContrasena.getText());
            Sesion.iniciar(empleado);
            abrirMenuPrincipal();
        } catch (CredencialesInvalidasException | AutenticacionException ex) {
            etiquetaMensaje.setText(ex.getMessage());
        } catch (Exception ex) {
            etiquetaMensaje.setText("Error inesperado al iniciar sesion");
            ex.printStackTrace();
        }
    }

    private void abrirMenuPrincipal() throws Exception {
        Parent raiz = FXMLLoader.load(getClass().getResource(FXML_MENU_PRINCIPAL));
        Stage escenario = (Stage) botonEntrar.getScene().getWindow();
        escenario.setScene(new Scene(raiz));
        escenario.setTitle("Doseme Pizza");
        escenario.setResizable(true);
        escenario.centerOnScreen();
    }
}