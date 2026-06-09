package com.dosemepizza.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DialogoAcercaDeController {

    @FXML private Button botonCerrar;

    @FXML
    private void manejarCerrar() {
        Stage escenario = (Stage) botonCerrar.getScene().getWindow();
        escenario.close();
    }
}