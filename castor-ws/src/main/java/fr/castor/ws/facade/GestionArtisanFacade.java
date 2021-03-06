package fr.castor.ws.facade;

import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import fr.castor.core.constant.CodeRetourService;
import fr.castor.core.constant.Constant;
import fr.castor.core.constant.WsPath;
import fr.castor.core.enums.PropertiesFileGeneral;
import fr.castor.core.exception.BackendException;
import fr.castor.core.exception.EmailException;
import fr.castor.dto.AvisDTO;
import fr.castor.dto.CategorieMetierDTO;
import fr.castor.dto.EntrepriseDTO;
import fr.castor.dto.ImageDTO;
import fr.castor.dto.aggregate.AjoutPhotoDTO;
import fr.castor.dto.aggregate.CreationPartenaireDTO;
import fr.castor.dto.aggregate.SuppressionPhotoDTO;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.dto.helper.DeserializeJsonHelper;
import fr.castor.ws.dao.*;
import fr.castor.ws.entity.*;
import fr.castor.ws.enums.PropertiesFileWS;
import fr.castor.ws.helper.JsonHelper;
import fr.castor.ws.interceptor.BatimenInterceptor;
import fr.castor.ws.service.*;
import fr.castor.ws.utils.FluxUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
@Consumes(MediaType.APPLICATION_JSON)
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

    @Inject
    private EntrepriseService entrepriseService;

    @Inject
    private NotationService notationService;

    @Inject
    private PhotoService photoService;

    @Inject
    private GestionUtilisateurFacade gestionUtilisateurFacade;

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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Paramétres en entrée de la creation d'artisan : {}", nouveauPartenaireDTO);
        }


        ModelMapper mapper = new ModelMapper();

        Artisan artisanExiste = artisanService.checkArtisanExiste(nouveauPartenaireDTO.getArtisan().getEmail());

        if (artisanExiste != null) {
            LOGGER.error("Artisan déja existant en BDD : {}", nouveauPartenaireDTO);
            return CodeRetourService.RETOUR_KO;
        }

        Artisan nouveauArtisan = artisanService.constructionNouveauArtisan(nouveauPartenaireDTO.getArtisan(), mapper);

        Entreprise entrepriseExiste = artisanService.checkEntrepriseExiste(nouveauPartenaireDTO.getEntreprise()
                .getSiret());

        if (entrepriseExiste != null) {
            LOGGER.error("Entreprise existante dans le base de données");
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
        nouvelleEntreprise.setIsVerifier(false);

        nouveauArtisan.setEntreprise(nouvelleEntreprise);
        artisanDAO.saveArtisan(nouveauArtisan);
        permissionDAO.creationPermission(permission);

        entrepriseDAO.saveEntreprise(nouvelleEntreprise);

        for (CategorieMetierDTO categorieDTO : categories) {
            CategorieMetier nouvelleCategorieMetier = new CategorieMetier();
            nouvelleCategorieMetier.setCategorieMetier(categorieDTO.getCategorieMetier());
            mapper.map(categorieDTO, nouvelleCategorieMetier);
            nouvelleCategorieMetier.setEntreprise(nouvelleEntreprise);
            categorieMetierDAO.persistCategorieMetier(nouvelleCategorieMetier);
        }

        // On recupere l'url du frontend
        Properties urlProperties = PropertiesFileGeneral.URL.getProperties();
        String urlFrontend = urlProperties.getProperty("url.frontend.web");
        Properties emailProperties = PropertiesFileWS.EMAIL.getProperties();
        Boolean emailInscriptionConfirmationByCastorTeam = Boolean.valueOf(emailProperties.getProperty("email.confirmation.by.team"));

        try {
            if (emailInscriptionConfirmationByCastorTeam) {
                String mailAdresseCastorTeam = emailProperties.getProperty("email.box.team");
                emailService.envoiMailActivationCompte(nouveauPartenaireDTO.getArtisan().getNom(), nouveauPartenaireDTO
                        .getArtisan().getPrenom(), nouveauPartenaireDTO.getArtisan().getLogin(), mailAdresseCastorTeam
                        , nouveauArtisan.getCleActivation(), urlFrontend);
            } else {
                emailService.envoiMailActivationCompte(nouveauPartenaireDTO.getArtisan().getNom(), nouveauPartenaireDTO
                        .getArtisan().getPrenom(), nouveauPartenaireDTO.getArtisan().getLogin(), nouveauPartenaireDTO
                        .getArtisan().getEmail(), nouveauArtisan.getCleActivation(), urlFrontend);
            }

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
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_GET_ENTREPISE_INFORMATION_BY_LOGIN)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public EntrepriseDTO getEntrepriseInformationByArtisanLogin(String login) {
        Entreprise entreprise = entrepriseDAO.getEntrepriseByArtisan(DeserializeJsonHelper.parseString(login));
        return entrepriseService.rempliEntrepriseInformation(entreprise);
    }

    /**
     * Permet de récuperer les informations d'une entreprise (Infos + adresse)
     *
     * @param entrepriseDTO Les informations de l'entreprise que l'on doit modifier
     * @return CodeRetourService
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_SAVE_ENTREPRISE_INFORMATION)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Integer saveEntrepriseInformation(EntrepriseDTO entrepriseDTO) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(entrepriseDTO.toString());
        }

        Entreprise entrepriseAMettreAJour = entrepriseDAO.getArtisanByNomCompletSatutSiretDepartement(entrepriseDTO.getNomComplet()
                , entrepriseDTO.getStatutJuridique(), entrepriseDTO.getSiret()
                , entrepriseDTO.getAdresseEntreprise().getDepartement());

        if (entrepriseAMettreAJour != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("L'entreprise a été trouvée, on effectue les modifs demandé par l'utilisateur.");
            }
            entrepriseAMettreAJour.setNbEmployees(entrepriseDTO.getNbEmployees());
            entrepriseAMettreAJour.setDateCreation(entrepriseDTO.getDateCreation());
            entrepriseAMettreAJour.setSpecialite(entrepriseDTO.getSpecialite());
            ModelMapper mapper = new ModelMapper();
            mapper.map(entrepriseDTO.getAdresseEntreprise(), entrepriseAMettreAJour.getAdresse());

            categorieMetierDAO.updateCategorieMetier(entrepriseDTO, entrepriseAMettreAJour);
            entrepriseDAO.update(entrepriseAMettreAJour);
            adresseDAO.update(entrepriseAMettreAJour.getAdresse());
            return CodeRetourService.RETOUR_OK;
        } else {
            return CodeRetourService.RETOUR_KO;
        }
    }

    /**
     * Permet de récuperer les informations d'une entreprise (Infos + adresse)
     *
     * @param siret Le siret de l'entreprise
     * @return Les informations de l'entreprise.
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_GET_ENTREPISE_INFORMATION_BY_SIRET)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public EntrepriseDTO getEntrepriseInformationBySiret(String siret) {
        String siretEscaped = DeserializeJsonHelper.parseString(siret);
        Entreprise entreprise = entrepriseDAO.getEntrepriseBySiret(DeserializeJsonHelper.parseString(siret));
        EntrepriseDTO entrepriseDTO = entrepriseService.rempliEntrepriseInformation(entreprise);
        entrepriseDTO.getNotationsDTO().addAll(notationService.getNotationBySiret(siretEscaped, 2));

        //Stats
        Double moyenneAvis = 0.0;
        int nbAvis = 0;
        for (AvisDTO notationDTO : entrepriseDTO.getNotationsDTO()) {
            moyenneAvis += notationDTO.getScore();
            nbAvis++;
        }

        moyenneAvis = moyenneAvis / nbAvis;

        entrepriseDTO.setMoyenneAvis(moyenneAvis);
        entrepriseDTO.setNbAnnonce(entreprise.getAnnonceEntrepriseSelectionnee().size());

        return entrepriseDTO;
    }

    /**
     * Permet de récuperer tous les avis d'une entreprise par son SIRETs
     *
     * @param siret Le siret de l'entreprise
     * @return Les informations de l'entreprise.
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_GET_NOTATION_BY_SIRET)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<AvisDTO> getEntrepriseNotationBySiret(String siret) {
        String siretEscaped = DeserializeJsonHelper.parseString(siret);
        return notationService.getNotationBySiret(siretEscaped, 0);
    }

    /**
     * Service qui permet à un artisan de pouvoir ajouter / rajouter des photos de chantier témoin<br/>
     * <p>
     * Mode multipart, en plus du JSON la request contient des photos.
     *
     * @param formInputRaw L'objet provenant du frontend qui permet l'ajout de photo
     * @return La liste des images appartenant à l'utilisateur contenu dans cloudinary.
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_AJOUT_PHOTO_CHANTIER_TEMOIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<ImageDTO> ajouterPhotoChantierTemoin(MultipartFormDataInput formInputRaw) {

        Map<String, List<InputPart>> formDataAnnonceRaw = formInputRaw.getFormDataMap();

        List<InputPart> contents = formDataAnnonceRaw.getOrDefault("content", new ArrayList<>());
        List<InputPart> files = formDataAnnonceRaw.getOrDefault("files", new ArrayList<>());

        AjoutPhotoDTO ajoutPhotoDTO = null;
        //Transformation de la partie JSON (Données de l'annonce).
        for (InputPart content : contents) {
            try {
                ajoutPhotoDTO = DeserializeJsonHelper.deserializeDTO(
                        FluxUtils.getJsonByInputStream(content.getBody(InputStream.class, null)), AjoutPhotoDTO.class);
            } catch (IOException e) {
                LOGGER.error("Erreur pendant la récuperation de l'input stream en JSON contenant les données de l'annonce", e);
            }
        }

        String rolesDemandeur = gestionUtilisateurFacade.getUtilisateurRoles(ajoutPhotoDTO.getLoginDemandeur());
        Entreprise entrepriseAjoutPhotos = artisanService.loadEntrepriseAndCheckRole(rolesDemandeur, ajoutPhotoDTO.getId(), ajoutPhotoDTO.getLoginDemandeur());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Chargement de l'entreprise en cours, grace à la DTO en entrée : {}", ajoutPhotoDTO.toString());
        }

        if (entrepriseAjoutPhotos == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Impossible de trouver l'entreprise avec le compte demandé, Détails : {}", ajoutPhotoDTO.toString());
            }
            return new ArrayList<>();
        }

        List<String> urlsPhoto = photoService.transformAndSendToCloud(files, entrepriseAjoutPhotos.getImagesChantierTemoin());

        if (!urlsPhoto.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persistance des urls des images");
            }
            photoService.persistPhoto(entrepriseAjoutPhotos, urlsPhoto);
        }
        return photoService.imageToImageDTO(entrepriseAjoutPhotos.getImagesChantierTemoin());
    }

    /**
     * Suppression d'une photo des chantiers temoin d'une entreprise.
     *
     * @param suppressionPhotoDTO L'hash id + le login du demandeur
     * @return La liste des objets images appartenant à l'annonce.
     */
    @POST
    @Path(WsPath.GESTION_PARTENAIRE_SERVICE_SUPPRESSION_PHOTO_CHANTIER_TEMOIN)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<ImageDTO> suppressionPhotoChantierTemoin(SuppressionPhotoDTO suppressionPhotoDTO) {
        String rolesDemandeur = gestionUtilisateurFacade.getUtilisateurRoles(suppressionPhotoDTO.getLoginDemandeur());
        List<Image> images = photoService.getImagesBySiretByLoginDemandeur(rolesDemandeur, suppressionPhotoDTO.getId(), suppressionPhotoDTO.getLoginDemandeur());

        return photoService.suppressionPhoto(images, suppressionPhotoDTO);
    }
}