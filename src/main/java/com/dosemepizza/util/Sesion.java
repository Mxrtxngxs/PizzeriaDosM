package com.dosemepizza.util;

import com.dosemepizza.modelo.Empleado;

public class Sesion {

    private static Empleado empleadoActivo;

    private Sesion() {
    }

    public static void iniciar(Empleado empleado) {
        empleadoActivo = empleado;
    }

    public static Empleado getEmpleadoActivo() {
        return empleadoActivo;
    }

    public static void cerrar() {
        empleadoActivo = null;
    }

    public static boolean hayUsuarioActivo() {
        return empleadoActivo != null;
    }
}