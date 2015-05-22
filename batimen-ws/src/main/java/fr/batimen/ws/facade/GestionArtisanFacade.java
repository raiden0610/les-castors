package fr.batimen.ws.facade;

import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import fr.batimen.core.constant.CodeRetourService;
import fr.batimen.core.constant.Constant;
import fr.batimen.core.constant.WsPath;
import fr.batimen.core.exception.BackendException;
import fr.batimen.core.exception.EmailException;
import fr.batimen.dto.AdresseDTO;
import fr.batimen.dto.CategorieMetierDTO;
import fr.batimen.dto.EntrepriseDTO;
import fr.batimen.dto.aggregate.CreationPartenaireDTO;
import fr.batimen.dto.enums.TypeCompte;
import fr.batimen.dto.helper.CategorieLoader;
import fr.batimen.dto.helper.DeserializeJsonHelper;
import fr.batimen.ws.dao.*;
import fr.batimen.ws.entity.*;
import fr.batimen.ws.enums.PropertiesFileWS;
import fr.batimen.ws.helper.JsonHelper;
import fr.batimen.ws.interceptor.BatimenInterceptor;
import fr.batimen.ws.service.ArtisanService;
import fr.batimen.ws.service.EmailService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Facade REST de gestion des artisans
 *
 * @author Casaucau Cyril
 */
@Stateless(name = "GestionArtisanFacade")
@LocalBean
@Path(WsPath.GESTION_PARTENAIRE_SERVICE_PATH)
@RolesAllowed(Constant.USERS_ROLE)
@Produces(JsonHelper.JSON_MEDIA_TYPE_AND_UTF_8_CHARSET)
@Consumes(JsonHelper.JSON_MEDIA_TYPE_AND_UTF_8_CHARSET)
@Interceptors(value = {BatimenInterceptor.class})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class GestionArtisanFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionArtisanFacade.class);

    @Inject
    private ArtisanDAO artisanDAO;

    @Inject
    private EntrepriseDAO entrepriseDAO;

    @Inject
    private AdresseDAO adresseDAO;

    @Inject
    private CategorieMetierDAO categorieMetierDAO;

    @Inject
    private PermissionDAO permissionDAO;

    @Inject
    private EmailService emailService;

    @Inject
    private ArtisanService artisanService;

    /**
     * Service de création d'un nouveau partenaire Artisan
     *
     * @param nouveauPartenaireDTO l'objet contenant l'ensemble des informations.
     * @return voir la classe : {@link Constant}
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_CREATION_PARTENAIRE)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Integer creationArtisan(CreationPartenaireDTO nouveauPartenaireDTO) {
        ModelMapper mapper = new ModelMapper();

        Artisan artisanExiste = artisanService.checkArtisanExiste(nouveauPartenaireDTO.getArtisan().getEmail());

        if (artisanExiste != null) {
            return CodeRetourService.RETOUR_KO;
        }

        Artisan nouveauArtisan = artisanService.constructionNouveauArtisan(nouveauPartenaireDTO.getArtisan(), mapper);

        Entreprise entrepriseExiste = artisanService.checkEntrepriseExiste(nouveauPartenaireDTO.getEntreprise()
                .getSiret());

        if (entrepriseExiste != null) {
            return CodeRetourService.RETOUR_KO;
        }

        // On init l'entité et on la rempli avec les champs de la DTO
        Entreprise nouvelleEntreprise = new Entreprise();
        Adresse nouvelleAdresse = new Adresse();
        // Remplissage automatique des champs commun
        mapper.map(nouveauPartenaireDTO.getEntreprise(), nouvelleEntreprise);
        mapper.map(nouveauPartenaireDTO.getAdresse(), nouvelleAdresse);

        // On set les permissions
        Permission permission = new Permission();
        permission.setTypeCompte(TypeCompte.ARTISAN);
        permission.setArtisan(nouveauArtisan);
        nouveauArtisan.getPermission().add(permission);

        List<CategorieMetierDTO> categories = nouveauPartenaireDTO.getEntreprise().getCategoriesMetier();

        try {
            adresseDAO.saveAdresse(nouvelleAdresse);
        } catch (BackendException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("L'adresse existe déjà dans la BDD ", e);
            }
            return CodeRetourService.RETOUR_KO;
        }

        nouvelleEntreprise.setAdresse(nouvelleAdresse);

        nouveauArtisan.setEntreprise(nouvelleEntreprise);
        artisanDAO.saveArtisan(nouveauArtisan);
        permissionDAO.creationPermission(permission);

        entrepriseDAO.saveEntreprise(nouvelleEntreprise);

        for (CategorieMetierDTO categorieDTO : categories) {
            CategorieMetier nouvelleCategorieMetier = new CategorieMetier();
            nouvelleCategorieMetier.setCategorieMetier(categorieDTO.getCodeCategorieMetier());
            mapper.map(categorieDTO, nouvelleCategorieMetier);
            nouvelleCategorieMetier.setEntreprise(nouvelleEntreprise);
            categorieMetierDAO.persistCategorieMetier(nouvelleCategorieMetier);
        }

        // On recupere l'url du frontend
        Properties urlProperties = PropertiesFileWS.URL.getProperties();
        String urlFrontend = urlProperties.getProperty("url.frontend.web");
        try {
            emailService.envoiMailActivationCompte(nouveauPartenaireDTO.getArtisan().getNom(), nouveauPartenaireDTO
                    .getArtisan().getPrenom(), nouveauPartenaireDTO.getArtisan().getLogin(), nouveauPartenaireDTO
                    .getArtisan().getEmail(), nouveauArtisan.getCleActivation(), urlFrontend);
        } catch (EmailException | MandrillApiError | IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Problème d'envoi de mail de confirmation pour un artisan", e);
            }
        }

        return CodeRetourService.RETOUR_OK;
    }

    /**
     * Permet de récuperer les informations d'une entreprise (Infos + adresse)
     *
     * @param login Le login de l'artisan dont on veut l'entreprise
     * @return Les informations de l'entreprise.
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_GET_ENTREPISE_INFORMATION)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public EntrepriseDTO getEntrepriseInformationByArtisanLogin(String login) {
        Entreprise entreprise = entrepriseDAO.getEntrepriseByArtisan(DeserializeJsonHelper.parseString(login));

        if(entreprise.getId() != null){
            ModelMapper mapper = new ModelMapper();
            EntrepriseDTO entrepriseDTO = mapper.map(entreprise, EntrepriseDTO.class);

            for(CategorieMetier categorieMetier : entreprise.getCategoriesMetier()){
                entrepriseDTO.getCategoriesMetier().add(CategorieLoader.getCategorieByCode(categorieMetier.getCategorieMetier()));
            }

            AdresseDTO adresseEntreprise = mapper.map(entreprise.getAdresse(), AdresseDTO.class);
            entrepriseDTO.setAdresseEntreprise(adresseEntreprise);
            return entrepriseDTO;
        }else{
            return new EntrepriseDTO();
        }



    }

}