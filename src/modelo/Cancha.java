
package modelo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Cancha implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private String nombre;
    @Basic
    private String ubicacion;
    @javax.persistence.Basic
    private boolean disponible;
    
    public Cancha(){
    }

    public Cancha(int id, String nombre, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }
    
    public void registrarAlta(String nombre, String ubicacion, boolean disponible) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.disponible = disponible;
    }
    
    public void modificar(String nuevoNombre, String nuevaUbicacion, boolean disponible) {
        this.nombre = nuevoNombre;
        this.ubicacion = nuevaUbicacion;
        this.disponible = disponible;
    }
    
    public void registrarBaja() {
        this.disponible = false;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    @Override
    public String toString(){
        return nombre + " (" + ubicacion + ")";
    }
    
    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Cancha)) return false;
        Cancha otra = (Cancha) o;
        if (this.id > 0 && otra.id > 0) return this.id == otra.id;
        if (this.nombre != null && otra.nombre != null) return this.nombre.equalsIgnoreCase(otra.nombre);
        return false;
    }

    @Override
    public int hashCode() {
        if (this.id > 0) return Integer.hashCode(this.id);
        return this.nombre != null ? this.nombre.toLowerCase().hashCode() : 0;
    }
}
