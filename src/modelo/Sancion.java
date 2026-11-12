package modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Sancion implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Basic
    private String tipo;
    
    @Basic
    private String motivo;
    
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    
    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    public Sancion() {
    }

    public Sancion(int id, String tipo, String motivo, Date fechaInicio, Date fechaFin) {
        this.id = id;
        this.tipo = tipo;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Sancion(Date fechaInicio, Date fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
    
    public Sancion(String tipo, String motivo, Date fechaInicio, Date fechaFin) {
        this.tipo = tipo;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
    
    public boolean verificarVigencia() {
        java.util.Date hoy = new java.util.Date();
        if (this.fechaInicio != null && this.fechaFin != null) {
            if (!hoy.before(this.fechaInicio) && !hoy.after(this.fechaFin)) {
                return true;
            }
        }
        return false;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    @Override
    public String toString() {
        return (tipo != null ? tipo : "Sanción") + ": " + (motivo != null ? motivo : "") + " (Vigente: " + (verificarVigencia() ? "Sí" : "No") + ")";
    }
}
