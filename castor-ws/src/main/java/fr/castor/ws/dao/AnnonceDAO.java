package fr.castor.ws.dao;

import fr.castor.core.constant.QueryJPQL;
import fr.castor.core.exception.DuplicateEntityException;
import fr.castor.dto.DemandeAnnonceDTO;
import fr.castor.dto.enums.EtatAnnonce;
import fr.castor.ws.utils.RolesUtils;
import fr.castor.ws.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe d'accés aux données pour les annonces
 *
 * @author Casaucau Cyril
 */
@Stateless(name = "AnnonceDAO")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AnnonceDAO extends AbstractDAO<Annonce> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonceDAO.class);

    @Inject
    private RolesUtils rolesUtils;

    /**
     * Récupère les annonces par login de leurs utilsateurs
     *
     * @param login le login du client dont on veut recupérer les annonces.
     * @return Liste d'annonces appartenant à l'utilisateur.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Object[]> getAnnoncesByLoginForMesAnnonces(String login, boolean isArtisan, Integer rangeDebut, Integer rangeFin) {

        List<Object[]> listAnnonceByLogin = null;

        try {
            TypedQuery<Object[]> query = null;

            if (isArtisan) {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_ARTISAN_LOGIN_FETCH_ARTISAN,
                        Object[].class);
            } else {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_DEMANDEUR_LOGIN_FETCH_ARTISAN,
                        Object[].class);
            }

            query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

            query.setFirstResult(rangeDebut);
            query.setMaxResults(rangeFin);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Chargement requete JPQL OK ");
            }

            listAnnonceByLogin = query.getResultList();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Récuperation resultat requete JPQL OK ");
            }

            return listAnnonceByLogin;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les annonces par login de leurs utilsateurs
     *
     * @param login le login du client dont on veut recupérer les annonces.
     * @return Liste d'annonces appartenant à l'utilisateur.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<Annonce> getAnnoncesByLogin(String login) {

        List<Annonce> listAnnonceByLogin = null;

        try {
            TypedQuery<Annonce> query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_LOGIN, Annonce.class);
            query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Chargement requete JPQL OK ");
            }

            listAnnonceByLogin = query.getResultList();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Récuperation resultat requete JPQL OK ");
            }

            return listAnnonceByLogin;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return new ArrayList<Annonce>();
        }
    }

    /**
     * Recupere les annonces par titre, description et login : notament utilisé
     * dans la verification de la duplication.
     *
     * @param titre       Le titre de l'annonce.
     * @param description La description de l'annonce.
     * @param login       Le login du cliebnt
     * @return La liste d'annonce qui correspond au titre, description et
     * utilsateur present en BDD
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Annonce> getAnnonceByTitleAndDescriptionAndLogin(String description, String login) {

        List<Annonce> annoncesBytitreAndDescription = null;

        try {
            TypedQuery<Annonce> query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_TITLE_AND_DESCRIPTION,
                    Annonce.class);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_DESCRIPTION, description);
            query.setParameter(QueryJPQL.CLIENT_LOGIN, login);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Chargement requete JPQL OK ");
            }

            annoncesBytitreAndDescription = query.getResultList();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Récuperation resultat requete JPQL OK ");
            }

            return annoncesBytitreAndDescription;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return new ArrayList<Annonce>();
        }
    }

    /**
     * Sauvegarde d'une annonce lors de son initialisation, check dans la bdd si
     * elle existe déjà pour un utilisateur donné.
     *
     * @param nouvelleAnnonce L'annonce a sauvegarder dans la bdd
     * @throws DuplicateEntityException Exception throw si l'entité existe déjà.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveAnnonceFirstTime(Annonce nouvelleAnnonce) throws DuplicateEntityException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut persistence d'une nouvelle annonce......");
        }

        List<Annonce> annoncesDupliquees = getAnnonceByTitleAndDescriptionAndLogin(nouvelleAnnonce.getDescription(),
                nouvelleAnnonce.getDemandeur().getLogin());

        // On check si l'annonce n'existe pas déjà
        if (annoncesDupliquees.isEmpty()) {
            entityManager.persist(nouvelleAnnonce);
        } else {
            StringBuilder sbError = new StringBuilder("Impossible de perister l'annonce: ");
            sbError.append(nouvelleAnnonce.getId());
            sbError.append(" car elle existe déjà");

            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(sbError.toString());
            }

            throw new DuplicateEntityException(sbError.toString());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin persistence d'une nouvelle annonce......OK");
        }
    }

    /**
     * Calcul le nb d'annonce qu'un client a postés
     *
     * @param login le login du client
     * @return Le nb d'annonce
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long getNbAnnonceByLoginForClient(String login) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut comptage du nb d'annonce");
        }

        TypedQuery<Long> query = entityManager.createNamedQuery(QueryJPQL.NB_ANNONCE_BY_LOGIN_FOR_CLIENT, Long.class);
        query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin comptage du nb d'annonce");
        }

        return query.getSingleResult();
    }

    /**
     * Calcul le nb d'annonce surlesquuels un artisan est inscrit.
     *
     * @param login le login de l'artisan
     * @return Le nb d'annonce
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long getNbAnnonceByLoginForArtisan(String login) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut comptage du nb d'annonce");
        }

        TypedQuery<Long> query = entityManager.createNamedQuery(QueryJPQL.NB_ANNONCE_BY_LOGIN_FOR_ARTISAN, Long.class);
        query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, login);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin comptage du nb d'annonce");
        }

        return query.getSingleResult();
    }

    /**
     * Récupère les informations d'une annonce dans le but de les afficher.
     *
     * @param login le login du client
     * @return Le nb d'annonce
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Annonce getAnnonceByIDForAffichage(String id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de la récuperation de l'annonce par l'id");
        }

        try {
            TypedQuery<Annonce> query = entityManager.createNamedQuery(
                    QueryJPQL.ANNONCE_BY_ID_FETCH_ARTISAN_ENTREPRISE_CLIENT_ADRESSE, Annonce.class);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_ID, id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fin de la récuperation de l'annonce par l'id");
            }

            return query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return null;
        }
    }

    /**
     * Charge une annonce grace à son hash ID, ne crée pas de transaction
     *
     * @param login le login du client
     * @return Le nb d'annonce
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Annonce getAnnonceByIDWithoutTransaction(String id, boolean isAdmin) {
        return getAnnonceByID(id, isAdmin);
    }

    /**
     * Charge une annonce grace à son hash ID, crée une transaction
     *
     * @param login le login du client
     * @return Le nb d'annonce
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Annonce getAnnonceByIDWithTransaction(String id, boolean isAdmin) {
        return getAnnonceByID(id, isAdmin);
    }

    private Annonce getAnnonceByID(String id, boolean isAdmin) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de la récuperation de l'annonce par l'id");
        }

        TypedQuery<Annonce> query = null;
        try {
            if (isAdmin) {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_ID_ADMIN, Annonce.class);
            } else {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_ID, Annonce.class);
            }

            query.setParameter(QueryJPQL.PARAM_ANNONCE_ID, id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fin de la récuperation de l'annonce par l'id");
            }

            return query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return null;
        }
    }

    /**
     * Mets a jour le nb de consultation d'une annonce dans la BDD
     *
     * @param nbConsultation le nb de consultation deja incrémenté
     * @param hashID         L'identifiant unique de l'annonce.
     * @return True si la mise a jour s'est bien passé .
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Boolean updateAnnonceNbConsultationByHashId(Integer nbConsultation, String hashID) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de mise a jour du nb de consultation d'une annonce");
        }

        try {
            Query query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_UPDATE_NB_CONSULTATION);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_ID, hashID);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_NB_CONSULTATION, nbConsultation);
            Integer nbUpdated = query.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("nb de row updated: " + nbUpdated);
                LOGGER.debug("Fin de la mise a jour du nb de consultation d'une annonce");
            }

            if (nbUpdated == 0 || nbUpdated != 1) {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return Boolean.FALSE;
        }
    }

    /**
     * Supprime une annonce présente en base de données : <br/>
     * Attention ce n'est pas vraiment une supression, on ne fait que desactiver
     * l'annonce en base de données.
     *
     * @param demandeAnnonceDTO Objet qui possede les infos pour verifier que l'utilisateur a
     *                          les droits et pour supprimer l'annonce.
     * @return True si la suppression s'est bien passée.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Boolean suppressionAnnonce(DemandeAnnonceDTO demandeAnnonceDTO) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de la suppression de l'annonce : " + demandeAnnonceDTO.getId());
            LOGGER.debug("A la demande de : " + demandeAnnonceDTO.getLoginDemandeur());
            LOGGER.debug("Et qui possede le role : " + demandeAnnonceDTO.getTypeCompteDemandeur());
        }
        Query query = null;
        try {
            if (rolesUtils.checkIfClient(demandeAnnonceDTO.getTypeCompteDemandeur())) {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_SUPRESS_ANNONCE_FOR_CLIENT);
                query.setParameter(QueryJPQL.PARAM_CLIENT_LOGIN, demandeAnnonceDTO.getLoginDemandeur());
            } else if (rolesUtils.checkIfAdmin(demandeAnnonceDTO.getTypeCompteDemandeur())) {
                query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_SUPRESS_ANNONCE_FOR_ADMIN);
            }

            query.setParameter(QueryJPQL.PARAM_ANNONCE_ID, demandeAnnonceDTO.getId());
            query.setParameter(QueryJPQL.PARAM_ANNONCE_ETAT, EtatAnnonce.SUPPRIMER);

            Integer nbUpdated = query.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fin de la suppression de l'annonce");
            }

            if (nbUpdated == 0 || nbUpdated != 1) {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return Boolean.FALSE;
        }
    }

    /**
     * Cherche en base de données et desactive toutes les annonces qui sont plus
     * petite que la date passée en paramètre et qui ont un nombre d'artisan
     * inscrit égale a la properties du nb max d'artisan <br/>
     *
     * @param todayMinusXDays : la date du jour - le nb de jour present dans le fichier de
     *                        properties
     * @param nbMaxArtisan    : Nombre max d'artisan pouvant s'inscrire a une annonce (voir
     *                        fichier de properties)
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void desactiveAnnoncePerime(Date todayMinusXDays, Integer nbMaxArtisan) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de mise a jour des annonces périmées");
        }

        try {
            Query query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_DESACTIVE_PERIMEE);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_TODAY_MINUS_X_DAYS, todayMinusXDays);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_NB_ARTISAN_MAX, nbMaxArtisan);
            Integer nbUpdated = query.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("nb de row updated: " + nbUpdated);
                LOGGER.debug("Fin de mise a jour des annonces périmées");
            }

        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
        }
    }

    /**
     * Recherche une annonce par son id et le login du demandeur <br/>
     *
     * @param hashID         L'id unique de l'annonce
     * @param loginDemandeur Le login du demandeur (celui qui a poster l'annonce)
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Annonce getAnnonceByIdByLogin(String hashID, String loginDemandeur) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de requete selection annonce par hash et demandeur");
        }

        TypedQuery<Annonce> query = null;
        try {

            query = entityManager.createNamedQuery(QueryJPQL.ANNONCE_BY_HASHID_AND_DEMANDEUR, Annonce.class);

            query.setParameter(QueryJPQL.PARAM_ANNONCE_ID, hashID);
            query.setParameter(QueryJPQL.PARAM_ANNONCE_DEMANDEUR_LOGIN, loginDemandeur);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fin de requete selection annonce par hash et demandeur");
            }

            return query.getSingleResult();
        } catch (NoResultException nre) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Aucune correspondance trouvées dans la BDD", nre);
            }
            return null;
        }
    }

    /**
     * Permet d'effectuer une recherche d'annonce dans la base de données.
     *
     * @param categoriesMetier La catégorie de l'annonce
     * @param aPartirDu        A partir de quelle date
     * @param departement      Le departement où se trouve le chantier
     * @param rangeDebut       Pagination de début
     * @param rangeFin         Pagination de fin
     * @return La liste d'annonces correspondantent
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Annonce> searchAnnonce(List<Short> categoriesMetier, Integer departement
            , Integer rangeDebut, Integer rangeFin) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de requete de recherche d'annonce");
        }

        CriteriaBuilder criteriaBuilderSearch = entityManager.getCriteriaBuilder();
        CriteriaQuery<Annonce> searchCriteria = criteriaBuilderSearch.createQuery(Annonce.class);
        Root<Annonce> searchAnnonceRoot = searchCriteria.from(Annonce.class);

        //Ajout des prédicats à la requete
        searchCriteria.where(createSearchPredicate(searchAnnonceRoot, categoriesMetier, departement));
        searchCriteria.orderBy(criteriaBuilderSearch.desc(searchAnnonceRoot.get(Annonce_.dateCreation)));

        //Preparation au lancement de la requete
        TypedQuery<Annonce> searchQuery = entityManager.createQuery(searchCriteria.distinct(true));
        //Gestion de la pagination
        searchQuery.setFirstResult(rangeDebut);
        searchQuery.setMaxResults(rangeFin);
        //Récuperation des résutats
        return searchQuery.getResultList();
    }

    /**
     * Permet de compter le nombre de résultat total pour une demande de recherche
     *
     * @param categoriesMetier La catégorie de l'annonce
     * @param aPartirDu        A partir de quelle date
     * @param departement      Le departement où se trouve le chantier
     * @return La liste d'annonces correspondantent
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Long countSearchAnnonce(List<Short> categoriesMetier, Integer departement) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Debut de requete de recherche d'annonce");
        }

        CriteriaBuilder criteriaBuilderSearch = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> searchCriteria = criteriaBuilderSearch.createQuery(Long.class);
        searchCriteria.distinct(true);
        Root<Annonce> searchAnnonceRoot = searchCriteria.from(Annonce.class);
        //On lui dit de compter les annonces.
        searchCriteria.select(criteriaBuilderSearch.countDistinct(searchAnnonceRoot));

        //Ajout des prédicats à la requete
        searchCriteria.where(createSearchPredicate(searchAnnonceRoot, categoriesMetier,  departement));

        //Preparation au lancement de la requete
        TypedQuery<Long> searchQuery = entityManager.createQuery(searchCriteria);

        //Récuperation des résutats
        return searchQuery.getSingleResult();
    }

    private Predicate createSearchPredicate(Root<Annonce> searchAnnonceRoot, List<Short> categoriesMetier, Integer departement) {
        CriteriaBuilder criteriaBuilderSearch = entityManager.getCriteriaBuilder();

        //Join Annonce => Mot clé, Mot clé Catégorie join
        Join<Annonce, MotCle> motCleJoin = searchAnnonceRoot.join(Annonce_.motcles);
        Join<MotCle, CategorieMetier> categorieJoin = motCleJoin.join(MotCle_.categoriesMetier);

        //Predicat pour les catégories
        Expression<Short> categNumberExpression = categorieJoin.get(CategorieMetier_.categorieMetier);
        Predicate categPredicate = categNumberExpression.in(categoriesMetier);

        //Predicat pour l'etat de l'annonce
        Expression<EtatAnnonce> etatAnnonceExpression = searchAnnonceRoot.get(Annonce_.etatAnnonce);
        Predicate etatAnnoncePredicate = etatAnnonceExpression.in(EtatAnnonce.ACTIVE);

        //Join avec adresse pour comparer le departement
        Join<Annonce, Adresse> adresseJoin = searchAnnonceRoot.join(Annonce_.adresseChantier);
        Predicate departementPredicate = criteriaBuilderSearch.and(criteriaBuilderSearch.equal(adresseJoin.get(Adresse_.departement), departement));

        //Fusion des predicats avec des "ET"
        categPredicate = criteriaBuilderSearch.and(etatAnnoncePredicate, departementPredicate, categPredicate);

        return categPredicate;
    }


}