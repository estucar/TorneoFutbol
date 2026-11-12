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
import modelo.ReporteArbitro;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class ReporteArbitroJpaController implements Serializable {

    public ReporteArbitroJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ReporteArbitro reporteArbitro) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(reporteArbitro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ReporteArbitro reporteArbitro) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            reporteArbitro = em.merge(reporteArbitro);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = reporteArbitro.getId();
                if (findReporteArbitro(id) == null) {
                    throw new NonexistentEntityException("The reporteArbitro with id " + id + " no longer exists.");
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
            ReporteArbitro reporteArbitro;
            try {
                reporteArbitro = em.getReference(ReporteArbitro.class, id);
                reporteArbitro.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reporteArbitro with id " + id + " no longer exists.", enfe);
            }
            em.remove(reporteArbitro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ReporteArbitro> findReporteArbitroEntities() {
        return findReporteArbitroEntities(true, -1, -1);
    }

    public List<ReporteArbitro> findReporteArbitroEntities(int maxResults, int firstResult) {
        return findReporteArbitroEntities(false, maxResults, firstResult);
    }

    private List<ReporteArbitro> findReporteArbitroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ReporteArbitro.class));
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

    public ReporteArbitro findReporteArbitro(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ReporteArbitro.class, id);
        } finally {
            em.close();
        }
    }

    public int getReporteArbitroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ReporteArbitro> rt = cq.from(ReporteArbitro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
