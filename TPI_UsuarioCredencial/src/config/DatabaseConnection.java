
package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import DAO.UsuarioDAO;
import Models.Usuario;

/**
 * Clase utilitaria para gestionar conexiones a la base de datos MySQL.
 *
 * Patrón: Factory con configuración estática
 * - No se puede instanciar (constructor privado)
 * - Proporciona conexiones mediante método estático getConnection()
 * - Configuración cargada una sola vez en bloque static
 *
 * Configuración por defecto:
 * - URL: jdbc:mysql://localhost:3306/dbtpi3
 * - Usuario: root
 * - Contraseña: vacía (común en desarrollo local)
 *
 * Override mediante system properties:
 * - java -Ddb.url=... -Ddb.user=... -Ddb.password=...
 */
public final class DatabaseConnection {
    
    
    /** URL de conexión JDBC. */
    // He agregado "jdbc:mysql://" al principio, los dos puntos ":" para el puerto
    // y la barra "/" para el nombre de la base de datos.
    private static final String URL = System.getProperty("db.url", 
        "jdbc:mysql://mysql-1e724b1f-bd2025.d.aivencloud.com:27115/usuariocredencial?sslMode=REQUIRED");

    /** Usuario. En Aiven suele ser "avnadmin". */
    private static final String USER = System.getProperty("db.user", "avnadmin"); 

    /** Contraseña. En Aiven es obligatoria, no puede estar vacía. */
    private static final String PASSWORD = System.getProperty("db.password", "");


    /**
     * Bloque de inicialización estática.
     * Se ejecuta UNA SOLA VEZ cuando la clase se carga en memoria.
     *
     * Acciones:
     * 1. Carga el driver JDBC de MySQL
     * 2. Valida que la configuración sea correcta
     *
     * Si falla, lanza ExceptionInInitializerError y detiene la aplicación.
     * Esto es intencional: sin BD correcta, la app no puede funcionar.
     */
    static {
        try {
            // Carga explícita del driver (requerido en algunas versiones de Java)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Valida configuración tempranamente (fail-fast)
            validateConfiguration();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error: No se encontró el driver JDBC de MySQL: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError("Error en la configuración de la base de datos: " + e.getMessage());
        }
    }

    /**
     * Constructor privado para prevenir instanciación.
     * Esta es una clase utilitaria con solo métodos estáticos.
     */
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     *
     * Importante:
     * - Cada llamada crea una NUEVA conexión (no hay pooling)
     * - El caller es responsable de cerrar la conexión (usar try-with-resources)
     * - La configuración ya fue validada en el bloque static
     *
     * Uso correcto:
     * <pre>
     * try (Connection conn = DatabaseConnection.getConnection()) {
     *     // usar conexión
     * } // se cierra automáticamente
     * </pre>
     *
     * @return Conexión JDBC activa
     * @throws SQLException Si no se puede establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Valida que los parámetros de configuración sean válidos.
     * Llamado una sola vez desde el bloque static.
     *
     * Reglas:
     * - URL y USER no pueden ser null ni estar vacíos
     * - PASSWORD puede ser vacío (común en MySQL local root sin password)
     * - PASSWORD no puede ser null
     *
     * @throws IllegalStateException Si la configuración es inválida
     */
    private static void validateConfiguration() {
        if (URL == null || URL.trim().isEmpty()) {
            throw new IllegalStateException("La URL de la base de datos no está configurada");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("El usuario de la base de datos no está configurado");
        }
        // PASSWORD puede ser vacío (común en MySQL local con usuario root sin contraseña)
        // Solo validamos que no sea null
        if (PASSWORD == null) {
            throw new IllegalStateException("La contraseña de la base de datos no está configurada");
        }
    }
    
        //*********** Prueba de conexión a la base de datos ********

//    // 👇👇 PEGA EL MÉTODO 'MAIN' AQUÍ 👇👇
//    /**
//     * Main de prueba para verificar la conexión.
//     * EJECUTAR ESTE ARCHIVO (Run File) para probar.
//     */
//    public static void main(String[] args) {
//        System.out.println("Intentando conectar a la base de datos...");
//        
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            
//            if (conn != null) {
//                System.out.println("======================================");
//                System.out.println("¡¡CONEXIÓN EXITOSA!!");
//                System.out.println("Conectado a la base de datos: " + conn.getCatalog());
//                System.out.println("======================================");
//            }
//            
//        } catch (SQLException e) {
//            System.out.println("======================================");
//            System.out.println("¡¡ERROR AL CONECTAR!!");
//            System.out.println("======================================");
//            e.printStackTrace(); 
//        }
//    }
//    // 👆👆 AQUÍ TERMINA EL MÉTODO 'MAIN' 👆👆
    
    
    //*********** Prueba de muestra de registros obtenidos de la DB ********
    
//    /**
//     * Main de prueba para verificar DAO.
//     * EJECUTAR ESTE ARCHIVO (Run File) para probar.
//     */
//    public static void main(String[] args) {
//        System.out.println("---[ Prueba de DAO ]---");
//        
//        // Ahora los imports (arriba del todo) deberían usarse
//        
//        try {
//            // 2. Crea una instancia de tu DAO (usando el nombre corto)
//            UsuarioDAO miUsuarioDAO = new UsuarioDAO();
//            
//            System.out.println("Intentando llamar a UsuarioDAO.getAll()...");
//            
//            // 3. Llama al método (usando el nombre corto)
//            List<Usuario> usuarios = miUsuarioDAO.getAll();
//            
//            // 4. Muestra los resultados
//            if (usuarios.isEmpty()) {
//                System.out.println("======================================");
//                System.out.println("¡CONEXIÓN EXITOSA!");
//                System.out.println("La consulta funcionó, pero no se encontraron usuarios.");
//                System.out.println("======================================");
//            } else {
//                System.out.println("======================================");
//                System.out.println("¡¡CONEXIÓN Y LECTURA EXITOSAS!!");
//                System.out.println("Se encontraron " + usuarios.size() + " usuarios:");
//                
//                // (Usando el nombre corto)
//                for (Usuario u : usuarios) {
//                    System.out.println("--------------------");
//                    System.out.println("  ID: " + u.getId());
//                    System.out.println("  Nombre: " + u.getNombre());
//                    System.out.println("  Username: " + u.getUsername());
//                    
//                    // Prueba del Eager Loading
//                    if (u.getCredencial() != null) {
//                        System.out.println("  Credencial: ¡Cargada exitosamente! (ID: " + u.getCredencial().getId() + ")");
//                    } else {
//                        System.out.println("  Credencial: No encontrada (o sin credencial).");
//                    }
//                }
//                System.out.println("======================================");
//            }
//            
//        } catch (Exception e) {
//            System.out.println("======================================");
//            System.out.println("¡¡ERROR EN EL DAO!!");
//            System.out.println("======================================");
//            e.printStackTrace(); 
//        }
//    }
//    
}
