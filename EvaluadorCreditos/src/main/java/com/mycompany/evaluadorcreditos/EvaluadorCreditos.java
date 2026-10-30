package com.mycompany.evaluadorcreditos;

import com.mycompany.evaluadorcreditos.vista.VentanaLogin;
import javax.swing.SwingUtilities;

public class EvaluadorCreditos {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            VentanaLogin ventana = new VentanaLogin();
            ventana.setVisible(true);
        });
    }
}