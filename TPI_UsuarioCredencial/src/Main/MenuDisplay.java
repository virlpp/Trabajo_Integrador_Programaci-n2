// Funciona como el "Locutor". Su única responsabilidad es imprimir texto .
// en la consola. No tiene Scanner, no tiene lógica de negocio, ni llama a los DAO.
// Es una clase "tonta" que solo sirve como una biblioteca de mensajes, no depende de otra clase.

package Main;

import Models.Usuario;
import java.util.List;

/**
 * Clase "Locutor" o "Vista".
 * ÚNICA RESPONSABILIDAD: Imprimir texto en la consola.
 * No contiene lógica, ni Scanners, ni toma de decisiones.
 * Solo ofrece métodos para que otras clases (como AppMenu y MenuHandler)
 * los usen para mostrar información al usuario.
 */
public class MenuDisplay {

    // --- Menús Principales ---

    // Método mostrar Header sobrecargado
    
    // 1. EL MÉTODO QUE USA 'AppMenu.java' (El original)
    // (Este es el que probablemente te falta ahora)
    public void mostrarHeader() {
        System.out.println("=============================================");
        System.out.println("  SISTEMA DE GESTIÓN DE USUARIOS (v1.0)");
        System.out.println("=============================================");
    }
    
    // 2. EL MÉTODO QUE USA 'MenuHandler.java' (El que agregamos)
    public void mostrarHeader(String titulo) {
        // Lo ponemos en mayúsculas y con separadores
        System.out.println("\n=============================================");
        System.out.println("  " + titulo.toUpperCase());
        System.out.println("=============================================");
    }

    public void mostrarMenuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Crear nuevo Usuario (con Credencial)");
        System.out.println("2. Listar todos los Usuarios");
        System.out.println("3. Buscar Usuario por Username");
        System.out.println("4. Actualizar datos de Usuario");
        System.out.println("5. Eliminar Usuario (Baja lógica)");
        System.out.println("---------------------------------------------");
        System.out.println("0. Salir");
    }

    // --- Prompts (Peticiones de datos) ---

    public void pedirOpcion() {
        System.out.print("\nPor favor, ingrese una opción: ");
    }
    
    public void pedirNombre() {
        System.out.print("Ingrese el Nombre: ");
    }

    public void pedirApellido() {
        System.out.print("Ingrese el Apellido: ");
    }

    public void pedirUsername() {
        System.out.print("Ingrese el Username (ej: juanperez): ");
    }

    public void pedirEmail() {
        System.out.print("Ingrese el Email (ej: juan@correo.com): ");
    }

    public void pedirPassword() {
        System.out.print("Ingrese la Contraseña para este usuario: ");
    }
    
    public void pedirIdPara(String accion) {
        // Reutilizable para "Actualizar", "Eliminar", etc.
        System.out.print("Ingrese el ID del usuario que desea " + accion + ": ");
    }
    
    public void pedirUsernameParaBuscar() {
        System.out.print("Ingrese el Username del usuario que desea buscar: ");
    }

    // --- Feedback (Éxito, Error, Info) ---

    /**
     * Muestra un mensaje de éxito genérico.
     * @param mensaje El mensaje a mostrar.
     */
    public void mostrarExito(String mensaje) {
        System.out.println("\n✅ ¡ÉXITO! " + mensaje);
    }

    /**
     * Muestra un mensaje de error genérico.
     * @param mensaje El mensaje a mostrar.
     */
    public void mostrarError(String mensaje) {
        System.err.println("\n❌ ¡ERROR! " + mensaje);
    }
    
    /**
     * Muestra un mensaje de advertencia genérico.
     * @param mensaje El mensaje a mostrar.
     */
    public void mostrarAdvertencia(String mensaje) {
        System.out.println("\n⚠️ AVISO: " + mensaje);
    }
    
    public void mostrarOpcionInvalida() {
        System.err.println("Opción no válida. Por favor, intente de nuevo.");
    }
    
    public void mostrarSalida() {
        System.out.println("\n=============================================");
        System.out.println("  Saliendo del programa. ¡Hasta luego!");
        System.out.println("=============================================");
    }
    
    public void presioneEnterParaContinuar() {
        System.out.println("\n(Presione Enter para continuar...)");
    }

    // --- Visualización de Datos ---

    public void mostrarHeaderListaUsuarios() {
        System.out.println("\n--- LISTA DE USUARIOS ACTIVOS ---");
    }

    /**
     * Muestra los detalles de un único objeto Usuario.
     * Es llamado por el MenuHandler dentro de un bucle.
     * @param usuario El usuario a mostrar.
     */
    public void mostrarUsuario(Usuario usuario) {
        System.out.println("---------------------------------");
        System.out.println("  ID: \t\t" + usuario.getId());
        System.out.println("  Nombre: \t" + usuario.getNombre() + " " + usuario.getApellido());
        System.out.println("  Username: \t" + usuario.getUsername());
        System.out.println("  Email: \t" + usuario.getEmail());
        System.out.println("  Activo: \t" + (usuario.getActivo() ? "Sí" : "No"));
        
        // Verificamos si la credencial fue cargada (Eager Loading)
        if (usuario.getCredencial() != null) {
            System.out.println("  Credencial: \t¡Cargada!");
        } else {
            System.out.println("  Credencial: \t(No asignada)");
        }
    }
    
    public void mostrarMultiplesUsuarios(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            mostrarAdvertencia("No se encontraron usuarios.");
            return;
        }
        
        mostrarHeaderListaUsuarios();
        for (Usuario u : usuarios) {
            mostrarUsuario(u);
        }
    }
}
