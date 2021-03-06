package fr.castor.ws.dao;

import fr.castor.core.constant.QueryJPQL;
import fr.castor.ws.entity.Avis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe d'accés aux données pour les notations.
 *
 * @author Casaucau Cyril
 */
@Stateless(name = "NotationDAO")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class NotationDAO extends AbstractDAO<Avis> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotationDAO.class);

    /**
     * Recupere les notations pour un client via ces annonces par ordre
     * decroissant ainsi que le nom complet des entreprises correspondantes.
     *
     * @param login                le login du client
     * @param limitToThreeNotation limite la requete aux 3 derniers avis
     * @return les notations et les noms complets associés des entreprises
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Object[]> getNotationByLoginClient(String login, Boolean limitToThreeNotation) {
        List<Object[]> notations = null;

        try {
            TypedQuery<Object[]> query = entityManager.createNamedQuery(QueryJPQL.NOTATION_BY_CLIENT_LOGIN,
                    Object[].class);
            query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

            if (limitToThreeNotation) {
                query.setMaxResults(3);
            }

            notations = query.getResultList();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Pas de résultat sur la requète de notation", nre);
            }
            return new ArrayList<Object[]>();
        }

        return notations;
    }

    /**
     * Recupere les notations pour une entreprise
     * <p/>
     * Si on ne veut pas limiter les resultats, mettre maxResult à zéro.
     *
     * @param siret le siret de l'entreprise partenaire
     * @return les notations des entreprises.
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Avis> getNotationByEntrepriseSiret(String siret, int maxResult) {
        List<Avis> notations = null;

        try {
            TypedQuery<Avis> query = entityManager.createNamedQuery(QueryJPQL.NOTATION_BY_ENTREPRISE_SIRET,
                    Avis.class);
            query.setParameter(QueryJPQL.PARAM_ENTREPRISE_SIRET, siret);

            if (maxResult != 0) {
                query.setMaxResults(maxResult);
            }

            notations = query.getResultList();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Pas de résultat sur la requète de notation", nre);
            }
            return new ArrayList<>();
        }

        return notations;
    }
}