package com.mycompany.evaluadorcreditos.vista;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class VentanaLogin extends JFrame {

    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);
    private final JButton botonIngresar = new JButton("Ingresar");
    private final GestorBDD gestor= new GestorBDD);

    public VentanaLogin() {
        super("Evaluador de Créditos - Acceso");
        setSize(350, 180);
        setLocationRelativeTo(null);          
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Clave:"));
        panel.add(campoClave);
        panel.add(new JLabel());
        panel.add(botonIngresar);
        add(panel);

        botonIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(botonIngresar);   
    }

    private void ingresar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete usuario y clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (gestor.validarUsuario(usuario, clave)) {
                dispose();
                new VentanaPrincipal(gestor).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                campoClave.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos:\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}