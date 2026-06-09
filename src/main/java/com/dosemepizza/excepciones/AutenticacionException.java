package com.dosemepizza.excepciones;

public class AutenticacionException extends Exception {

  public AutenticacionException(String mensaje) {
    super(mensaje);
  }

  public AutenticacionException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}