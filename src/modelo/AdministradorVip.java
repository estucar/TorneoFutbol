
package modelo;

import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity 
public class AdministradorVip extends Usuario {
    
    @Basic
    private String nivelAcceso;

    public AdministradorVip() {
        super();
    }

    public AdministradorVip(int id, long dni, String nombre, String apellido, String email, String contrasenia, String nivelAcceso) {
        super(id, dni, nombre, apellido, email, contrasenia);
        this.nivelAcceso = nivelAcceso;
    }
    
    public String getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(String nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }
  
}
