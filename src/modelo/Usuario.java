
package modelo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private long dni;
    @Basic
    private String nombre;
    @Basic
    private String apellido;
    @Basic
    private String email;
    @Basic
    private String estado;
    @Basic
    private String contrasenia;
    @javax.persistence.Basic
    private String rol;

    public Usuario() {
    }

    public Usuario(int id, long dni, String nombre, String apellido, String email, String contrasenia) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.estado = "Activo";
        this.contrasenia = contrasenia;
    }
    
    public void iniciarSesion(){
        
    }
    
    public void cerrarSesion(){
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDni() {
        return dni;
    }

    public void setDni(long dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
    
    public void registrarAlta(long dni, String nombre, String apellido, String email, String contrasenia, String rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.estado = "Activo";
    }

    public void modificar(String nombre, String apellido, String email, String contrasenia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
    }

    public void registrarBaja() {
        this.estado = "Inactivo";
    }
    
    public static boolean validarCredenciales(String email, String contrasenia) {
        return email != null && !email.trim().isEmpty() && contrasenia != null && !contrasenia.trim().isEmpty();
    }

    public boolean autenticar(String emailIngresado, String passIngresada) {
        if (!validarCredenciales(emailIngresado, passIngresada)) {
            return false;
        }
        if ("Inactivo".equalsIgnoreCase(this.estado)) {
            return false;
        }
        return this.email.equals(emailIngresado) && this.contrasenia.equals(passIngresada);
    }

    public boolean registrarUsuario(String nombre, String apellido, String email, String contrasenia, String rol) {
        if (nombre == null || nombre.trim().isEmpty() || email == null || email.trim().isEmpty() || contrasenia == null || contrasenia.trim().isEmpty()) {
            return false;
        }
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.estado = "Activo";
        
        return true;
    }

    @Override
    public String toString() {
        String nom = (nombre != null ? nombre : "") + (apellido != null && !apellido.isEmpty() ? " " + apellido : "");
        String tipo = (rol != null && !rol.isEmpty()) ? rol : getClass().getSimpleName();
        return nom.trim() + " [" + tipo + " - " + (email != null ? email : "sin email") + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Usuario)) return false;
        Usuario otro = (Usuario) o;
        if (this.id > 0 && otro.id > 0) return this.id == otro.id;
        if (this.dni > 0 && otro.dni > 0) return this.dni == otro.dni;
        if (this.email != null && otro.email != null) return this.email.equalsIgnoreCase(otro.email);
        return false;
    }

    @Override
    public int hashCode() {
        if (this.id > 0) return Integer.hashCode(this.id);
        if (this.dni > 0) return Long.hashCode(this.dni);
        return this.email != null ? this.email.toLowerCase().hashCode() : 0;
    }
}
