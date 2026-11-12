package modelo;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//aca no se si está bien, la ia limó e hizo que se genere un codigo de barras
@Entity
public class Credencial implements Serializable {
    
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private int id;
    
    @Basic
    private String codigoBarras;

    public Credencial() {
    }
    
    public Credencial(int id, String codigoBarras) {
        this.id = id;
        this.codigoBarras = codigoBarras;
    }
    
    public void generar() {
        if (this.codigoBarras == null || this.codigoBarras.isEmpty()) {
            this.codigoBarras = "CRED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }
    
    @Override
    public String toString(){
        return "Credencial [Código = " + codigoBarras + " ]";
    }
}