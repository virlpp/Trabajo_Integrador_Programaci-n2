
package Models;

/**
 *
 * @author USER
 */
public abstract class Base {
    private int id; //Identificador único
    private Boolean eliminado; //Marca en la base de datos cuando un elemento es eliminado de forma lógica

    //Constructor
    public Base(int id, Boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }

    //Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    //Constructor vacio
    public Base() {
    }
    
    
}
