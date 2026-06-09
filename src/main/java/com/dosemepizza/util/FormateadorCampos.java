package com.dosemepizza.util;

import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class FormateadorCampos {

    public static final int MAX_NOMBRE = 80;
    public static final int MAX_APELLIDOS = 120;
    public static final int MAX_TELEFONO = 10;
    public static final int MAX_EMAIL = 150;
    public static final int MAX_CALLE = 150;
    public static final int MAX_NUMERO = 20;
    public static final int MAX_CODIGO_POSTAL = 5;
    public static final int MAX_CIUDAD = 100;
    public static final int MAX_USERNAME = 60;
    public static final int MAX_PASSWORD = 100;
    public static final int MAX_CODIGO_PRODUCTO = 30;
    public static final int MAX_NOMBRE_PRODUCTO = 150;
    public static final int MAX_DESCRIPCION = 500;
    public static final int MAX_RESTRICCIONES = 255;

    private FormateadorCampos() {
    }

    public static void limitarTexto(TextInputControl campo, int maximo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            if (cambio.getControlNewText().length() > maximo) {
                return null;
            }
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    public static void soloDigitos(TextInputControl campo, int maximo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String nuevo = cambio.getControlNewText();
            if (nuevo.length() > maximo) return null;
            if (!nuevo.matches("\\d*")) return null;
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    public static void soloDecimales(TextInputControl campo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String nuevo = cambio.getControlNewText();
            if (nuevo.isEmpty()) return cambio;
            if (!nuevo.matches("\\d{0,8}(\\.\\d{0,2})?")) return null;
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}