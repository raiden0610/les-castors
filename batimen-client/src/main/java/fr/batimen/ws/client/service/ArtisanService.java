package fr.batimen.ws.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.batimen.core.constant.Constant;
import fr.batimen.core.constant.WsPath;
import fr.batimen.dto.aggregate.CreationPartenaireDTO;
import fr.batimen.ws.client.WsConnector;

/**
 * Classe d'appel du webservice permettant la gestion des artisans.
 * 
 * @author Casaucau Cyril
 * 
 */
public class ArtisanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtisanService.class);

    private ArtisanService() {

    }

    /**
     * Crée un nouvel artisan / partenaire, son entreprise, etc
     * 
     * @param nouveauPartenaire
     *            DTO contenant toutes les informations données par
     *            l'utilisateur
     * @return Voir la classe {@link Constant} pour les retours possibles du
     *         service
     */
    public static Integer creationNouveauPartenaire(CreationPartenaireDTO nouveauPartenaire) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début appel service nouveau partenaire + deserialization");
        }

        String objectInJSON = WsConnector.getInstance().sendRequest(WsPath.GESTION_PARTENAIRE_SERVICE_PATH,
                WsPath.GESTION_PARTENAIRE_SERVICE_CREATION_PARTENAIRE, nouveauPartenaire);

        Integer codeRetour = Integer.valueOf(objectInJSON);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fin appel service nouveau partenaire + deserialization");
        }

        return codeRetour;
    }

}