package Main;

/**
 * Punto de entrada principal de la aplicación (El "Botón de Encendido").
 *
 * RESPONSABILIDAD:
 * Únicamente arrancar la aplicación, instanciando y ejecutando el
 * orquestador del menú (AppMenu).
 */
public class Main {

    /**
     * El método main que inicia toda la aplicación.
     * @param args Argumentos de línea de comandos (no se usan en este proyecto).
     */
    public static void main(String[] args) {
        
        // 1. Crea una instancia del "Director de Orquesta"
        AppMenu menuPrincipal = new AppMenu();
        
        // 2. Le da la orden de empezar
        menuPrincipal.run();
    }
}
