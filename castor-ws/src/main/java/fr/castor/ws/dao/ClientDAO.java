package fr.castor.ws.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import fr.castor.ws.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.castor.core.constant.QueryJPQL;
import fr.castor.core.exception.BackendException;
import fr.castor.core.exception.DuplicateEntityException;

/**
 * Classe d'accés aux données pour les clients
 * 
 * @author Casaucau Cyril
 */
@Stateless(name = "ClientDAO")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ClientDAO extends AbstractDAO<Client> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDAO.class);

    /**
     * Methode de récuperation d'un client en fonction de son login.
     * 
     * @param String
     *            Le login de l'utilsateur
     * @return Client vide si l'utilisateur n'existe pas.
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Client getClientByLoginName(String login) {

        Client clientFinded = null;

        try {
            TypedQuery<Client> query = entityManager.createNamedQuery(QueryJPQL.CLIENT_LOGIN, Client.class);
            query.setParameter(QueryJPQL.CLIENT_LOGIN, login);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Chargement requete JPQL OK ");
            }

            clientFinded = query.getSingleResult();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Récuperation resultat requete JPQL OK ");
            }

            return clientFinded;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return new Client();
        }
    }

    /**
     * Methode de récuperation d'un client en fonction de son email (utilisé
     * notament dans la verification de la duplication)
     * 
     * @param email
     *            L'email du client
     * @return Le client qui a le mail passé en param.
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Client getClientByEmail(String email) {

        Client clientFinded = null;

        try {
            TypedQuery<Client> query = entityManager.createNamedQuery(QueryJPQL.CLIENT_BY_EMAIL, Client.class);
            query.setParameter(QueryJPQL.PARAM_CLIENT_EMAIL, email);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Chargement requete JPQL client email OK ");
            }

            clientFinded = query.getSingleResult();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Récuperation resultat requete JPQL client email OK ");
            }

            return clientFinded;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return new Client();
        }
    }

    /**
     * Sauvegarde un client dans la base de données et check si l'utilisateur
     * existe deja
     * 
     * @param nouveauClient
     *            L'entité a ajouter dans la BDD
     * @throws BackendException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveNewClient(Client nouveauClient) throws DuplicateEntityException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut persistence d'un nouveau client......");
        }

        // Test paranoiaque n'est jamais censé arrivé......il a normalement été
        // checké par le frontend
        Client clientMemeLogin = getClientByLoginName(nouveauClient.getLogin());
        Client clientMemeEmail = getClientByEmail(nouveauClient.getEmail());

        if ("".equals(clientMemeLogin.getLogin()) && "".equals(clientMemeEmail.getLogin())) {
            entityManager.persist(nouveauClient);
        } else {
            StringBuilder sbError = new StringBuilder("Impossible de perister le client : ");
            sbError.append(nouveauClient.getLogin());
            sbError.append(" - ");
            sbError.append(nouveauClient.getEmail());
            sbError.append(" car il existe déjà");
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(sbError.toString());
            }

            throw new DuplicateEntityException(sbError.toString());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin persistence d'un nouveau client......OK");
        }
    }

    /**
     * Sauvegarde un client dans la base de données et check si l'utilisateur
     * existe deja
     * 
     * @param nouveauClient
     *            L'entité a ajouter dans la BDD
     * @throws BackendException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveClient(Client client) throws BackendException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut persistence d'un client......");
        }

        if (client != null) {
            entityManager.persist(client);
        } else {
            throw new BackendException("L'entité client est null, impossible de la persister");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin persistence d'un nouveau client......OK");
        }
    }

    /**
     * Active le compte d'un client
     * 
     * @param nouveauClient
     *            L'entité a ajouter dans la BDD
     * @throws BackendException
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Client getClientByActivationKey(String cleActivation) {

        Client clientFinded = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut récuperation compte client avec la clé : " + cleActivation);
        }

        try {
            TypedQuery<Client> query = entityManager.createNamedQuery(QueryJPQL.CLIENT_BY_ACTIVATION_KEY, Client.class);
            query.setParameter(QueryJPQL.PARAM_ACTIVATION_KEY, cleActivation);

            clientFinded = query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD pour la clé: " + cleActivation, nre);
            }
            return new Client();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin récuperation compte client avec la clé");
        }

        return clientFinded;
    }

    /**
     * Renvoi le hash pour un client donné
     * 
     * @param login
     *            Le login du client
     * @return
     */
    public String getHash(String login) {
        String hash = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut récuperation hash pour le client : " + login);
        }

        try {
            TypedQuery<String> query = entityManager.createNamedQuery(QueryJPQL.CLIENT_HASH_BY_LOGIN, String.class);
            query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

            hash = query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées pour le hash du client : " + login, nre);
            }
            return "";
        }
        return hash;
    }

    /**
     * Renvoi le statut pour un client donné
     * 
     * @param login
     *            Le login du client
     * @return
     */
    public Boolean getStatutActive(String login) {
        Boolean isActive = Boolean.FALSE;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut récuperation statut pour le client : " + login);
        }

        try {
            TypedQuery<Boolean> query = entityManager.createNamedQuery(QueryJPQL.CLIENT_STATUT_BY_LOGIN, Boolean.class);
            query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

            isActive = query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées pour le statut du client : " + login, nre);
            }
            return Boolean.FALSE;
        }
        return isActive;
    }

}
