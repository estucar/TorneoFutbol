package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Partido implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.DATE)
    private Date fecha;
    
    @Basic
    private String hora;
    
    @Basic    
    private String estado;
    
    @Basic
    private String resultado;
    
    @Basic
    private String motivoPostergacion;
    
    @Basic
    private String fase;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Cancha cancha;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Arbitro arbitro;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Equipo equipoLocal;
    
    @ManyToOne(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private Equipo equipoVisitante;
    
    @OneToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Incidente> incidentes;
    
    public Partido() {
        this.incidentes = new ArrayList<>();
    }

    public Partido(int id, Date fecha, String hora, String fase, Cancha cancha, Equipo equipoLocal, Equipo equipoVisitante) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = "Pendiente";
        this.fase = fase;
        this.cancha = cancha;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.incidentes = new ArrayList<>();
    }
    
    public void programar() {
        this.estado = "Pendiente";
    }
    
    public boolean cargarResultado(int golesLocal, int golesVisitante, java.util.List<Incidente> nuevosIncidentes) {
        if (this.estado == null || this.estado.equals("Finalizado") || this.estado.equals("Cancelado")) {
            return false;
        }

        if (golesLocal < 0 || golesVisitante < 0) {
            return false;
        }

        if (nuevosIncidentes != null) {
            for (Incidente incidente : nuevosIncidentes) {
                if (incidente == null || incidente.getTipo() == null || incidente.getTipo().trim().isEmpty() || incidente.getMinuto() < 0 || incidente.getJugador() == null) {
                    return false;
                }
            }
        }

        this.resultado = golesLocal + " - " + golesVisitante;

        if (nuevosIncidentes != null) {
            for (Incidente incidente : nuevosIncidentes) {
                this.getIncidentes().add(incidente);

                Jugador j = incidente.getJugador();
                Date inicio = (this.fecha != null) ? this.fecha : new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(inicio);
                c.add(Calendar.DAY_OF_YEAR, 7);
                Date fin = c.getTime();

                if ("Tarjeta Roja".equalsIgnoreCase(incidente.getTipo())) {
                    j.actualizarEstado("Suspendido");
                    Sancion sancionRoja = new Sancion("Suspension", "Expulsion directa por tarjeta roja", inicio, fin);
                    j.vincularSancion(sancionRoja);
                } else if ("Tarjeta Amarilla".equalsIgnoreCase(incidente.getTipo())) {
                    int conteoAmarillas = 0;
                    for (Incidente incRegistrado : this.getIncidentes()) {
                        if ("Tarjeta Amarilla".equalsIgnoreCase(incRegistrado.getTipo()) && incRegistrado.getJugador().equals(j)) {
                            conteoAmarillas++;
                        }
                    }
                    if (conteoAmarillas >= 2) {
                        j.setEstado("Suspendido");
                        Sancion sancionDobleAmarilla = new Sancion("Suspension", "Suspension por acumulacion de dos tarjetas amarillas", inicio, fin);
                        j.vincularSancion(sancionDobleAmarilla);
                    }
                }
            }
        }

        this.estado = "Finalizado";
        return true;
    }
    
    public boolean postergar(String motivo) {
        if ("Pendiente".equals(this.estado)) {
            this.estado = "Postergado";
            this.motivoPostergacion = motivo;
            return true;
        }
        return false;
    }

    public boolean suspender(String motivo) {
        if (!"Finalizado".equals(this.estado)) {
            this.estado = "Suspendido";
            this.motivoPostergacion = (motivo != null && !motivo.trim().isEmpty()) ? motivo.trim() : "Suspendido por fuerza mayor";
            return true;
        }
        return false;
    }
    
    public void asignarArbitro(Arbitro arbitroAsignado) {
        this.arbitro = arbitroAsignado;
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

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getMotivoPostergacion() {
        return motivoPostergacion;
    }

    public void setMotivoPostergacion(String motivoPostergacion) {
        this.motivoPostergacion = motivoPostergacion;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public Cancha getCancha() {
        return cancha;
    }

    public void setCancha(Cancha cancha) {
        this.cancha = cancha;
    }

    public Arbitro getArbitro() {
        return arbitro;
    }

    public void setArbitro(Arbitro arbitro) {
        this.arbitro = arbitro;
    }

    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(Equipo equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(Equipo equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public List<Incidente> getIncidentes() {
        return incidentes;
    }

    public void setIncidentes(List<Incidente> incidentes) { 
        this.incidentes = incidentes;
    }
    
    public Equipo getGanador() {
        if ("Finalizado".equals(this.estado) && this.resultado != null && this.resultado.contains("-")) {
            String[] partes = this.resultado.split("-");
            int golesL = Integer.parseInt(partes[0].trim());
            int golesV = Integer.parseInt(partes[1].trim());

            if (golesL > golesV) {
                return this.equipoLocal;
            } else if (golesL < golesV) {
                return this.equipoVisitante;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String loc = (equipoLocal != null && equipoLocal.getNombre() != null) ? equipoLocal.getNombre() : "Local";
        String vis = (equipoVisitante != null && equipoVisitante.getNombre() != null) ? equipoVisitante.getNombre() : "Visitante";
        String res = (resultado != null && !resultado.isEmpty()) ? " [" + resultado + "]" : "";
        return "#" + id + " " + loc + " vs " + vis + res + " (" + (fase != null ? fase : "Fase") + " - " + (estado != null ? estado : "Pendiente") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Partido)) return false;
        Partido otro = (Partido) o;
        if (this.id > 0 && otro.id > 0) return this.id == otro.id;
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }
}
