package modelo;

import java.util.Date;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Inscripcion implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Basic
    private String estado;
    
    @ElementCollection
    private List<String> documentacion;
    
    @Basic
    private String motivoRechazo;
    
    @Basic
    private String documentoAdjunto;

    public Inscripcion() {
        this.documentacion = new ArrayList<>();
    }
    
    public Inscripcion(int id, Date fecha) {
        this.id = id;
        this.fecha = fecha;
        this.estado = "Pendiente";
        this.documentacion = new ArrayList<>();
    }
    
    public void habilitar(){
        this.estado = "Habilitado";
    }
    
    public void rechazar(String motivo) {
        this.estado = "Rechazada";
        this.motivoRechazo = motivo;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDocumentoAdjunto() {
        return documentoAdjunto;
    }

    public void setDocumentoAdjunto(String documentoAdjunto) {
        this.documentoAdjunto = documentoAdjunto;
    }
    
    public void setDocumentacion(List<String> documentacion) {
        this.documentacion = documentacion;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
    
    public void adjuntarDocumentacion(String rutaOComprobante) {
        if (rutaOComprobante != null && !rutaOComprobante.trim().isEmpty()) {
            this.documentoAdjunto = rutaOComprobante;
        }
    }

    public String consultarEstado() {
        if ("Rechazada".equalsIgnoreCase(this.estado) && this.motivoRechazo != null && !this.motivoRechazo.trim().isEmpty()) {
            return this.estado + " - Motivo: " + this.motivoRechazo;
        }
        return this.estado;
    }
}