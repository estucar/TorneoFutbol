package modelo;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class HistorialTorneo extends Reporte {
    
    @Basic
    private String entidadConsultada;

    public HistorialTorneo() {
        super();
    }

    public HistorialTorneo(String entidadConsultada) {
        super();
        this.entidadConsultada = entidadConsultada;
    }

    public HistorialTorneo(String entidadConsultada, Date fechaGeneracion) {
        super(fechaGeneracion);
        this.entidadConsultada = entidadConsultada;
    }

    public String getEntidadConsultada() {
        return entidadConsultada;
    }

    public void setEntidadConsultada(String entidadConsultada) {
        this.entidadConsultada = entidadConsultada;
    }
}