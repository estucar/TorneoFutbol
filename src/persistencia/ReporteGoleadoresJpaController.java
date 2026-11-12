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
import modelo.ReporteGoleadores;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class ReporteGoleadoresJpaController implements Serializable {

    public ReporteGoleadoresJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ReporteGoleadores reporteGoleadores) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(reporteGoleadores);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ReporteGoleadores reporteGoleadores) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            reporteGoleadores = em.merge(reporteGoleadores);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = reporteGoleadores.getId();
                if (findReporteGoleadores(id) == null) {
                    throw new NonexistentEntityException("The reporteGoleadores with id " + id + " no longer exists.");
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
            ReporteGoleadores reporteGoleadores;
            try {
                reporteGoleadores = em.getReference(ReporteGoleadores.class, id);
                reporteGoleadores.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reporteGoleadores with id " + id + " no longer exists.", enfe);
            }
            em.remove(reporteGoleadores);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ReporteGoleadores> findReporteGoleadoresEntities() {
        return findReporteGoleadoresEntities(true, -1, -1);
    }

    public List<ReporteGoleadores> findReporteGoleadoresEntities(int maxResults, int firstResult) {
        return findReporteGoleadoresEntities(false, maxResults, firstResult);
    }

    private List<ReporteGoleadores> findReporteGoleadoresEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ReporteGoleadores.class));
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

    public ReporteGoleadores findReporteGoleadores(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ReporteGoleadores.class, id);
        } finally {
            em.close();
        }
    }

    public int getReporteGoleadoresCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ReporteGoleadores> rt = cq.from(ReporteGoleadores.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
