//Rol 3: Interfaces y clases DAO

// Paquete DAO
package DAO;

import java.sql.Connection;
import java.util.List;

/**
 * Interfaz genérica (estilo cátedra).
 * Define métodos comunes para operaciones CRUD y transacciones.
 * Lanza 'Exception' para ser consistente con el ejemplo.
 */
public interface GenericDAO<T> {

    /**
     * Inserta una entidad, creando y cerrando su propia conexión.
     */
    void insertar(T entidad) throws Exception;

    /**
     * Inserta una entidad usando una conexión externa (para transacciones).
     */
    void insertTx(T entidad, Connection conn) throws Exception;

    /**
     * Actualiza una entidad, creando y cerrando su propia conexión.
     */
    void actualizar(T entidad) throws Exception;

    /**
     * Elimina (lógicamente) una entidad por su ID.
     */
    void eliminar(int id) throws Exception;

    /**
     * Obtiene una entidad por su ID.
     */
    T getById(int id) throws Exception;

    /**
     * Obtiene todas las entidades activas.
     */
    List<T> getAll() throws Exception;
}
