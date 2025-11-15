package Main;

import Models.CredencialAcceso;
import Models.Usuario;
import Service.UsuarioServiceImpl; // <-- Importa el "cerebro"

import java.time.LocalDateTime; // Necesario para los constructores
import java.util.List;
import java.util.Scanner;

/**
 * Clase "Recepcionista" o "Manejador".
 *
 * RESPONSABILIDAD:
 * 1. Implementar la lógica de CADA opción del menú.
 * 2. Usar un 'Scanner' para capturar la entrada del usuario.
 * 3. Usar 'MenuDisplay' para mostrar los mensajes y pedir datos.
 * 4. Llamar a la capa de 'Servicio' (UsuarioServiceImpl) para ejecutar las acciones.
 * 5. Capturar y manejar las 'Exceptions' que pueda lanzar el Servicio.
 */
public class MenuHandler {

    // Dependencias (las recibe en el constructor)
    private final MenuDisplay display;
    private final UsuarioServiceImpl usuarioService;
    private final Scanner scanner;

    /**
     * Constructor para Inyección de Dependencias.
     * Recibe las herramientas que necesita para trabajar.
     *
     * @param display El "locutor" (para imprimir).
     * @param usuarioService El "gerente" (para la lógica).
     * @param scanner El 'Scanner' para leer la entrada.
     */
    public MenuHandler(MenuDisplay display, UsuarioServiceImpl usuarioService, Scanner scanner) {
        this.display = display;
        this.usuarioService = usuarioService;
        this.scanner = scanner;
    }

    /**
     * Lógica para la Opción 1: Crear Usuario y Credencial.
     */
    public void crearUsuario() {
        display.mostrarHeader("CREAR NUEVO USUARIO"); // Método ficticio para un header
        
        try {
            // 1. Pedir datos del Usuario
            display.pedirNombre();
            String nombre = scanner.nextLine();
            display.pedirApellido();
            String apellido = scanner.nextLine();
            display.pedirUsername();
            String username = scanner.nextLine();
            display.pedirEmail();
            String email = scanner.nextLine();
            
            // 2. Pedir datos de la Credencial
            display.pedirPassword();
            String password = scanner.nextLine();
            // (Aquí, en un proyecto real, generarías el HASH y el SALT)
            // Por ahora, lo guardamos en texto plano para la prueba.
            
            // 3. Crear los objetos Modelo (POJOs)
            // Se usan los constructores que ya definimos
            Usuario nuevoUsuario = new Usuario(nombre, apellido, username, email, true, LocalDateTime.now(), 0, false);
            CredencialAcceso nuevaCredencial = new CredencialAcceso(password, "salt_de_prueba", LocalDateTime.now(), false, 0, 0, false);
            
            // 4. Llamar al "cerebro" (Servicio)
            usuarioService.crearUsuarioConCredencial(nuevoUsuario, nuevaCredencial);
            
            // 5. Informar éxito
            display.mostrarExito("¡Usuario '" + username + "' creado con éxito!");

        } catch (Exception e) {
            // Si el Servicio lanza un error (ej: username duplicado), lo mostramos
            display.mostrarError(e.getMessage());
        }
        
        pausarHastaEnter();
    }

    /**
     * Lógica para la Opción 2: Listar todos los Usuarios.
     */
    public void listarUsuarios() {
        display.mostrarHeaderListaUsuarios();
        
        try {
            // 1. Llamar al servicio
            List<Usuario> usuarios = usuarioService.getAll();
            
            // 2. Usar el display para mostrarlos
            display.mostrarMultiplesUsuarios(usuarios);
            
        } catch (Exception e) {
            display.mostrarError("No se pudieron cargar los usuarios: " + e.getMessage());
        }
        
        pausarHastaEnter();
    }

