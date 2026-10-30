package com.mycompany.evaluadorcreditos.modelo;

public class PersonaFisica extends Cliente {

    public PersonaFisica(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
    }

    @Override
    public boolean evaluarRiesgo() {
        if (!tieneBuenHistorial) {
            return false; // riesgo nulo / no apto
        }
        double margen = ingresos - gastos;
        return margen >= (ingresos * 0.30);
    }
}
