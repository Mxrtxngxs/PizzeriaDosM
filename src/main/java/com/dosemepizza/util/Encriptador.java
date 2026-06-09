package com.dosemepizza.util;

import org.mindrot.jbcrypt.BCrypt;

public class Encriptador {

    private static final int FACTOR_TRABAJO = 12;

    private Encriptador() {
    }

    public static String encriptar(String passwordPlana) {
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt(FACTOR_TRABAJO));
    }

    public static boolean verificar(String passwordPlana, String hash) {
        return BCrypt.checkpw(passwordPlana, hash);
    }
}