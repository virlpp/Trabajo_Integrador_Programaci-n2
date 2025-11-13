// Paquete DAO
package DAO;

//Clases de otras careptas del proyecto
import Config.DatabaseConnection;  // clase de conexión está en un paquete 'Config' -> Emilce
import Models.CredencialAcceso; // Clase CredencialAcceso en Models -> Joana
import Models.Usuario; // Clase CredencialAcceso en Models -> Joana

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Usuario.
 * Sigue el patrón de la cátedra (PersonaDAO).
 *
 * Características:
 * - Implementa GenericDAO<Usuario>.
 * - Usa LEFT JOIN con 'credencial' para Eager Loading o Carga temprana (estrategia de programación para obtener datos) -> cargar los datos principales y todos sus datos relacionados de inmediato, en una sola consulta
 * - Implementa 'soft delete' -> técnica de programación en donde la fila no se borra fisicamente sino que queda como eliminada en una columna especial para ello.
 * - Obtiene IDs generados (RETURN_GENERATED_KEYS).
 * - Proporciona métodos con conexión propia y métodos '...Tx'.
 * - Incluye búsqueda especializada por 'username' (único).
 */
public class UsuarioDAO implements GenericDAO<Usuario> {

    
    
    
    // --- QUERIES ESTATICAS ---
    // 
    
    /**
     * Query de inserción de usuario.
     * Inserta solo los campos de la tabla 'usuario'.
     * 'id' es AUTO_INCREMENT. 'eliminado', 'activo', 'fechaRegistro' tienen DEFAULT.
     */
    private static final String INSERT_SQL = "INSERT INTO usuario (nombre, apellido, username, email) VALUES (?, ?, ?, ?)";

    
    
    /**
     * Query de actualización de usuario.
     * Solo actualiza campos de la tabla 'usuario'.
     */
    private static final String UPDATE_SQL = "UPDATE usuario SET nombre = ?, apellido = ?, username = ?, email = ?, activo = ? WHERE id = ?";

    
    
    /**
     * Query de soft delete (baja lógica).
     * Marca 'eliminado' y 'activo'.
     */
    private static final String DELETE_SQL = "UPDATE usuario SET eliminado = true, activo = false WHERE id = ?";

    
    
    /**
     * Query base para todas las lecturas de Usuario.
     * Usa LEFT JOIN para traer la credencial asociada (Eager Loading).
     * u.* -> todos los campos de usuario
     * c.id AS c_id -> alias para el id de credencial (evita colisión con u.id)
     * c.* -> todos los campos de credencial
     */
    private static final String SELECT_BASE = 
          "FROM usuario u "
        + "LEFT JOIN credencial c ON u.id = c.id_usuario AND c.eliminado = false ";

    
    
    /**
     * Query para obtener usuario por ID.
     * Incluye la credencial (Eager Loading).
     */
    private static final String SELECT_BY_ID_SQL = "SELECT u.*, "
        + "c.id AS c_id, c.contraseña, c.salt, c.ultimo_cambio, c.require_reset, c.id_usuario "
        + SELECT_BASE
        + "WHERE u.id = ? AND u.eliminado = false";

    /**
     * Query para obtener todos los usuarios.
     * Incluye sus credenciales (Eager Loading).
     */
    private static final String SELECT_ALL_SQL = "SELECT u.*, "
        + "c.id AS c_id, c.contraseña, c.salt, c.ultimo_cambio, c.require_reset, c.id_usuario "
        + SELECT_BASE
        + "WHERE u.eliminado = false";

    /**
     * Query para buscar por 'username' (que es UNIQUE).
     * Incluye su credencial (Eager Loading).
     */
    private static final String SELECT_BY_USERNAME_SQL = "SELECT u.*, "
        + "c.id AS c_id, c.contraseña, c.salt, c.ultimo_cambio, c.require_reset, c.id_usuario "
        + SELECT_BASE
        + "WHERE u.username = ? AND u.eliminado = false";


    // --- IMPLEMENTACIÓN GenericDAO ---

    /**
     * Inserta un usuario (versión con conexión propia).
     */
    @Override
    public void insertar(Usuario usuario) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setUsuarioParameters(stmt, usuario);
            stmt.executeUpdate();
            setGeneratedId(stmt, usuario); // Asigna el ID generado al objeto
        }
    }

    /**
     * Inserta un usuario (versión transaccional).
     */
    @Override
    public void insertTx(Usuario usuario, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setUsuarioParameters(stmt, usuario);
            stmt.executeUpdate();
            setGeneratedId(stmt, usuario); // Asigna el ID generado al objeto
        }
    }

    /**
     * Actualiza un usuario (versión con conexión propia).
     */
    @Override
    public void actualizar(Usuario usuario) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            setUsuarioParameters(stmt, usuario); // Reutilizamos el setter de params
            stmt.setBoolean(5, usuario.getActivo()); // Param 5 es 'activo'
            stmt.setInt(6, usuario.getId());       // Param 6 es 'id' en el WHERE
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el usuario con ID: " + usuario.getId());
            }
        }
    }

    /**
     * Elimina lógicamente (soft delete) un usuario por ID.
     */
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró usuario con ID: " + id);
            }
        }
    }

    /**
     * Obtiene un usuario por su ID, incluyendo su credencial.
     */
    @Override
    public Usuario getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null; // No se encontró
    }

    /**
     * Obtiene todos los usuarios activos, incluyendo sus credenciales.
     */
    @Override
    public List<Usuario> getAll() throws Exception {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }
        return usuarios;
    }

    // --- MÉTODOS ESPECIALIZADOS ---

    /**
     * Busca un usuario por su 'username' (que es UNIQUE).
     */
    public Usuario getByUsername(String username) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME_SQL)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null; // No se encontró
    }

    // --- MÉTODOS PRIVADOS (HELPERS) ---

    /**
     * Método helper para setear parámetros de INSERT/UPDATE.
     */
    private void setUsuarioParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getNombre());
        stmt.setString(2, usuario.getApellido());
        stmt.setString(3, usuario.getUsername());
        stmt.setString(4, usuario.getEmail());
    }

    /**
     * Método helper para obtener el ID auto-generado y asignarlo al objeto.
     */
    private void setGeneratedId(PreparedStatement stmt, Usuario usuario) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                usuario.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción falló, no se obtuvo ID.");
            }
        }
    }

    /**
     * Método helper para "mapear" el ResultSet (con JOIN) a un objeto Usuario.
     * Este es el "mapResultSetToPersona" del ejemplo.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        // Mapear campos de Usuario (tabla 'u')
        usuario.setId(rs.getInt("id"));
        usuario.setEliminado(rs.getBoolean("eliminado"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setFechaRegistro(rs.getTimestamp("fechaRegistro").toLocalDateTime());

        // Mapear campos de Credencial (tabla 'c')
        // Usamos el alias 'c_id' para el ID de la credencial
        int credId = rs.getInt("c_id");
        if (!rs.wasNull()) { // ¡Importante! Verifica si el LEFT JOIN trajo algo
            CredencialAcceso credencial = new CredencialAcceso();
            credencial.setId(credId);
            credencial.setHashPassword(rs.getString("contraseña"));
            credencial.setSalt(rs.getString("salt"));
            credencial.setUltimoCambio(rs.getTimestamp("ultimo_cambio").toLocalDateTime());
            credencial.setRequiereReset(rs.getBoolean("require_reset"));
            credencial.setIdUsuario(rs.getInt("id_usuario")); // Este es el FK
            
            // Asignamos la credencial al usuario (Eager Loading)
            usuario.setCredencial(credencial);
        }
        
        return usuario;
    }
}
