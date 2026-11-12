package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Jugador extends PublicoGeneral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private long dni;
    @Basic
    private String nombre;
    @Basic
    private String posicion;
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;
    @Basic
    private String estado;
    @Basic
    private int goles;
    @Basic
    private int tarjetas;
    @Basic
    private String email;
    @Basic
    private String contrasenia;
    
    @OneToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Credencial credencial;
    
    @OneToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Sancion> sanciones;

    public Jugador() {
        this.sanciones = new ArrayList<>();
    }

    public Jugador(int id, long dni, String nombre, String posicion, Date fechaNacimiento, String email, String contrasenia) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.posicion = posicion;
        this.fechaNacimiento = fechaNacimiento;
        this.estado = "Activo";
        this.email = email;
        this.contrasenia = contrasenia;
        this.sanciones = new ArrayList<>();
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

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getGoles() {
        return goles;
    }

    public void setGoles(int goles) {
        this.goles = goles;
    }

    public int getTarjetas() {
        return tarjetas;
    }

    public void setTarjetas(int tarjetas) {
        this.tarjetas = tarjetas;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Credencial getCredencial() {
        return credencial;
    }

    public void setCredencial(Credencial credencial) {
        this.credencial = credencial;
    }

    public List<Sancion> getSanciones() {
        return sanciones;
    }

    public void setSanciones(List<Sancion> sanciones) {
        this.sanciones = sanciones;
    }
    
    public void actualizarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public void vincularSancion(Sancion nuevaSancion) {
        if (nuevaSancion != null) {
            this.sanciones.add(nuevaSancion);
        }
    }
    
    public boolean tieneSancionVigente() {
        if (this.sanciones != null) {
            for (Sancion s : this.sanciones) {
                if (s.verificarVigencia()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cumpleEdadMinima(int edadMinima) {
        if (this.fechaNacimiento == null) {
            return false;
        }
        java.util.Calendar hoy = java.util.Calendar.getInstance();
        java.util.Calendar nac = java.util.Calendar.getInstance();
        nac.setTime(this.fechaNacimiento);
        
        int edad = hoy.get(java.util.Calendar.YEAR) - nac.get(java.util.Calendar.YEAR);
        if (hoy.get(java.util.Calendar.DAY_OF_YEAR) < nac.get(java.util.Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad >= edadMinima;
    }
    
    public void registrarAltaDesktop(long dni, String nombre, java.util.Date fechaNacimiento, String posicion) {
        this.setDni(dni);
        this.setNombre(nombre);
        this.setFechaNacimiento(fechaNacimiento);
        this.setPosicion(posicion);
        this.setEstado("Activo");
    }
    
    public String verPerfil() {
        String tieneSanciones = this.tieneSancionVigente() ? "Sí" : "No";
        return "Jugador: " + this.nombre + " | DNI: " + this.dni + " | Posición: " + this.posicion + " | Goles: " + this.goles + " | Estado: " + this.estado + " | Sanciones vigentes: " + tieneSanciones;
    }
    
    public java.util.List<Torneo> consultarHistorialTorneos(java.util.List<Torneo> todosLosTorneos) {
        java.util.List<Torneo> historial = new java.util.ArrayList<>();
        if (todosLosTorneos != null) {
            for (Torneo t : todosLosTorneos) {
                if (t.getEquipos() != null) {
                    for (Equipo eq : t.getEquipos()) {
                        if (eq.getPlantel() != null && eq.getPlantel().contains(this)) {
                            if (!historial.contains(t)) {
                                historial.add(t);
                            }
                        }
                    }
                }
            }
        }
        return historial;
    }

    public String imprimirCredencial(String nombreTorneo, String nombreEquipo) {
        StringBuilder credencial = new StringBuilder();
        credencial.append("*************************************\n");
        credencial.append("       CREDENCIAL DE JUGADOR         \n");
        credencial.append("*************************************\n");
        credencial.append("Torneo: ").append(nombreTorneo).append("\n");
        credencial.append("Equipo: ").append(nombreEquipo).append("\n");
        credencial.append("Nombre: ").append(this.nombre).append("\n");
        credencial.append("DNI: ").append(this.dni).append("\n");
        credencial.append("Posición: ").append(this.posicion).append("\n");
        credencial.append("*************************************\n");
        
        return credencial.toString();
    }

    @Override
    public String toString() {
        return (nombre != null ? nombre : "Jugador") + " (DNI: " + dni + " | " + (posicion != null ? posicion : "Sin pos.") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Jugador)) return false;
        Jugador otro = (Jugador) o;
        if (this.id > 0 && otro.id > 0) return this.id == otro.id;
        if (this.dni > 0 && otro.dni > 0) return this.dni == otro.dni;
        return false;
    }

    @Override
    public int hashCode() {
        if (this.id > 0) return Integer.hashCode(this.id);
        return Long.hashCode(this.dni);
    }
}
