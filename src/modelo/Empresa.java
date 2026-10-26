package com.mycompany.evaluadorcreditos;

public class Empresa extends Cliente {
    private String rubro;
    private String director;

    public Empresa(String nombre, double ingresos, double gastos, boolean tieneBuenHistorial,
                   String rubro, String director) {
        super(nombre, ingresos, gastos, tieneBuenHistorial);
        this.rubro = rubro;
        this.director = director;
    }

    @Override
    public boolean evaluarRiesgo() {
        if (gastos == 0) {
            return true; 
        }
        double ratio = ingresos / gastos;
        return ratio >= 1.5;
    }

    public String getRubro() { return rubro; }
    public String getDirector() { return director; }
}
