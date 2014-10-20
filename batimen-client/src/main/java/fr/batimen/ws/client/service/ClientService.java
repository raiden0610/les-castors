package fr.batimen.ws.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.batimen.core.constant.WsPath;
import fr.batimen.dto.ClientDTO;
import fr.batimen.dto.LoginDTO;
import fr.batimen.ws.client.WsConnector;

/**
 * 
 * Sert à appeler les services de controle des utilisateurs du WS
 * 
 * @author Casaucau Cyril
 * 
 */
public class ClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientService.class);

    private ClientService() {

    }

    /**
     * Verification nom utilisateur / mdp
     * 
     * @param loginDTO
     *            l'objet d'échange pour verifier les données.
     * @return true si le couple login / mdp correspond.
     */
    public static ClientDTO login(LoginDTO loginDTO) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service login + deserialization");
        }

        String objectInJSON = WsConnector.getInstance().sendRequest(WsPath.GESTION_CLIENT_SERVICE_PATH,
                WsPath.GESTION_CLIENT_SERVICE_LOGIN, loginDTO);

        ClientDTO clientDTO = ClientDTO.deserializeUserDTO(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service login + deserialization");
        }

        return clientDTO;
    }

    /**
     * Récupérer un client a partir de son email
     * 
     * @param email
     *            L'email du client
     * @return Les informations du client, vide si il n'existe pas.
     */
    public static ClientDTO getClientByEmail(String email) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation client par email + deserialization");
        }

        String objectInJSON = WsConnector.getInstance().sendRequest(WsPath.GESTION_CLIENT_SERVICE_PATH,
                WsPath.GESTION_CLIENT_SERVICE_BY_EMAIL, email);

        ClientDTO clientDTO = ClientDTO.deserializeUserDTO(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service service de recuperation client par email + deserialization");
        }

        return clientDTO;
    }

    public static int activateAccount(String cleActivation) {

        int resultatService;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation client par email + deserialization");
        }

        String objectInJSON = WsConnector.getInstance().sendRequest(WsPath.GESTION_CLIENT_SERVICE_PATH,
                WsPath.GESTION_CLIENT_SERVICE_ACTIVATION, cleActivation);

        resultatService = Integer.parseInt(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service service de recuperation client par email + deserialization");
        }

        return resultatService;
    }

    public static String getHashByLogin(String login) {

        String hash = "";

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service de recuperation du hash par login + deserialization");
        }

        String objectInJSON = WsConnector.getInstance().sendRequest(WsPath.GESTION_CLIENT_SERVICE_PATH,
                WsPath.GESTION_CLIENT_SERVICE_HASH, login);

        hash = String.valueOf(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service de recuperation du hash par login + deserialization");
        }

        return hash;
    }
}
