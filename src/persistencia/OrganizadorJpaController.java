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
import modelo.Organizador;
import persistencia.exceptions.NonexistentEntityException;

/**
 *
 * @author Acer
 */
public class OrganizadorJpaController implements Serializable {

    public OrganizadorJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Organizador organizador) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(organizador);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Organizador organizador) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            organizador = em.merge(organizador);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                int id = organizador.getId();
                if (findOrganizador(id) == null) {
                    throw new NonexistentEntityException("The organizador with id " + id + " no longer exists.");
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
            Organizador organizador;
            try {
                organizador = em.getReference(Organizador.class, id);
                organizador.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The organizador with id " + id + " no longer exists.", enfe);
            }
            em.remove(organizador);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Organizador> findOrganizadorEntities() {
        return findOrganizadorEntities(true, -1, -1);
    }

    public List<Organizador> findOrganizadorEntities(int maxResults, int firstResult) {
        return findOrganizadorEntities(false, maxResults, firstResult);
    }

    private List<Organizador> findOrganizadorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Organizador.class));
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

    public Organizador findOrganizador(int id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Organizador.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrganizadorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Organizador> rt = cq.from(Organizador.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
