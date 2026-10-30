package com.mycompany.evaluadorcreditos.vista;

import com.mycompany.evaluadorcreditos.modelo.*;
import com.mycompany.evaluadorcreditos.persistencia.GestorBDD;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private GestorBDD gestor;

    // Acciones compartidas: el mismo objeto se usa en el menú, la toolbar y el botón
    private Action accionGuardar;
    private Action accionRefrescar;
    private Action accionUsuarios;

    private JTabbedPane panelPestanias;
    private JComboBox<String> comboTipoCliente;
    private JTextField campoNombre;
    private JSpinner spinnerIngresos;
    private JSpinner spinnerGastos;
    private JCheckBox checkHistorial;
    private JPanel panelEmpresa;
    private JTextField campoRubro;
    private JTextField campoDirector;
    private JButton botonGuardar;
    private JProgressBar barraProgreso;

    private DefaultTableModel modeloClientes;
    private JTable tablaClientes;

    public VentanaPrincipal(GestorBDD gestor) {
        this.gestor = gestor;

        setTitle("Evaluador de Créditos");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        crearAcciones();

        setJMenuBar(crearMenu());
        add(crearToolBar(), BorderLayout.NORTH);

        panelPestanias = new JTabbedPane();
        panelPestanias.addTab("Nuevo análisis", crearPanelFormulario());
        panelPestanias.addTab("Clientes registrados", crearPanelListado());
        add(panelPestanias, BorderLayout.CENTER);

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        add(barraProgreso, BorderLayout.SOUTH);

        actualizarCamposSegunTipo();
        refrescarClientes();
    }

    // ===================== ACCIONES =====================

    private void crearAcciones() {
        accionGuardar = new AbstractAction("Guardar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCliente();
            }
        };
        accionRefrescar = new AbstractAction("Refrescar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarClientes();
            }
        };
        accionUsuarios = new AbstractAction("Usuarios") {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaUsuarios();
            }
        };
    }

    // ===================== MENÚ Y TOOLBAR =====================

    private JMenuBar crearMenu() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.add(new JMenuItem(accionGuardar));
        menuArchivo.add(new JMenuItem(accionRefrescar));
        menuArchivo.addSeparator();
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);

        JMenu menuAdmin = new JMenu("Administración");
        menuAdmin.add(new JMenuItem(accionUsuarios));

        barraMenu.add(menuArchivo);
        barraMenu.add(menuAdmin);
        return barraMenu;
    }

    private JToolBar crearToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(accionGuardar);
        toolBar.add(accionRefrescar);
        toolBar.addSeparator();
        toolBar.add(accionUsuarios);
        return toolBar;
    }

    // ===================== PESTAÑA FORMULARIO =====================

    private JPanel crearPanelFormulario() {
        comboTipoCliente = new JComboBox<>(new String[]{"Persona Física", "Empresa"});
        campoNombre = new JTextField(20);
        spinnerIngresos = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999999.0, 1000.0));
        spinnerGastos = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999999.0, 1000.0));
        checkHistorial = new JCheckBox("Tiene buen historial crediticio");
        campoRubro = new JTextField(20);
        campoDirector = new JTextField(20);
        botonGuardar = new JButton(accionGuardar);

        comboTipoCliente.addActionListener(e -> actualizarCamposSegunTipo());

        JPanel panelComun = new JPanel(new GridLayout(5, 2, 8, 8));
        panelComun.add(new JLabel("Tipo de cliente:"));
        panelComun.add(comboTipoCliente);
        panelComun.add(new JLabel("Nombre:"));
        panelComun.add(campoNombre);
        panelComun.add(new JLabel("Ingresos:"));
        panelComun.add(spinnerIngresos);
        panelComun.add(new JLabel("Gastos:"));
        panelComun.add(spinnerGastos);
        panelComun.add(new JLabel("Historial:"));
        panelComun.add(checkHistorial);

        panelEmpresa = new JPanel(new GridLayout(2, 2, 8, 8));
        panelEmpresa.setBorder(BorderFactory.createTitledBorder("Datos de la empresa"));
        panelEmpresa.add(new JLabel("Rubro:"));
        panelEmpresa.add(campoRubro);
        panelEmpresa.add(new JLabel("Director:"));
        panelEmpresa.add(campoDirector);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(botonGuardar);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(panelComun, BorderLayout.NORTH);
        panel.add(panelEmpresa, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarCamposSegunTipo() {
        boolean esEmpresa = comboTipoCliente.getSelectedItem().equals("Empresa");
        panelEmpresa.setVisible(esEmpresa);
        if (!esEmpresa) {
            campoRubro.setText("");
            campoDirector.setText("");
        }
        revalidate();
        repaint();
    }

    // ===================== PESTAÑA LISTADO =====================

    private JPanel crearPanelListado() {
        modeloClientes = new DefaultTableModel(
                new String[]{"ID", "Nombre", "Tipo", "Ingresos", "Gastos", "Historial", "Resultado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloClientes);
        tablaClientes.setRowSorter(new TableRowSorter<>(modeloClientes));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        return panel;
    }

    // ===================== GUARDAR CLIENTE =====================

    private void guardarCliente() {
        String nombre = campoNombre.getText().trim();
        double ingresos = ((Number) spinnerIngresos.getValue()).doubleValue();
        double gastos = ((Number) spinnerGastos.getValue()).doubleValue();
        boolean historial = checkHistorial.isSelected();
        boolean esEmpresa = comboTipoCliente.getSelectedItem().equals("Empresa");
        String rubro = campoRubro.getText().trim();
        String director = campoDirector.getText().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            avisar("Ingrese el nombre del cliente.");
            return;
        }
        if (ingresos <= 0) {
            avisar("Los ingresos deben ser mayores a cero.");
            return;
        }
        if (esEmpresa && (rubro.isEmpty() || director.isEmpty())) {
            avisar("Complete rubro y director de la empresa.");
            return;
        }

        // Polimorfismo: variable de tipo Cliente con la subclase que corresponde
        Cliente cliente;
        if (esEmpresa) {
            cliente = new Empresa(nombre, ingresos, gastos, historial, rubro, director);
        } else {
            cliente = new PersonaFisica(nombre, ingresos, gastos, historial);
        }

        iniciarOperacion("Guardando cliente...");
        try {
            int id = gestor.guardarCliente(cliente); // tabla padre, devuelve el id generado

            if (esEmpresa) {
                gestor.guardarEmpresa(id, rubro, director); // tabla hija
            } else {
                gestor.guardarPersonaFisica(id);            // tabla hija
            }

            JOptionPane.showMessageDialog(this,
                    "Cliente guardado con ID " + id + ".\nResultado de la evaluación: "
                            + textoResultado(cliente.evaluarRiesgo()),
                    "Análisis registrado", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            refrescarClientes();
        } catch (SQLException ex) {
            mostrarError("No se pudo guardar el cliente", ex);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== REFRESCAR LISTADO =====================

    private void refrescarClientes() {
        iniciarOperacion("Cargando clientes...");
        try {
            List<Object[]> filas = gestor.listarClientes();
            modeloClientes.setRowCount(0);

            for (Object[] f : filas) {
                int id = (Integer) f[0];
                String nombre = (String) f[1];
                double ingresos = (Double) f[2];
                double gastos = (Double) f[3];
                boolean historial = (Boolean) f[4];
                String tipo = (String) f[5];

                Cliente c;
                if (tipo.equals("Empresa")) {
                    c = new Empresa(nombre, ingresos, gastos, historial, "", "");
                } else {
                    c = new PersonaFisica(nombre, ingresos, gastos, historial);
                }

                modeloClientes.addRow(new Object[]{
                        id, nombre, tipo, ingresos, gastos,
                        historial ? "Sí" : "No",
                        textoResultado(c.evaluarRiesgo())
                });
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar el listado de clientes", ex);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== ABRIR VENTANA USUARIOS =====================

    private void abrirVentanaUsuarios() {
        iniciarOperacion("Abriendo gestión de usuarios...");
        try {
            new VentanaUsuarios(gestor).setVisible(true);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== AUXILIARES =====================

    private void iniciarOperacion(String texto) {
        barraProgreso.setIndeterminate(true);
        barraProgreso.setString(texto);
        accionGuardar.setEnabled(false);
        accionRefrescar.setEnabled(false);
        accionUsuarios.setEnabled(false);
        barraProgreso.paintImmediately(new Rectangle(barraProgreso.getSize())); // se dibuja antes de consultar
    }

    private void finalizarOperacion() {
        barraProgreso.setIndeterminate(false);
        barraProgreso.setString("Listo");
        accionGuardar.setEnabled(true);
        accionRefrescar.setEnabled(true);
        accionUsuarios.setEnabled(true);
    }

    private void limpiarFormulario() {
        comboTipoCliente.setSelectedIndex(0);
        campoNombre.setText("");
        spinnerIngresos.setValue(0.0);
        spinnerGastos.setValue(0.0);
        checkHistorial.setSelected(false);
        campoRubro.setText("");
        campoDirector.setText("");
    }

    // Sirve tanto si evaluarRiesgo() devuelve boolean como si devuelve un número
    private String textoResultado(Object resultado) {
        if (resultado instanceof Boolean) {
            return ((Boolean) resultado) ? "Apto" : "No apto";
        }
        return String.valueOf(resultado);
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}