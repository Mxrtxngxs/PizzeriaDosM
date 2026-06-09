package com.dosemepizza.excepciones;

public class PedidoNoEditableException extends RuntimeException {
    public PedidoNoEditableException(String message) {
        super(message);
    }
}
