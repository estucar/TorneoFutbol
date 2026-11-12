package controlador;

import modelo.*;
import javax.persistence.EntityManager;

public class ControladorAdministrador {
    
    private Usuario usuarioLogueado;
    private EntityManager em;

    
    public ControladorAdministrador(Usuario usuarioLogueado, EntityManager em) {
        this.usuarioLogueado = usuarioLogueado;
        this.em = em;
    }

    private boolean validarAcceso() {
        return this.usuarioLogueado != null && (this.usuarioLogueado instanceof Administrador);
    }

    // CRUD DE USUARIOS

    public String registrarAltaUsuario(Usuario nuevoUsuario) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.persist(nuevoUsuario); 
            em.getTransaction().commit();
            return "Usuario guardado permanentemente en el sistema.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al guardar el usuario: " + e.getMessage();
        }
    }

    public String modificarUsuario(Usuario usuarioActualizado) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.merge(usuarioActualizado); 
            em.getTransaction().commit();
            return "Datos del usuario actualizados en la base de datos.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al actualizar el usuario: " + e.getMessage();
        }
    }

    public String registrarBajaUsuario(Usuario usuario) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            Usuario usuarioABorrar = em.merge(usuario); 
            em.remove(usuarioABorrar);
            em.getTransaction().commit();
            return "Usuario eliminado definitivamente del sistema.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al eliminar el usuario: " + e.getMessage();
        }
    }

    // CRUD DE TORNEOS 

    public String registrarAltaTorneo(Torneo torneo) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.persist(torneo);
            em.getTransaction().commit();
            return "Torneo creado y guardado en la base de datos.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al crear el torneo: " + e.getMessage();
        }
    }

    public String modificarTorneo(Torneo torneo) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.merge(torneo);
            em.getTransaction().commit();
            return "Torneo modificado correctamente.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al modificar el torneo: " + e.getMessage();
        }
    }

    public String registrarBajaTorneo(Torneo torneo) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            Torneo torneoABorrar = em.merge(torneo);
            em.remove(torneoABorrar);
            em.getTransaction().commit();
            return "El torneo ha sido borrado por completo de la base de datos.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al borrar el torneo: " + e.getMessage();
        }
    }

    // CRUD DE CANCHAS 

    public String registrarAltaCancha(Cancha cancha) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.persist(cancha);
            em.getTransaction().commit();
            return "Cancha nueva registrada en el sistema.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al registrar la cancha: " + e.getMessage();
        }
    }

    public String modificarCancha(Cancha cancha) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            em.merge(cancha);
            em.getTransaction().commit();
            return "Cancha actualizada en la base de datos.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al actualizar la cancha: " + e.getMessage();
        }
    }

    public String registrarBajaCancha(Cancha cancha) {
        if (!validarAcceso()) return "Acceso Denegado: Exclusivo para Administrador.";
        try {
            em.getTransaction().begin();
            Cancha canchaABorrar = em.merge(cancha);
            em.remove(canchaABorrar);
            em.getTransaction().commit();
            return "Cancha eliminada definitivamente.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return "Error al eliminar la cancha: " + e.getMessage();
        }
    }
}