package com.dosemepizza.excepciones;

public class AccesoADatosException extends RuntimeException {
    public AccesoADatosException(String message) {
        super(message);
    }
    public AccesoADatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

}
