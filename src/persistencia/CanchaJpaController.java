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
import modelo.Cancha;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class CanchaJpaController implements Serializable {

    public CanchaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;
    
    public CanchaJpaController() {
        this.emf = javax.persistence.Persistence.createEntityManagerFactory("TorneoDeFutbolPU");
    }
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cancha cancha) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(cancha);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cancha cancha) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            cancha = em.merge(cancha);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = cancha.getId();
                if (findCancha(id) == null) {
                    throw new NonexistentEntityException("The cancha with id " + id + " no longer exists.");
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
            Cancha cancha;
            try {
                cancha = em.getReference(Cancha.class, id);
                cancha.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cancha with id " + id + " no longer exists.", enfe);
            }
            em.remove(cancha);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cancha> findCanchaEntities() {
        return findCanchaEntities(true, -1, -1);
    }

    public List<Cancha> findCanchaEntities(int maxResults, int firstResult) {
        return findCanchaEntities(false, maxResults, firstResult);
    }

    private List<Cancha> findCanchaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cancha.class));
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

    public Cancha findCancha(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cancha.class, id);
        } finally {
            em.close();
        }
    }

    public int getCanchaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cancha> rt = cq.from(Cancha.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
