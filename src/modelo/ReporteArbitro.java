package modelo;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class ReporteArbitro extends Reporte {
    
    @Basic
    private int matArbitro;

    public ReporteArbitro() {
        super();
    }

    public ReporteArbitro(int matArbitro) {
        super();
        this.matArbitro = matArbitro;
    }

    public ReporteArbitro(int matArbitro, Date fechaGeneracion) {
        super(fechaGeneracion);
        this.matArbitro = matArbitro;
    }

    public int getMatArbitro() {
        return matArbitro;
    }

    public void setMatArbitro(int matArbitro) {
        this.matArbitro = matArbitro;
    }
}