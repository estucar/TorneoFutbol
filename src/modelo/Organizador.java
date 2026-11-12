package modelo;

import java.util.Date;
import javax.persistence.Entity;
import servicios.ServicioCorreo;

@Entity
public class Organizador extends AdministradorVip {

    public Organizador() {
        super();
    }

    public Organizador(int id, long dni, String nombre, String apellido, String email, String contrasenia, String nivelAcceso) {
        super(id, dni, nombre, apellido, email, contrasenia, nivelAcceso);
    }

    public void aplicarSanciones(Jugador jugador, String tipo, String motivo, Date fechaInicio, Date fechaFin) {
        if (jugador == null || tipo == null || motivo == null || fechaInicio == null) {
            return;
        }

        Sancion nuevaSancion = new Sancion(tipo, motivo, fechaInicio, fechaFin);

        jugador.setEstado("Suspendido");

        if (jugador.getSanciones() != null) {
            jugador.getSanciones().add(nuevaSancion);
        }
    }

    public boolean programarPartidos(Equipo equipoLocal, Equipo equipoVisitante, java.util.Date fecha, String hora, String fase, Cancha cancha, Arbitro arbitro, java.util.List<Partido> partidosExistentes) {
        if (equipoLocal == null || equipoVisitante == null || fecha == null || hora == null || hora.trim().isEmpty() || fase == null || fase.trim().isEmpty() || cancha == null || arbitro == null) {
            return false;
        }

        if (equipoLocal.equals(equipoVisitante)) {
            return false;
        }

        if (equipoLocal.getInscripcion() == null || !"Habilitado".equals(equipoLocal.getInscripcion().getEstado())) {
            return false;
        }
        
        if (equipoVisitante.getInscripcion() == null || !"Habilitado".equals(equipoVisitante.getInscripcion().getEstado())) {
            return false;
        }

        if (partidosExistentes != null) {
            for (Partido p : partidosExistentes) {
                if (p.getCancha() != null && p.getCancha().equals(cancha)) {
                    if (p.getFecha().equals(fecha) && p.getHora().equals(hora)) {
                        return false;
                    }
                }
            }
        }

        if (!arbitro.consultarDisponibilidad(fecha, hora, partidosExistentes)) {
            return false;
        }

        Partido nuevoPartido = new Partido();
        nuevoPartido.setEquipoLocal(equipoLocal);
        nuevoPartido.setEquipoVisitante(equipoVisitante);
        nuevoPartido.setFecha(fecha);
        nuevoPartido.setHora(hora);
        nuevoPartido.setFase(fase);
        nuevoPartido.setCancha(cancha);

        nuevoPartido.programar();
        nuevoPartido.asignarArbitro(arbitro);

        if (partidosExistentes != null) {
            partidosExistentes.add(nuevoPartido);
        }

        ServicioCorreo servicioCorreo = new ServicioCorreo();
        String detallePartido = "Encuentro: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre() + "\n"
                + "Fase: " + fase + "\n"
                + "Fecha: " + fecha.toString() + " a las " + hora + " hs\n"
                + "Cancha: " + cancha.getNombre() + " (" + cancha.getUbicacion() + ")";

        if (equipoLocal.getCapitan() != null && equipoLocal.getCapitan().getEmail() != null) {
            servicioCorreo.enviarCorreo(
                equipoLocal.getCapitan().getEmail(),
                "Partido Programado: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre(),
                "Estimado Capitán, se ha programado el siguiente encuentro:\n\n" + detallePartido
            );
        }

        if (equipoVisitante.getCapitan() != null && equipoVisitante.getCapitan().getEmail() != null) {
            servicioCorreo.enviarCorreo(
                equipoVisitante.getCapitan().getEmail(),
                "Partido Programado: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre(),
                "Estimado Capitán, se ha programado el siguiente encuentro:\n\n" + detallePartido
            );
        }

        if (arbitro.getEmail() != null) {
            servicioCorreo.enviarCorreo(
                arbitro.getEmail(),
                "Designación Arbitral: " + equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre(),
                "Estimado Árbitro, ha sido designado para dirigir el siguiente encuentro:\n\n" + detallePartido
            );
        }

        return true;
    }

    public boolean resolverReclamo(Reclamo reclamo, boolean aceptado, String dictamen) {
        if (reclamo == null || dictamen == null || dictamen.trim().isEmpty()) {
            return false;
        }

        if (aceptado) {
            reclamo.setEstado("Aceptado");
        } else {
            reclamo.setEstado("Rechazado");
        }
        
        reclamo.setResolucion(dictamen);

        return true;
    }

    public boolean examinarReclamo(Reclamo reclamo) {
        if (reclamo == null) {
            return false;
        }
        
        if ("Pendiente".equals(reclamo.getEstado())) {
            reclamo.setEstado("En Revision");
            return true;
        }
        
        return false;
    }

    public boolean evaluarInscripcion(Equipo equipoAEvaluar, int edadMinima, boolean documentacionValida, String motivoRechazoManual) {
        if (equipoAEvaluar == null || equipoAEvaluar.getInscripcion() == null) {
            return false;
        }

        Inscripcion inscripcion = equipoAEvaluar.getInscripcion();

        if (!documentacionValida) {
            inscripcion.rechazar(motivoRechazoManual);
            return false;
        }

        if (equipoAEvaluar.getPlantel() != null) {
            for (Jugador jugador : equipoAEvaluar.getPlantel()) {
                if (!jugador.cumpleEdadMinima(edadMinima)) {
                    inscripcion.rechazar("Rechazo automático: El jugador con DNI " + jugador.getDni() + " no cumple con la edad mínima.");
                    return false;
                }
                if (jugador.tieneSancionVigente()) {
                    inscripcion.rechazar("Rechazo automático: El jugador con DNI " + jugador.getDni() + " posee suspensiones vigentes.");
                    return false;
                }
            }
        }

        inscripcion.habilitar();

        if (equipoAEvaluar.getCapitan() != null && equipoAEvaluar.getCapitan().getEmail() != null) {
            ServicioCorreo servicioCorreo = new ServicioCorreo();
            servicioCorreo.enviarCorreo(
                equipoAEvaluar.getCapitan().getEmail(),
                "Inscripción Aprobada - " + equipoAEvaluar.getNombre(),
                "Felicitaciones Capitán, la documentación y plantel del equipo '" 
                + equipoAEvaluar.getNombre() + "' han sido validados con éxito. El equipo se encuentra Habilitado."
            );
        }

        return true;
    }
    
    public boolean reprogramarPartido(Partido partido, java.util.Date nuevaFecha, String nuevaHora, Cancha nuevaCancha, java.util.List<Partido> partidosExistentes) {
        if (partido == null || nuevaFecha == null || nuevaHora == null || nuevaHora.trim().isEmpty() || nuevaCancha == null) {
            return false;
        }
        
        if (!"Postergado".equals(partido.getEstado()) && !"Pendiente".equals(partido.getEstado()) && !"Suspendido".equals(partido.getEstado())) {
             return false;
        }

        if (partidosExistentes != null) {
            for (Partido p : partidosExistentes) {
                if (p.getCancha() != null && p.getCancha().equals(nuevaCancha)) {
                    if (p.getFecha().equals(nuevaFecha) && p.getHora().equals(nuevaHora)) {
                        if (!p.equals(partido)) { 
                            return false; 
                        }
                    }
                }
            }
        }

        partido.setFecha(nuevaFecha);
        partido.setHora(nuevaHora);
        partido.setCancha(nuevaCancha);
        partido.setEstado("Pendiente");
        
        return true;
    }
}