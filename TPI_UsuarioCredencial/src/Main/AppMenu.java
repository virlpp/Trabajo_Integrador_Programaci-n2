package Main;

import Service.UsuarioServiceImpl;
import DAO.CredencialAccesoDAO;
import DAO.UsuarioDAO;
import java.util.Scanner;

/**
 * Clase "Director de Orquesta" o "Motor" del Menú.
 *
 * RESPONSABILIDAD:
 * 1. Inicializar todas las capas (DAOs, Servicios, Handlers).
 * 2. Contener el bucle principal (while) que mantiene la app viva.
 * 3. Usar 'MenuDisplay' para mostrar el menú.
 * 4. Usar 'Scanner' para leer la OPCIÓN del menú.
 * 5. Usar un 'switch' para delegar la acción al 'MenuHandler' correcto.
 */
public class AppMenu {

    // Todas las piezas que la app necesita para funcionar
    private final MenuDisplay display;
    private final MenuHandler handler;
    private final Scanner scanner;
    private final UsuarioServiceImpl usuarioService;
    private final UsuarioDAO usuarioDAO;
    private final CredencialAccesoDAO credencialDAO;

    /**
     * Constructor principal.
     * Aquí se "cablea" toda la aplicación (Inyección de Dependencias).
     */
    public AppMenu() {
        // Inicializa las herramientas
        this.scanner = new Scanner(System.in);
        this.display = new MenuDisplay();

        // Inicializa la capa DAO
        this.usuarioDAO = new UsuarioDAO();
        this.credencialDAO = new CredencialAccesoDAO();

        // Inicializa la capa Service (pasándole los DAOs que necesita)
        this.usuarioService = new UsuarioServiceImpl(usuarioDAO, credencialDAO);

        // Inicializa el Handler (pasándole las herramientas que necesita)
        this.handler = new MenuHandler(display, usuarioService, scanner);
    }

    /**
     * Inicia el bucle principal de la aplicación.
     */
    public void run() {
        display.mostrarHeader();
        boolean running = true;

        // Bucle principal de la aplicación
        while (running) {
            display.mostrarMenuPrincipal();
            display.pedirOpcion();
            
            String opcion = scanner.nextLine();

            // El "switch" que delega el trabajo al handler
            switch (opcion) {
                case "1":
                    handler.crearUsuario();
                    break;
                case "2":
                    handler.listarUsuarios();
                    break;
                case "3":
                    handler.buscarUsuarioPorUsername();
                    break;
                case "4":
                    handler.actualizarUsuario();
                    break;
                case "5":
                    handler.eliminarUsuario();
                    break;
                case "0":
                    running = false; // Termina el bucle
                    break;
                default:
                    display.mostrarOpcionInvalida();
                    break;
            }
        }
        
        display.mostrarSalida();
        scanner.close(); // Cierra el scanner al salir
    }
}
