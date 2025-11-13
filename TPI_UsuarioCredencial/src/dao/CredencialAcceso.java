// Paquete DAO
package DAO;

// Asumo que la clase de conexión está en un paquete 'Config'
import Config.DatabaseConnection; 
import Models.CredencialAcceso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad CredencialAcceso.
 * Sigue el patrón de la cátedra (DomicilioDAO).
 *
 * Características:
 * - Implementa GenericDAO<CredencialAcceso>.
 * - Es más simple, no usa JOINs para cargar.
 * - Implementa 'soft delete'.
 * - Obtiene IDs generados (RETURN_GENERATED_KEYS).
 * - Proporciona métodos con conexión propia y métodos '...Tx'.
 * - Incluye búsqueda especializada por 'id_usuario' (único).
 */
public class CredencialAccesoDAO implements GenericDAO<CredencialAcceso> {

    // --- QUERIES ESTATICAS ---

    private static final String INSERT_SQL = "INSERT INTO credencial (contraseña, salt, id_usuario) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE credencial SET contraseña = ?, salt = ?, require_reset = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE credencial SET eliminado = true WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM credencial WHERE id = ? AND eliminado = false";
    private static final String SELECT_ALL_SQL = "SELECT * FROM credencial WHERE eliminado = false";
    
    /**
     * Query para buscar por 'id_usuario' (que es UNIQUE).
     * Crucial para la lógica de negocio.
     */
    private static final String SELECT_BY_ID_USUARIO_SQL = "SELECT * FROM credencial WHERE id_usuario = ? AND eliminado = false";

    // --- IMPLEMENTACIÓN GenericDAO ---

    @Override
    public void insertar(CredencialAcceso credencial) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setCredencialParameters(stmt, credencial);
            stmt.executeUpdate();
            setGeneratedId(stmt, credencial);
        }
    }

    @Override
    public void insertTx(CredencialAcceso credencial, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setCredencialParameters(stmt, credencial);
            stmt.executeUpdate();
            setGeneratedId(stmt, credencial);
        }
    }

    @Override
    public void actualizar(CredencialAcceso credencial) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, credencial.getHashPassword());
            stmt.setString(2, credencial.getSalt());
            stmt.setBoolean(3, credencial.getRequiereReset());
            stmt.setInt(4, credencial.getId()); // ID en el WHERE
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar la credencial con ID: " + credencial.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró credencial con ID: " + id);
            }
        }
    }

    @Override
    public CredencialAcceso getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCredencial(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<CredencialAcceso> getAll() throws Exception {
        List<CredencialAcceso> credenciales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                credenciales.add(mapResultSetToCredencial(rs));
            }
        }
        return credenciales;
    }

    // --- MÉTODOS ESPECIALIZADOS ---

    /**
     * Busca una credencial usando el ID del Usuario (que es UNIQUE).
     */
    public CredencialAcceso getByIdUsuario(int idUsuario) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_USUARIO_SQL)) {
            
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCredencial(rs);
                }
            }
        }
        return null;
    }

    // --- MÉTODOS PRIVADOS (HELPERS) ---

    /**
     * Método helper para setear parámetros de INSERT.
     */
    private void setCredencialParameters(PreparedStatement stmt, CredencialAcceso credencial) throws SQLException {
        stmt.setString(1, credencial.getHashPassword());
        stmt.setString(2, credencial.getSalt());
        stmt.setInt(3, credencial.getIdUsuario()); // FK del usuario
    }

    /**
     * Método helper para obtener el ID auto-generado y asignarlo al objeto.
     * Nota: Tu clase Base debe tener 'setId(int id)'
     */
    private void setGeneratedId(PreparedStatement stmt, CredencialAcceso credencial) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                credencial.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción falló, no se obtuvo ID.");
            }
        }
    }

    /**
     * Método helper para "mapear" el ResultSet a un objeto CredencialAcceso.
     */
    private CredencialAcceso mapResultSetToCredencial(ResultSet rs) throws SQLException {
        CredencialAcceso cred = new CredencialAcceso();
        
        // Campos Base
        cred.setId(rs.getInt("id"));
        cred.setEliminado(rs.getBoolean("eliminado"));
        
        // Campos CredencialAcceso
        cred.setHashPassword(rs.getString("contraseña"));
        cred.setSalt(rs.getString("salt"));
        cred.setUltimoCambio(rs.getTimestamp("ultimo_cambio").toLocalDateTime());
        cred.setRequiereReset(rs.getBoolean("require_reset"));
        cred.setIdUsuario(rs.getInt("id_usuario"));
        
        return cred;
    }
}
