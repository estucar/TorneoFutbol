package modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public abstract class Reporte implements Serializable {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private int id;
    
    @Temporal(TemporalType.DATE)
    private Date fechaGeneracion;

    public Reporte() {
        this.fechaGeneracion = new Date();
    }

    public Reporte(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

}