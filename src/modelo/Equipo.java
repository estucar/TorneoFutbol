package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Equipo implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private String nombre;
    @Basic
    private String categoria;
    @Basic
    private String colorCamiseta;
    @Basic
    private String sedeHabitual;
    @Basic
    private String escudo;
    
    @OneToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Capitan capitan;
    
    @OneToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Jugador> plantel;
    
    @OneToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Inscripcion inscripcion;

    public Equipo() {
        this.plantel = new ArrayList<>();
    }
        
    public Equipo(int id, String nombre, String categoria, String colorCamiseta, String sedeHabitual, Capitan capitan) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.colorCamiseta = colorCamiseta;
        this.sedeHabitual = sedeHabitual;
        this.capitan = capitan;
        this.plantel = new ArrayList<>();
    }
    
    public void registrarEquipo(String nombre, Capitan capitan) {
        this.nombre = nombre;
        this.capitan = capitan;
        
        Inscripcion nuevaInscripcion = new Inscripcion();
        nuevaInscripcion.setFecha(new java.util.Date());
        nuevaInscripcion.setEstado("Pendiente");
        
        this.inscripcion = nuevaInscripcion;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getColorCamiseta() {
        return colorCamiseta;
    }

    public void setColorCamiseta(String colorCamiseta) {
        this.colorCamiseta = colorCamiseta;
    }

    public String getSedeHabitual() {
        return sedeHabitual;
    }

    public void setSedeHabitual(String sedeHabitual) {
        this.sedeHabitual = sedeHabitual;
    }

    public String getEscudo() {
        return escudo;
    }

    public void setEscudo(String escudo) {
        this.escudo = escudo;
    }

    public Capitan getCapitan() {
        return capitan;
    }

    public void setCapitan(Capitan capitan) {
        this.capitan = capitan;
    }

    public List<Jugador> getPlantel() {
        return plantel;
    }

    public void setPlantel(List<Jugador> plantel) {
        this.plantel = plantel;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }
    
    public boolean existeJugador(long dni) {
        if (this.capitan != null && this.capitan.getDni() == dni) {
            return true;
        }
        if (this.plantel != null) {
            for (Jugador j : this.plantel) {
                if (j.getDni() == dni) {
                    return true;
                }
            }
        }
        return false;
    }

    public void agregarJugador(Jugador jugador) {
        if (jugador != null) {
            if (this.plantel == null) {
                this.plantel = new ArrayList<>();
            }
            if (!this.plantel.contains(jugador)) {
                this.plantel.add(jugador);
            }
        }
    }

    public void removerJugador(Jugador jugador) {
        if (jugador != null && this.plantel != null) {
            this.plantel.remove(jugador);
        }
    }
    
    public String verPerfil() {
        String nombreCapitan = (this.capitan != null && this.capitan.getNombre() != null) ? this.capitan.getNombre() : "Sin asignar";
        String estadoInscripcion = (this.inscripcion != null && this.inscripcion.getEstado() != null) ? this.inscripcion.getEstado() : "Sin inscripción";
        
        int cantidadHabilitados = 0;
        if (this.plantel != null) {
            for (Jugador j : this.plantel) {
                if ("Activo".equalsIgnoreCase(j.getEstado())) {
                    cantidadHabilitados++;
                }
            }
        }
        
        return "Equipo: " + this.nombre + " | Capitán: " + nombreCapitan + " | Jugadores habilitados: " + cantidadHabilitados + " | Estado: " + estadoInscripcion;
    }
    
    public java.util.List<Torneo> consultarHistorialTorneos(java.util.List<Torneo> todosLosTorneos) {
        java.util.List<Torneo> historial = new java.util.ArrayList<>();
        if (todosLosTorneos != null) {
            for (Torneo t : todosLosTorneos) {
                if (t.getEquipos() != null && t.getEquipos().contains(this)) {
                    historial.add(t);
                }
            }
        }
        return historial;
    }

    @Override
    public String toString() {
        String est = (inscripcion != null && inscripcion.getEstado() != null) ? inscripcion.getEstado() : "Sin inscr.";
        return (nombre != null ? nombre : "Equipo") + " [" + est + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Equipo)) return false;
        Equipo otro = (Equipo) o;
        if (this.id > 0 && otro.id > 0) return this.id == otro.id;
        if (this.nombre != null && otro.nombre != null) return this.nombre.equalsIgnoreCase(otro.nombre);
        return false;
    }

    @Override
    public int hashCode() {
        if (this.id > 0) return Integer.hashCode(this.id);
        return this.nombre != null ? this.nombre.toLowerCase().hashCode() : 0;
    }
}
