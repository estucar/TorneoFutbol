package controlador;

import modelo.*;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;

public class ControladorPartidos {
    
    private Organizador motorOrganizador;
    private Usuario usuarioLogueado;
    private EntityManager em;

    public ControladorPartidos(Organizador motorOrganizador, Usuario usuarioLogueado, EntityManager em) {
        this.motorOrganizador = motorOrganizador;
        this.usuarioLogueado = usuarioLogueado;
        this.em = em;
    }

    public String programarPartido(Equipo local, Equipo visitante, Date fecha, String hora, String fase, Cancha cancha, Arbitro arbitro, List<Partido> lista) {
        if (this.usuarioLogueado == null || !(this.usuarioLogueado instanceof Organizador)) {
            return "Acceso Denegado: Exclusivo para Organizador.";
        }
        
        try {
            em.getTransaction().begin();
            boolean exito = motorOrganizador.programarPartidos(local, visitante, fecha, hora, fase, cancha, arbitro, lista);
            
            if (exito) {
                Partido nuevoPartido = lista.get(lista.size() - 1);
                em.persist(nuevoPartido); // Guarda el partido en MySQL
                em.getTransaction().commit();
                return "Partido programado, correos enviados y guardado en BD.";
            } else {
                em.getTransaction().rollback();
                return "Error en la validacion al programar partido.";
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String cargarResultados(Partido partido, int golesL, int golesV, List<Incidente> incidentes) {
        if (this.usuarioLogueado == null || !(this.usuarioLogueado instanceof Arbitro)) {
            return "Acceso Denegado: Exclusivo para Arbitro.";
        }
        Arbitro arbitroActivo = (Arbitro) this.usuarioLogueado;
        
        try {
            em.getTransaction().begin();
            boolean exito = arbitroActivo.cargarResultado(partido, golesL, golesV, incidentes);
            
            if (exito) {
                em.merge(partido);
                em.getTransaction().commit();
                return "Resultados e incidentes guardados permanentemente en BD.";
            } else {
                em.getTransaction().rollback();
                return "Error: El arbitro no corresponde a este partido.";
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error de base de datos: " + e.getMessage();
        }
    }
}