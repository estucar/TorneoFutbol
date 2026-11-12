package controlador;

import modelo.*;
import java.util.Date;
import javax.persistence.EntityManager;

public class ControladorOrganizador {
    
    private Organizador motorOrganizador;
    private Usuario usuarioLogueado;
    private EntityManager em; 

    public ControladorOrganizador(Organizador motorOrganizador, Usuario usuarioLogueado, EntityManager em) {
        this.motorOrganizador = motorOrganizador;
        this.usuarioLogueado = usuarioLogueado;
        this.em = em;
    }

    private boolean validarAcceso() {
        return this.usuarioLogueado != null && (this.usuarioLogueado instanceof Organizador);
    }

    public String evaluarInscripcion(Equipo equipo, int edadMinima, boolean docValida, String motivoRechazo) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Organizador.";
        
        try {
            em.getTransaction().begin();
            boolean exito = motorOrganizador.evaluarInscripcion(equipo, edadMinima, docValida, motivoRechazo);
            
            if (exito) {
                em.merge(equipo);
                em.getTransaction().commit();
                return "Inscripcion evaluada, habilitada y guardada en BD.";
            } else {
                em.merge(equipo); 
                em.getTransaction().commit();
                return "Inscripcion rechazada y registrada en BD.";
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String registrarEquipo(Equipo equipo, String nombre, Capitan capitan) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Organizador.";
        try {
            em.getTransaction().begin();
            equipo.registrarEquipo(nombre, capitan);
            em.persist(equipo); 
            em.getTransaction().commit();
            return "Equipo registrado permanentemente en BD.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String aplicarSanciones(Jugador jugador, String tipo, String motivo, Date fechaInicio, Date fechaFin) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Organizador.";
        try {
            em.getTransaction().begin();
            motorOrganizador.aplicarSanciones(jugador, tipo, motivo, fechaInicio, fechaFin);
            em.merge(jugador); 
            em.getTransaction().commit();
            return "Sancion aplicada y guardada en BD.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error de base de datos: " + e.getMessage();
        }
    }

}