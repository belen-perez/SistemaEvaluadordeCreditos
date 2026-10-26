package com.mycompany.evaluadorcreditos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorBDD {

    private final String URL = "jdbc:mysql://localhost:3306/evaluador_creditos";
    private final String USER = "root";         
    private final String PASS = "12341234";   

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
       

    public List<Object[]> listarUsuarios() throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre_usuario FROM usuarios ORDER BY id_usuario";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[2];
                fila[0] = rs.getInt("id_usuario");
                fila[1] = rs.getString("nombre_usuario");
                lista.add(fila);
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
}
