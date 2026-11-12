package modelo;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class ReportePosicion extends Reporte {
    
    @Basic
    private String equipoConsultado;

    public ReportePosicion() {
        super();
    }

    public ReportePosicion(String equipoConsultado) {
        super();
        this.equipoConsultado = equipoConsultado;
    }

    public ReportePosicion(String equipoConsultado, Date fechaGeneracion) {
        super(fechaGeneracion);
        this.equipoConsultado = equipoConsultado;
    }

    public String getEquipoConsultado() {
        return equipoConsultado;
    }

    public void setEquipoConsultado(String equipoConsultado) {
        this.equipoConsultado = equipoConsultado;
    }
}