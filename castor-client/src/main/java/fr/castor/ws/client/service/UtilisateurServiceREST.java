package fr.castor.ws.client.service;

import fr.castor.core.constant.Constant;
import fr.castor.core.constant.WsPath;
import fr.castor.dto.ClientDTO;
import fr.castor.dto.DemandeMesAnnoncesDTO;
import fr.castor.dto.LoginDTO;
import fr.castor.dto.ModifClientDTO;
import fr.castor.dto.aggregate.MesAnnoncesAnnonceDTO;
import fr.castor.dto.aggregate.MesAnnoncesNotificationDTO;
import fr.castor.dto.helper.DeserializeJsonHelper;
import fr.castor.ws.client.WsConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * 
 * Sert à appeler les services de controle des utilisateurs du WS
 * 
 * @author Casaucau Cyril
 * 
 */
@Named("utilisateurServiceREST")
public class UtilisateurServiceREST implements Serializable{

    private static final long serialVersionUID = 8722133810649462157L;

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateurServiceREST.class);

    @Inject
    private WsConnector wsConnector;

    public int activateAccount(String cleActivation) {

        int resultatService;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation client par email + deserialization");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_ACTIVATION, cleActivation);

        resultatService = Integer.parseInt(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service service de recuperation client par email + deserialization");
        }

        return resultatService;
    }

    public String getHashByLogin(String login) {

        String hash = "";

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation du hash par login + deserialization");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_HASH, login);

        hash = String.valueOf(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de recuperation du hash par login + deserialization");
        }

        return hash;
    }

    public String getRolesByLogin(String login) {
        String roles = "";

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation des roles par login + deserialization");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_ROLES, login);

        roles = String.valueOf(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de recuperation du hash par login + deserialization");
        }

        return roles;
    }

    /**
     * Récupérer un client a partir de son email
     * 
     * @param email
     *            L'email du client
     * @return Les informations du client, vide si il n'existe pas.
     */
    public ClientDTO getUtilisateurByEmail(String email) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation client par email + deserialization");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_BY_EMAIL, email);

        ClientDTO clientDTO = DeserializeJsonHelper.deserializeDTO(objectInJSON, ClientDTO.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service service de recuperation client par email + deserialization");
        }

        return clientDTO;
    }

    /**
     * Verification nom utilisateur / mdp
     * 
     * @param loginDTO
     *            l'objet d'échange pour verifier les données.
     * @return true si le couple login / mdp correspond.
     */
    public ClientDTO login(LoginDTO loginDTO) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service login + deserialization");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_LOGIN, loginDTO);

        ClientDTO clientDTO = DeserializeJsonHelper.deserializeDTO(objectInJSON, ClientDTO.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service login + deserialization");
        }

        return clientDTO;
    }

    /**
     * Mise a jour des infos de l'utilisateur
     * 
     * @param utilisateurToUpdate
     *            DTO contenant les informations
     * @return code retour @see {@link Constant}
     */
    public Integer updateUtilisateurInfos(ModifClientDTO utilisateurToUpdate) {
        Integer codeRetour;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de mise a jour des informations du client");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_UPDATE_INFO, utilisateurToUpdate);

        codeRetour = Integer.valueOf(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de mise a jour des informations du client");
        }

        return codeRetour;
    }

    public MesAnnoncesAnnonceDTO getAnnonceForMesAnnonces(DemandeMesAnnoncesDTO demandeMesAnnoncesDTO) {
        MesAnnoncesAnnonceDTO mesAnnoncesAnnonceDTO = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation des données de la page mes annonces (annonces)");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_ANNONCES_FOR_MES_ANNONCES, demandeMesAnnoncesDTO);

        mesAnnoncesAnnonceDTO = DeserializeJsonHelper.deserializeDTO(objectInJSON, MesAnnoncesAnnonceDTO.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de recuperation des données de la page mes annonces + deserialization (annonces)");
        }

        return mesAnnoncesAnnonceDTO;
    }

    public MesAnnoncesNotificationDTO getNotificationForMesAnnonces(DemandeMesAnnoncesDTO demandeMesAnnoncesDTO) {
        MesAnnoncesNotificationDTO mesAnnoncesNotificationDTO = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation des données de la page mes annonces (notifications)");
        }

        String objectInJSON = wsConnector.sendRequestJSON(WsPath.GESTION_UTILISATEUR_SERVICE_PATH,
                WsPath.GESTION_UTILISATEUR_SERVICE_NOTIFICATIONS_FOR_MES_ANNONCES, demandeMesAnnoncesDTO);

        mesAnnoncesNotificationDTO = DeserializeJsonHelper.deserializeDTO(objectInJSON, MesAnnoncesNotificationDTO.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de recuperation des données de la page mes annonces + deserialization (notifications)");
        }

        return mesAnnoncesNotificationDTO;
    }
}