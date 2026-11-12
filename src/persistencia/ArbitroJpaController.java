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
import modelo.Arbitro;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class ArbitroJpaController implements Serializable {

    public ArbitroJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Arbitro arbitro) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(arbitro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Arbitro arbitro) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            arbitro = em.merge(arbitro);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = arbitro.getId();
                if (findArbitro(id) == null) {
                    throw new NonexistentEntityException("The arbitro with id " + id + " no longer exists.");
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
            Arbitro arbitro;
            try {
                arbitro = em.getReference(Arbitro.class, id);
                arbitro.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The arbitro with id " + id + " no longer exists.", enfe);
            }
            em.remove(arbitro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Arbitro> findArbitroEntities() {
        return findArbitroEntities(true, -1, -1);
    }

    public List<Arbitro> findArbitroEntities(int maxResults, int firstResult) {
        return findArbitroEntities(false, maxResults, firstResult);
    }

    private List<Arbitro> findArbitroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Arbitro.class));
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

    public Arbitro findArbitro(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Arbitro.class, id);
        } finally {
            em.close();
        }
    }

    public int getArbitroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Arbitro> rt = cq.from(Arbitro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
