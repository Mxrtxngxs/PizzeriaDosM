package com.dosemepizza.util;

import com.dosemepizza.excepciones.DatosInvalidosException;

public class Validador {

    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    private static final String REGEX_CODIGO_POSTAL = "^\\d{5}$";
    private static final String REGEX_USERNAME = "^[a-zA-Z0-9_]{4,60}$";
    private static final String REGEX_PASSWORD = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$";
    private static final String REGEX_CODIGO_PRODUCTO = "^[A-Z0-9-]{3,30}$";

    private Validador() {
    }

    public static void validarObligatorio(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio");
        }
    }

    public static void validarEmail(String email) throws DatosInvalidosException {
        validarObligatorio(email, "email");
        if (!email.matches(REGEX_EMAIL)) {
            throw new DatosInvalidosException("El email no tiene un formato valido");
        }
    }

    public static void validarTelefono(String telefono) throws DatosInvalidosException {
        validarObligatorio(telefono, "telefono");
        if (!telefono.matches(REGEX_TELEFONO)) {
            throw new DatosInvalidosException("El telefono debe tener 10 digitos");
        }
    }

    public static void validarCodigoPostal(String codigoPostal) throws DatosInvalidosException {
        validarObligatorio(codigoPostal, "codigo postal");
        if (!codigoPostal.matches(REGEX_CODIGO_POSTAL)) {
            throw new DatosInvalidosException("El codigo postal debe tener 5 digitos");
        }
    }

    public static void validarUsername(String username) throws DatosInvalidosException {
        validarObligatorio(username, "username");
        if (!username.matches(REGEX_USERNAME)) {
            throw new DatosInvalidosException("El username debe tener entre 4 y 60 caracteres alfanumericos");
        }
    }

    public static void validarPassword(String password) throws DatosInvalidosException {
        validarObligatorio(password, "contrasena");
        if (!password.matches(REGEX_PASSWORD)) {
            throw new DatosInvalidosException("La contrasena debe tener al menos 8 caracteres con letras y numeros");
        }
    }

    public static void validarCodigoProducto(String codigo) throws DatosInvalidosException {
        validarObligatorio(codigo, "codigo");
        if (!codigo.matches(REGEX_CODIGO_PRODUCTO)) {
            throw new DatosInvalidosException("El codigo debe tener entre 3 y 30 caracteres en mayusculas, digitos o guiones");
        }
    }

    public static void validarPositivo(double valor, String campo) throws DatosInvalidosException {
        if (valor <= 0) {
            throw new DatosInvalidosException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    public static void validarNoNegativo(int valor, String campo) throws DatosInvalidosException {
        if (valor < 0) {
            throw new DatosInvalidosException("El campo " + campo + " no puede ser negativo");
        }
    }
}