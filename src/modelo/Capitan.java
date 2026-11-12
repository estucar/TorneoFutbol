package modelo;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class Capitan extends Jugador {

    @Basic
    private String nivelAccesoCapitan;
    
    @OneToMany(cascade = {javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE})
    private List<Reclamo> reclamos;

    public Capitan() {
        this.reclamos = new ArrayList<>();
    }

    public Capitan(int idCapitan, String nivelAccesoCapitan) {
        this.nivelAccesoCapitan = nivelAccesoCapitan;
        this.reclamos = new ArrayList<>();
    }

    public boolean cargaPlantel(Equipo miEquipo, List<Equipo> equiposActivos, long dni, String nombre, String posicion, java.util.Date fechaNacimiento) {
        if (miEquipo == null || dni <= 0 || nombre == null || nombre.trim().isEmpty() || posicion == null || posicion.trim().isEmpty() || fechaNacimiento == null) {
            return false;
        }

        if (miEquipo.existeJugador(dni)) {
            return false;
        }

        if (equiposActivos != null) {
            for (Equipo e : equiposActivos) {
                if (e.existeJugador(dni)) {
                    return false;
                }
            }
        }

        Jugador nuevoJugador = new Jugador();
        nuevoJugador.setDni(dni);
        nuevoJugador.setNombre(nombre);
        nuevoJugador.setPosicion(posicion);
        nuevoJugador.setFechaNacimiento(fechaNacimiento);
        nuevoJugador.setEstado("Activo");

        miEquipo.agregarJugador(nuevoJugador);

        return true;
    }

    public Reclamo realizarReclamo(Partido partido, String motivo, Equipo miEquipo) {
        if (partido == null || motivo == null || motivo.trim().isEmpty() || miEquipo == null) {
            return null;
        }

        if (!"Finalizado".equals(partido.getEstado())) {
            return null;
        }

        if (!miEquipo.equals(partido.getEquipoLocal()) && !miEquipo.equals(partido.getEquipoVisitante())) {
            return null;
        }

        Reclamo nuevoReclamo = new Reclamo();
        nuevoReclamo.setFecha(new java.util.Date());
        nuevoReclamo.setMotivo(motivo);
        nuevoReclamo.setEstado("Pendiente");
        nuevoReclamo.setPartido(partido);
        nuevoReclamo.setCapitan(this);

        if (this.reclamos == null) {
            this.reclamos = new ArrayList<>();
        }
        this.reclamos.add(nuevoReclamo);

        return nuevoReclamo;
    }

    public void cargarDocumentacion(Equipo miEquipo, String rutaDocumento) {
        if (miEquipo != null && miEquipo.getInscripcion() != null) {
            miEquipo.getInscripcion().adjuntarDocumentacion(rutaDocumento);
        }
    }

    public String getNivelAccesoCapitan() {
        return nivelAccesoCapitan;
    }

    public void setNivelAccesoCapitan(String nivelAccesoCapitan) {
        this.nivelAccesoCapitan = nivelAccesoCapitan;
    }

    public List<Reclamo> getReclamos() {
        return reclamos;
    }

    public void setReclamos(List<Reclamo> reclamos) {
        this.reclamos = reclamos;
    }
}