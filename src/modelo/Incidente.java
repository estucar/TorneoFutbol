package modelo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Incidente implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Basic
    private String tipo;
    
    @Basic
    private int minuto;
    
    @Basic
    private String descripcion;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Jugador jugador;

    public Incidente() {
    }

    public Incidente(int id, String tipo, int minuto, String descripcion, Jugador jugador) {
        this.id = id;
        this.tipo = tipo;
        this.minuto = minuto;
        this.descripcion = descripcion;
        this.jugador = jugador;
    }
    
    public void asociarJugador(Jugador jugadorInvolucrado) {
        this.jugador = jugadorInvolucrado;
    }
    
    public void asociarTarjeta(String tipoTarjeta) {
        this.tipo = tipoTarjeta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getMinuto() {
        return minuto;
    }

    public void setMinuto(int minuto) {
        this.minuto = minuto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    @Override
    public String toString() {
        String jug = (jugador != null && jugador.getNombre() != null) ? jugador.getNombre() : "Jugador";
        return minuto + "' " + (tipo != null ? tipo : "Incidente") + " - " + jug + " (" + (descripcion != null ? descripcion : "") + ")";
    }
}
