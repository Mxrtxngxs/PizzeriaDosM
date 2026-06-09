package com.dosemepizza;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Aplicacion extends Application {

    @Override
    public void start(Stage escenario) throws Exception {
        Parent raiz = FXMLLoader.load(
                getClass().getResource("/views/Login.fxml"));
        escenario.setTitle("Doseme Pizza - Inicio de sesion");
        escenario.setScene(new Scene(raiz));
        escenario.setResizable(false);
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}