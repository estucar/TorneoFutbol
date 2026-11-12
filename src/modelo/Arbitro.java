
package modelo;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.util.Date;
import java.util.List;

@Entity
public class Arbitro extends Usuario{
    
    @Basic
    private int matricula;

    public Arbitro() {
        super();
    }

    public Arbitro(int matricula,int id, long dni, String nombre, String apellido, String email, String contrasenia) {
    super(id, dni, nombre, apellido, email, contrasenia);
    this.matricula = matricula;
    }
    
    public boolean cargarResultado(Partido partido, int golesLocal, int golesVisitante, java.util.List<Incidente> incidentes) {
        if (partido != null && this.equals(partido.getArbitro())) {
            return partido.cargarResultado(golesLocal, golesVisitante, incidentes);
        }
        return false;
    }
    
    public void registrarIncidente(Partido partido, Incidente nuevoIncidente) {
        if (partido != null && this.equals(partido.getArbitro()) && nuevoIncidente != null) {
            partido.getIncidentes().add(nuevoIncidente);
        }
    }
    
    public boolean consultarDisponibilidad(Date fecha, String hora, List<Partido> partidosExistentes) {
        if (fecha == null || hora == null || partidosExistentes == null) {
            return true;
        }

        for (Partido p : partidosExistentes) {
            if (this.equals(p.getArbitro())) {
                if (p.getFecha() != null && p.getFecha().equals(fecha) && p.getHora() != null && p.getHora().equals(hora)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void aplicarSancion(Jugador jugador, String tipo, String motivo, Date fechaInicio, Date fechaFin) {
        if (jugador != null) {
            Sancion sancion = new Sancion(tipo, motivo, fechaInicio, fechaFin);
            jugador.vincularSancion(sancion);
        }
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

}
