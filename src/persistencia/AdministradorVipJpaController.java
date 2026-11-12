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
import modelo.AdministradorVip;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class AdministradorVipJpaController implements Serializable {

    public AdministradorVipJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AdministradorVip administradorVip) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(administradorVip);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AdministradorVip administradorVip) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            administradorVip = em.merge(administradorVip);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = administradorVip.getId();
                if (findAdministradorVip(id) == null) {
                    throw new NonexistentEntityException("The administradorVip with id " + id + " no longer exists.");
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
            AdministradorVip administradorVip;
            try {
                administradorVip = em.getReference(AdministradorVip.class, id);
                administradorVip.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The administradorVip with id " + id + " no longer exists.", enfe);
            }
            em.remove(administradorVip);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AdministradorVip> findAdministradorVipEntities() {
        return findAdministradorVipEntities(true, -1, -1);
    }

    public List<AdministradorVip> findAdministradorVipEntities(int maxResults, int firstResult) {
        return findAdministradorVipEntities(false, maxResults, firstResult);
    }

    private List<AdministradorVip> findAdministradorVipEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AdministradorVip.class));
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

    public AdministradorVip findAdministradorVip(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AdministradorVip.class, id);
        } finally {
            em.close();
        }
    }

    public int getAdministradorVipCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AdministradorVip> rt = cq.from(AdministradorVip.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
