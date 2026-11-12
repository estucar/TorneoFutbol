package modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Reclamo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Basic
    private String fundamento;
    
    @Basic
    private String estado;
    
    @Basic
    private String motivoResolucion;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Partido partido;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Capitan capitan;

    public Reclamo() {
    }

    public Reclamo(int id, Date fecha, String fundamento, String estado, String motivoResolucion) {
        this.id = id;
        this.fecha = fecha;
        this.fundamento = fundamento;
        this.estado = estado;
        this.motivoResolucion = motivoResolucion;
    }

    public Reclamo(String estado, String motivoResolucion) {
        this.estado = estado;
        this.motivoResolucion = motivoResolucion;
    }

    public Reclamo(int id, Date fecha, String fundamento) {
        this.id = id;
        this.fecha = fecha;
        this.fundamento = fundamento;
    }

    public void iniciar() {
        this.estado = "Pendiente";
    }

    public void adjuntarArgumento(String argumento) {
        this.fundamento = argumento;
    }

    public void resolver(String resolucion) {
        this.motivoResolucion = resolucion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getFundamento() {
        return fundamento;
    }

    public void setFundamento(String fundamento) {
        this.fundamento = fundamento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoResolucion() {
        return motivoResolucion;
    }

    public void setMotivoResolucion(String motivoResolucion) {
        this.motivoResolucion = motivoResolucion;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Capitan getCapitan() {
        return capitan;
    }

    public void setCapitan(Capitan capitan) {
        this.capitan = capitan;
    }

    public void setMotivo(String motivo) {
        this.fundamento = motivo;
    }

    public String getMotivo() {
        return this.fundamento;
    }

    public void setResolucion(String resolucion) {
        this.motivoResolucion = resolucion;
    }

    public String getResolucion() {
        return this.motivoResolucion;
    }

    @Override
    public String toString() {
        String cap = (capitan != null && capitan.getNombre() != null) ? capitan.getNombre() : "Capitán";
        return "Reclamo #" + id + " (" + (estado != null ? estado : "Pendiente") + ") - " + cap + ": " + (fundamento != null ? fundamento : "");
    }
}
