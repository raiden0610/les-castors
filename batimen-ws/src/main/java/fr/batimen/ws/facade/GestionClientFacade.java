package fr.batimen.ws.facade;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.batimen.core.constant.Constant;
import fr.batimen.core.constant.WsPath;
import fr.batimen.dto.AnnonceDTO;
import fr.batimen.dto.NotationDTO;
import fr.batimen.dto.NotificationDTO;
import fr.batimen.dto.aggregate.MesAnnoncesDTO;
import fr.batimen.dto.aggregate.MonProfilDTO;
import fr.batimen.dto.enums.TypeCompte;
import fr.batimen.dto.helper.DeserializeJsonHelper;
import fr.batimen.ws.dao.AnnonceDAO;
import fr.batimen.ws.dao.NotationDAO;
import fr.batimen.ws.entity.Notation;
import fr.batimen.ws.helper.JsonHelper;
import fr.batimen.ws.interceptor.BatimenInterceptor;
import fr.batimen.ws.service.NotificationService;

/**
 * Facade REST de gestion des clients
 * 
 * @author Casaucau Cyril
 * 
 */
@Stateless(name = "GestionClientFacade")
@LocalBean
@Path(WsPath.GESTION_CLIENT_SERVICE_PATH)
@RolesAllowed(Constant.USERS_ROLE)
@Produces(JsonHelper.JSON_MEDIA_TYPE_AND_UTF_8_CHARSET)
@Consumes(JsonHelper.JSON_MEDIA_TYPE_AND_UTF_8_CHARSET)
@Interceptors(value = { BatimenInterceptor.class })
@TransactionManagement(TransactionManagementType.CONTAINER)
public class GestionClientFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionClientFacade.class);

    @Inject
    private GestionAnnonceFacade gestionAnnonceFacade;

    @Inject
    private AnnonceDAO annonceDAO;

    @Inject
    private NotationDAO notationDAO;

    @Inject
    private NotificationService notificationService;

    /**
     * Methode de récuperation des informations de la page de mes annonces
     * (notifications + annonces) d'un client
     * 
     * @param login
     * @return
     */
    @POST
    @Path(WsPath.GESTION_CLIENT_SERVICE_INFOS_MES_ANNONCES)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public MesAnnoncesDTO getInfoForMesAnnonces(String login) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Récuperation des annonces / notifs pour : " + login);
        }

        MesAnnoncesDTO mesAnnoncesDTO = new MesAnnoncesDTO();
        String loginEscaped = DeserializeJsonHelper.parseString(login);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Récuperation notification.........");
        }

        List<NotificationDTO> notificationsDTO = notificationService.getNotificationByLogin(loginEscaped,
                TypeCompte.CLIENT);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Récuperation annonce.........");
        }

        List<AnnonceDTO> annoncesDTO = gestionAnnonceFacade.getAnnoncesByClientLoginForMesAnnonces(loginEscaped);

        mesAnnoncesDTO.setNotifications(notificationsDTO);
        mesAnnoncesDTO.setAnnonces(annoncesDTO);

        return mesAnnoncesDTO;
    }

    /**
     * Methode de récuperation des informations de la page de mon profil d'un
     * client
     * 
     * @param login
     * @return
     */
    @POST
    @Path(WsPath.GESTION_CLIENT_SERVICE_INFOS_MON_PROFIL)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public MonProfilDTO getInfoForMonProfil(String login) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Récuperation des infos de la page de mon profil pour : " + login);
        }
        String loginEscaped = DeserializeJsonHelper.parseString(login);

        // Recup des données
        Long nbAnnonce = annonceDAO.getNbAnnonceByLogin(loginEscaped);
        List<Object[]> notations = notationDAO.getNotationByLoginClient(loginEscaped, Boolean.TRUE);

        // Remplissage de la DTO
        MonProfilDTO monProfilDTO = new MonProfilDTO();
        monProfilDTO.setNbAnnonce(nbAnnonce);

        // Extraction de l'entité puis transfert dans la DTO
        ModelMapper mapper = new ModelMapper();
        for (Object[] notation : notations) {
            // On crée la DTO
            NotationDTO notationDTO = new NotationDTO();
            // On cast object en entity
            Notation notationCasted = (Notation) notation[0];
            // on le passe dans le mapper pour extraire les infos dans la DTO
            mapper.map(notationCasted, notationDTO);
            // On charge le nom de l'entreprise concernant cette notation
            notationDTO.setNomEntreprise(String.valueOf(notation[1]));
            // On ajoute la notation a la liste qui sera presente dans la DTO
            // MonProfil
            monProfilDTO.getNotations().add(notationDTO);
        }

        return monProfilDTO;
    }
}