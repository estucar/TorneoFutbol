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
import modelo.ReportePosicion;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class ReportePosicionJpaController implements Serializable {

    public ReportePosicionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ReportePosicion reportePosicion) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(reportePosicion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ReportePosicion reportePosicion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            reportePosicion = em.merge(reportePosicion);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = reportePosicion.getId();
                if (findReportePosicion(id) == null) {
                    throw new NonexistentEntityException("The reportePosicion with id " + id + " no longer exists.");
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
            ReportePosicion reportePosicion;
            try {
                reportePosicion = em.getReference(ReportePosicion.class, id);
                reportePosicion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reportePosicion with id " + id + " no longer exists.", enfe);
            }
            em.remove(reportePosicion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ReportePosicion> findReportePosicionEntities() {
        return findReportePosicionEntities(true, -1, -1);
    }

    public List<ReportePosicion> findReportePosicionEntities(int maxResults, int firstResult) {
        return findReportePosicionEntities(false, maxResults, firstResult);
    }

    private List<ReportePosicion> findReportePosicionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ReportePosicion.class));
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

    public ReportePosicion findReportePosicion(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ReportePosicion.class, id);
        } finally {
            em.close();
        }
    }

    public int getReportePosicionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ReportePosicion> rt = cq.from(ReportePosicion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
