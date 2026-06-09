package com.dosemepizza.excepciones;

public class ProductoEnUsoException extends RuntimeException {
    public ProductoEnUsoException(String message) {
        super(message);
    }
}
