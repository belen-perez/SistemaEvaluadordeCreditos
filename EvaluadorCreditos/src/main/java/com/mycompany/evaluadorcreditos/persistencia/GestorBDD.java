package com.mycompany.evaluadorcreditos.persistencia;
import com.mycompany.evaluadorcreditos.modelo.Cliente;
import com.mycompany.evaluadorcreditos.modelo.Empresa;
import com.mycompany.evaluadorcreditos.modelo.PersonaFisica;
import com.mycompany.evaluadorcreditos.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorBDD {

    private final String URL = "jdbc:mysql://localhost:3306/evaluador_creditos";
    private final String USER = "belen_dev";         
    private final String PASS = "usr_MST_labo26";   

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public int guardarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, ingresos, gastos, historial) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setDouble(2, cliente.getIngresos());
            ps.setDouble(3, cliente.getGastos());
            ps.setBoolean(4, cliente.isTieneBuenHistorial());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);

                    // Guardar en la tabla hija según el tipo
                    if (cliente instanceof PersonaFisica) {
                        guardarPersonaFisica(idGenerado);
                    } else if (cliente instanceof Empresa) {
                        Empresa emp = (Empresa) cliente;
                        guardarEmpresa(idGenerado, emp.getRubro(), emp.getDirector());
                    }

                    return idGenerado;
                }
            }
        }
        return -1; 
    }

    private void guardarPersonaFisica(int idCliente) throws SQLException {
        String sql = "INSERT INTO personas_fisicas (id_cliente) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    private void guardarEmpresa(int idCliente, String rubro, String director) throws SQLException {
        String sql = "INSERT INTO empresas (id_cliente, rubro, director) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, rubro);
            ps.setString(3, director);
            ps.executeUpdate();
        }
    }

    public boolean validarAcceso(String nombreUsuario, String clave) throws SQLException {
        String sql = "SELECT id_usuario FROM usuarios WHERE nombre_usuario = ? AND clave = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, clave);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        }
    }


        public void insertarUsuario(String nombreUsuario, String clave) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre_usuario, clave) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, clave);
            ps.executeUpdate();
        }
    }

  public List<Usuario> listarUsuarios() throws SQLException {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT id_usuario, nombre_usuario FROM usuarios ORDER BY id_usuario";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("id_usuario");
            String nombreUsuario = rs.getString("nombre_usuario");

            Usuario usuario = new Usuario(id, nombreUsuario);
            lista.add(usuario);
        }
    }
    return lista;
}

    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

public List<Object[]> listarClientes() throws SQLException {
    List<Object[]> lista = new ArrayList<>();

    String sql = "SELECT c.id_cliente, c.nombre, c.ingresos, c.gastos, c.historial, " +
                 "CASE " +
                 "WHEN e.id_cliente IS NOT NULL THEN 'Empresa' " +
                 "ELSE 'Persona Física' " +
                 "END AS tipo " +
                 "FROM clientes c " +
                 "LEFT JOIN empresas e ON c.id_cliente = e.id_cliente " +
                 "LEFT JOIN personas_fisicas pf ON c.id_cliente = pf.id_cliente " +
                 "ORDER BY c.id_cliente";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Object[] fila = {
                rs.getInt("id_cliente"),
                rs.getString("nombre"),
                rs.getDouble("ingresos"),
                rs.getDouble("gastos"),
                rs.getBoolean("historial"),
                rs.getString("tipo")
            };

            lista.add(fila);
        }
    }

    return lista;
}
}

