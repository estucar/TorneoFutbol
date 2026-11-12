package modelo;

import javax.persistence.Entity;

@Entity
public class Administrador extends AdministradorVip  {

    public Administrador() {
        super();
    }

    public Administrador(int id, long dni, String nombre, String apellido, String email, String contrasenia, String nivelAcceso) {
        super(id, dni, nombre, apellido, email, contrasenia, nivelAcceso);
    }
  
}