    /**
     * Lógica para la Opción 3: Buscar por Username.
     */
    public void buscarUsuarioPorUsername() {
        display.pedirUsernameParaBuscar();
        String username = scanner.nextLine();
        
        try {
            // 1. Llamar al servicio
            Usuario usuario = usuarioService.getByUsername(username);
            
            // 2. Mostrar
            if (usuario != null) {
                display.mostrarHeader("USUARIO ENCONTRADO");
                display.mostrarUsuario(usuario); // Muestra el usuario individual
            } else {
                display.mostrarAdvertencia("No se encontró ningún usuario con el username '" + username + "'.");
            }
            
        } catch (Exception e) {
            display.mostrarError("Error en la búsqueda: " + e.getMessage());
        }
        
        pausarHastaEnter();
    }

    /**
     * Lógica para la Opción 4: Actualizar Usuario.
     */
    public void actualizarUsuario() {
        display.pedirIdPara("actualizar");
        int id = leerIdSeguro(); // Usamos un helper para leer el ID
        if (id == -1) return; // El helper ya mostró el error

        try {
            // 1. Buscar al usuario primero
            Usuario usuario = usuarioService.getById(id);
            if (usuario == null) {
                display.mostrarError("No existe un usuario con ID " + id);
                pausarHastaEnter();
                return;
            }

            // 2. Mostrar datos actuales y pedir los nuevos
            display.mostrarUsuario(usuario);
            System.out.println("\nIngrese los nuevos datos (deje en blanco para no cambiar):");

            display.pedirNombre();
            String nombre = scanner.nextLine();
            if (!nombre.isBlank()) usuario.setNombre(nombre);

            display.pedirApellido();
            String apellido = scanner.nextLine();
            if (!apellido.isBlank()) usuario.setApellido(apellido);

            display.pedirEmail();
            String email = scanner.nextLine();
            if (!email.isBlank()) usuario.setEmail(email);

            // 3. Llamar al servicio para actualizar
            usuarioService.actualizar(usuario);
            display.mostrarExito("Usuario ID " + id + " actualizado correctamente.");

        } catch (Exception e) {
            display.mostrarError("Error al actualizar: " + e.getMessage());
        }
        
        pausarHastaEnter();
    }

    /**
     * Lógica para la Opción 5: Eliminar Usuario.
     */
    public void eliminarUsuario() {
        display.pedirIdPara("eliminar");
        int id = leerIdSeguro();
        if (id == -1) return;

        try {
            // 1. Validar que existe
            Usuario usuario = usuarioService.getById(id);
            if (usuario == null) {
                display.mostrarError("No existe un usuario con ID " + id);
                pausarHastaEnter();
                return;
            }
            
            // 2. Pedir confirmación
            display.mostrarAdvertencia("ADVERTENCIA: Está a punto de eliminar (baja lógica) a:");
            display.mostrarUsuario(usuario);
            System.out.print("¿Está seguro? (Escriba 'si' para confirmar): ");
            String confirmacion = scanner.nextLine();

            // 3. Llamar al servicio si confirma
            if (confirmacion.equalsIgnoreCase("si")) {
                usuarioService.eliminar(id);
                display.mostrarExito("Usuario " + usuario.getUsername() + " (ID " + id + ") eliminado correctamente.");
            } else {
                display.mostrarAdvertencia("Eliminación cancelada.");
            }

        } catch (Exception e) {
            display.mostrarError("Error al eliminar: " + e.getMessage());
        }
        
        pausarHastaEnter();
    }

    // --- Métodos Helper Internos ---

    /**
     * Pausa la ejecución hasta que el usuario presione Enter.
     */
    private void pausarHastaEnter() {
        display.presioneEnterParaContinuar();
        scanner.nextLine();
    }

    /**
     * Lee un ID del scanner y se asegura de que sea un número válido.
     * @return El ID como int, o -1 si la entrada fue inválida.
     */
    private int leerIdSeguro() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            display.mostrarError("Entrada inválida. Se esperaba un número.");
            return -1;
        }
    }
}
