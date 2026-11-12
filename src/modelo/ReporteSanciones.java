package modelo;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class ReporteSanciones extends Reporte {

    @Basic
    private String jugadorConsulta;

    public ReporteSanciones() {
        super();
    }

    public ReporteSanciones(String jugadorConsulta) {
        super();
        this.jugadorConsulta = jugadorConsulta;
    }

    public ReporteSanciones(String jugadorConsulta, Date fechaGeneracion) {
        super(fechaGeneracion);
        this.jugadorConsulta = jugadorConsulta;
    }

    public String getJugadorConsulta() {
        return jugadorConsulta;
    }

    public void setJugadorConsulta(String jugadorConsulta) {
        this.jugadorConsulta = jugadorConsulta;
    }
}