/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package persistencia;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.ReporteSanciones;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class ReporteSancionesJpaController implements Serializable {

    public ReporteSancionesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ReporteSanciones reporteSanciones) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(reporteSanciones);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ReporteSanciones reporteSanciones) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            reporteSanciones = em.merge(reporteSanciones);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = reporteSanciones.getId();
                if (findReporteSanciones(id) == null) {
                    throw new NonexistentEntityException("The reporteSanciones with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(int id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ReporteSanciones reporteSanciones;
            try {
                reporteSanciones = em.getReference(ReporteSanciones.class, id);
                reporteSanciones.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reporteSanciones with id " + id + " no longer exists.", enfe);
            }
            em.remove(reporteSanciones);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ReporteSanciones> findReporteSancionesEntities() {
        return findReporteSancionesEntities(true, -1, -1);
    }

    public List<ReporteSanciones> findReporteSancionesEntities(int maxResults, int firstResult) {
        return findReporteSancionesEntities(false, maxResults, firstResult);
    }

    private List<ReporteSanciones> findReporteSancionesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ReporteSanciones.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public ReporteSanciones findReporteSanciones(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ReporteSanciones.class, id);
        } finally {
            em.close();
        }
    }

    public int getReporteSancionesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ReporteSanciones> rt = cq.from(ReporteSanciones.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
