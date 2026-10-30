package com.mycompany.evaluadorcreditos.vista;

import com.mycompany.evaluadorcreditos.modelo.Usuario;
import com.mycompany.evaluadorcreditos.persistencia.GestorBDD;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class VentanaUsuarios extends JFrame {

    private GestorBDD gestor;

    private JTextField campoNombreUsuario;
    private JPasswordField campoClave;
    private JButton botonCrearUsuario;
    private JButton botonEliminarUsuario;
    private JButton botonRefrescar;
    private JProgressBar barraProgreso;

    private DefaultTableModel modeloUsuarios;
    private JTable tablaUsuarios;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPopupMenu menuContextual;

    public VentanaUsuarios(GestorBDD gestor) {
        this.gestor = gestor;

        setTitle("Gestión de usuarios");
        setSize(450, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana

        // VERTICAL_SPLIT = un panel arriba y otro abajo
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                crearPanelAlta(), crearPanelListado());
        split.setDividerLocation(150);
        split.setOneTouchExpandable(true);

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");

        setLayout(new BorderLayout());
        add(split, BorderLayout.CENTER);
        add(barraProgreso, BorderLayout.SOUTH);

        refrescarListado();
    }

    // ===================== PANEL SUPERIOR: ALTA =====================

    private JPanel crearPanelAlta() {
        campoNombreUsuario = new JTextField(15);
        campoClave = new JPasswordField(15);
        botonCrearUsuario = new JButton("Crear usuario");
        botonCrearUsuario.addActionListener(e -> crearUsuario());

        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.add(new JLabel("Nombre de usuario:"));
        campos.add(campoNombreUsuario);
        campos.add(new JLabel("Clave:"));
        campos.add(campoClave);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(botonCrearUsuario);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo usuario"));
        panel.add(campos, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        return panel;
    }

    // ===================== PANEL INFERIOR: LISTADO =====================

    private JPanel crearPanelListado() {
        modeloUsuarios = new DefaultTableModel(new String[]{"ID", "Usuario"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };

        tablaUsuarios = new JTable(modeloUsuarios);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ordenamiento por columna al hacer clic en el encabezado
        sorter = new TableRowSorter<>(modeloUsuarios);
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        tablaUsuarios.setRowSorter(sorter);

        // Oculta la columna ID en pantalla (el dato sigue en el modelo)
        tablaUsuarios.removeColumn(tablaUsuarios.getColumnModel().getColumn(0));

        // Menú contextual (clic derecho)
        menuContextual = new JPopupMenu();
        JMenuItem itemEliminar = new JMenuItem("Eliminar usuario");
        itemEliminar.addActionListener(e -> eliminarUsuarioSeleccionado()); // misma rutina que el botón
        JMenuItem itemRefrescar = new JMenuItem("Refrescar listado");
        itemRefrescar.addActionListener(e -> refrescarListado());
        menuContextual.add(itemEliminar);
        menuContextual.add(itemRefrescar);

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenu(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenu(e);
            }
            private void mostrarMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int fila = tablaUsuarios.rowAtPoint(e.getPoint());
                    if (fila >= 0) {
                        tablaUsuarios.setRowSelectionInterval(fila, fila);
                    }
                    menuContextual.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        botonRefrescar = new JButton("Refrescar");
        botonRefrescar.addActionListener(e -> refrescarListado());
        botonEliminarUsuario = new JButton("Eliminar seleccionado");
        botonEliminarUsuario.addActionListener(e -> eliminarUsuarioSeleccionado());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(botonRefrescar);
        panelBotones.add(botonEliminarUsuario);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Usuarios registrados"));
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    // ===================== LISTAR =====================

    private void refrescarListado() {
        iniciarOperacion("Cargando usuarios...");
        try {
            List<Usuario> usuarios = gestor.listarUsuarios();
            modeloUsuarios.setRowCount(0);
            for (Usuario u : usuarios) {
                modeloUsuarios.addRow(new Object[]{u.getId(), u.getNombreUsuario()});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar el listado de usuarios", ex);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== CREAR =====================

    private void crearUsuario() {
        String nombre = campoNombreUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete nombre de usuario y clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        iniciarOperacion("Creando usuario...");
        try {
            gestor.insertarUsuario(nombre, clave);
            JOptionPane.showMessageDialog(this, "Usuario \"" + nombre + "\" creado.",
                    "Alta exitosa", JOptionPane.INFORMATION_MESSAGE);
            campoNombreUsuario.setText("");
            campoClave.setText("");
            refrescarListado();
        } catch (SQLIntegrityConstraintViolationException ex) {
            // Salta por la restricción UNIQUE sobre nombre_usuario
            JOptionPane.showMessageDialog(this, "Ese nombre de usuario ya existe. Elija otro.",
                    "Usuario duplicado", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            mostrarError("No se pudo crear el usuario", ex);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== ELIMINAR =====================

    private void eliminarUsuarioSeleccionado() {
        int filaVista = tablaUsuarios.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Convierte la fila visible a la fila real del modelo (necesario por el ordenamiento)
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        int id = (Integer) modeloUsuarios.getValueAt(filaModelo, 0);
        String nombre = (String) modeloUsuarios.getValueAt(filaModelo, 1);

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el usuario \"" + nombre + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        iniciarOperacion("Eliminando usuario...");
        try {
            boolean eliminado = gestor.eliminarUsuario(id);
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.",
                        "Baja exitosa", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "El usuario ya no existía en la base. Se actualiza el listado.",
                        "Sin cambios", JOptionPane.WARNING_MESSAGE);
            }
            refrescarListado();
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar el usuario", ex);
        } finally {
            finalizarOperacion();
        }
    }

    // ===================== AUXILIARES =====================

    private void iniciarOperacion(String texto) {
        barraProgreso.setIndeterminate(true);
        barraProgreso.setString(texto);
        botonCrearUsuario.setEnabled(false);
        botonEliminarUsuario.setEnabled(false);
        botonRefrescar.setEnabled(false);
        barraProgreso.paintImmediately(new Rectangle(barraProgreso.getSize()));
    }

    private void finalizarOperacion() {
        barraProgreso.setIndeterminate(false);
        barraProgreso.setString("Listo");
        botonCrearUsuario.setEnabled(true);
        botonEliminarUsuario.setEnabled(true);
        botonRefrescar.setEnabled(true);
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}