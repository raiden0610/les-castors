package fr.castor.ws.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * Classe abstraite pour l'ensemble des classes DAO
 *
 * @author Casaucau Cyril
 */
public class AbstractDAO<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDAO.class);
    @PersistenceContext
    protected EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public T create(T t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Création de : " + t.getClass().getSimpleName());
        }
        entityManager.persist(t);

        flushing("Problème de flush pendant la creation de :  ", t);

        return t;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public T createMandatory(T t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Création de : " + t.getClass().getSimpleName());
        }
        entityManager.persist(t);

        flushing("Problème de flush pendant la creation de :  ", t);

        return t;
    }

    private void flushing(String exceptionMessage, T t) {
        try {
            entityManager.flush();
        } catch (PersistenceException pe) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(exceptionMessage + t.getClass().getSimpleName(), pe);
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T update(T t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Mise à jour de : " + t.getClass().getSimpleName());
        }
        t = entityManager.merge(t);

        flushing("Problème de flush pendant l'update de : ", t);

        return t;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(T t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début suppression de : " + t.getClass().getSimpleName());
        }
        t = entityManager.merge(t);
        entityManager.remove(t);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin suppression de : " + t.getClass().getSimpleName());
        }
        try {
            entityManager.flush();
        } catch (PersistenceException pe) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Problème de flush pendant l'update de : " + t.getClass().getSimpleName(), pe);
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void refresh(T t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début merge de : " + t.getClass().getSimpleName());
        }
        entityManager.refresh(t);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin merge de : " + t.getClass().getSimpleName());
        }
        try {
            entityManager.flush();
        } catch (PersistenceException pe) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Problème de flush pendant l'update de : " + t.getClass().getSimpleName(), pe);
            }
        }
    }

}
