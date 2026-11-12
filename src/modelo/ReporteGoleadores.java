package modelo;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class ReporteGoleadores extends Reporte {
    
    @Basic
    private int cantidadTop;

    public ReporteGoleadores() {
        super();
    }

    public ReporteGoleadores(int cantidadTop) {
        super();
        this.cantidadTop = cantidadTop;
    }

    public ReporteGoleadores(int cantidadTop, Date fechaGeneracion) {
        super(fechaGeneracion);
        this.cantidadTop = cantidadTop;
    }

    public int getCantidadTop() {
        return cantidadTop;
    }

    public void setCantidadTop(int cantidadTop) {
        this.cantidadTop = cantidadTop;
    }
}