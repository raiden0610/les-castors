package fr.castor.web.client.extend.member.client;

import fr.castor.core.constant.CodeRetourService;
import fr.castor.dto.AdresseDTO;
import fr.castor.dto.AnnonceDTO;
import fr.castor.dto.aggregate.AnnonceAffichageDTO;
import fr.castor.dto.aggregate.CreationAnnonceDTO;
import fr.castor.dto.aggregate.ModificationAnnonceDTO;
import fr.castor.dto.enums.EtatAnnonce;
import fr.castor.web.app.constants.ParamsConstant;
import fr.castor.web.app.enums.FeedbackMessageLevel;
import fr.castor.web.app.security.Authentication;
import fr.castor.web.client.component.*;
import fr.castor.web.client.event.AjoutPhotoEvent;
import fr.castor.web.client.event.ModificationAnnonceEvent;
import fr.castor.web.client.event.SuppressionPhotoEvent;
import fr.castor.web.client.extend.connected.Annonce;
import fr.castor.web.client.extend.member.client.util.PhotoServiceAjaxLogic;
import fr.castor.web.client.extend.nouveau.devis.Etape3AnnonceForm;
import fr.castor.web.client.master.MasterPage;
import fr.castor.ws.client.service.AnnonceServiceREST;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;

/**
 * Page permettant aux clients ou aux admins de modifier une annonce postée par un client
 * <p>
 * Created by Casaucau on 17/04/2015.
 * <p>
 *
 * @author Casaucau Cyril
 */
public class ModifierAnnonce extends MasterPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifierAnnonce.class);

    @Inject
    private AnnonceServiceREST annonceServiceREST;

    @Inject
    private Authentication authentication;

    @Inject
    private PhotoServiceAjaxLogic photoServiceAjaxLogic;

    private Etape3AnnonceForm etape3AnnonceForm;

    private AnnonceAffichageDTO annonceAffichageDTO;
    private CompoundPropertyModel propertyModelModificationAnnonce;

    private PhotosContainer photosContainer;

    private String idAnnonce;

    private MotCle motCleComposant;

    public ModifierAnnonce() {
        this((AnnonceAffichageDTO) null);
    }

    public ModifierAnnonce(AnnonceAffichageDTO annonceAffichageDTO) {
        super("Modifier mon annonce", "lol", "Modifier mon annonce", true, "img/bg_title1.jpg");
        this.annonceAffichageDTO = annonceAffichageDTO;

        if (annonceAffichageDTO == null
                || !annonceAffichageDTO.getAnnonce().getEtatAnnonce().equals(EtatAnnonce.DESACTIVE)
                || !annonceAffichageDTO.getAnnonce().getEtatAnnonce().equals(EtatAnnonce.DONNER_AVIS)
                || !annonceAffichageDTO.getAnnonce().getEtatAnnonce().equals(EtatAnnonce.SUPPRIMER)
                || !annonceAffichageDTO.getAnnonce().getEtatAnnonce().equals(EtatAnnonce.TERMINER)) {
            this.setResponsePage(MesAnnonces.class);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init de la page de modification de mon profil");
        }

        initData();
        initComposant();
    }

    public ModifierAnnonce(String idAnnonce, AnnonceAffichageDTO annonceAffichageDTO) {
        this(annonceAffichageDTO);
        this.idAnnonce = idAnnonce;
        photosContainer.setIdAnnonce(idAnnonce);
        etape3AnnonceForm.setIdAnnonce(idAnnonce);
    }

    public ModifierAnnonce(PageParameters params) {
        this((AnnonceAffichageDTO) null);
    }


    private void initComposant() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init des composants de la page de modification de mon annonce");
        }
        Profil profil = new Profil("profil", false);

        Form<Void> formPrincipalModification = new Form<Void>("formPrincipalModification"){
            @Override
            public boolean isRootForm() {
                return true;
            }

            @Override
            public boolean wantSubmitOnNestedFormSubmit() {
                return true;
            }
        };

        etape3AnnonceForm = new Etape3AnnonceForm("formQualification", propertyModelModificationAnnonce, true);

        photosContainer = new PhotosContainer("afficheurPhotos", annonceAffichageDTO.getImages(), "Les photos de votre annonce", "h4", true);
        photosContainer.setOutputMarkupId(true);

        motCleComposant = new MotCle("motCle", ((CreationAnnonceDTO) propertyModelModificationAnnonce.getObject()).getMotCles());

        formPrincipalModification.add(motCleComposant, etape3AnnonceForm);

        ContactezNous contactezNous = new ContactezNous("contactezNous");
        Commentaire commentaire = new Commentaire("commentaire");

        add(profil, formPrincipalModification, photosContainer, contactezNous, commentaire);
    }

    private void initData() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init des datas de la page de modification de mon annonce");
        }

        CreationAnnonceDTO creationAnnonceDTO = new CreationAnnonceDTO();
        ModelMapper mapper = new ModelMapper();
        mapper.map(annonceAffichageDTO.getAdresse(), creationAnnonceDTO);
        mapper.map(annonceAffichageDTO.getAnnonce(), creationAnnonceDTO);

        propertyModelModificationAnnonce = new CompoundPropertyModel<>(creationAnnonceDTO);
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        if (event.getPayload() instanceof ModificationAnnonceEvent) {
            //Extraction des infos
            ModificationAnnonceEvent modificationAnnonceEvent = (ModificationAnnonceEvent) event.getPayload();
            motCleComposant.rechercheWithInputTextField();
            if (!motCleComposant.getCategoriesSelectionnees().isEmpty()) {
                CreationAnnonceDTO creationAnnonceDTO = modificationAnnonceEvent.getCreationAnnonceDTO();

                ModelMapper mapper = new ModelMapper();

                //Init des objets
                ModificationAnnonceDTO modificationAnnonceDTO = new ModificationAnnonceDTO();
                modificationAnnonceDTO.setAnnonce(new AnnonceDTO());
                modificationAnnonceDTO.setAdresse(new AdresseDTO());

                //Remplissage des DTOs
                mapper.map(creationAnnonceDTO, modificationAnnonceDTO.getAnnonce());
                mapper.map(creationAnnonceDTO, modificationAnnonceDTO.getAdresse());

                //Champs non mappés
                modificationAnnonceDTO.setLoginDemandeur(authentication.getCurrentUserInfo().getLogin());

                modificationAnnonceDTO.getAnnonce().setDateMAJ(new Date());
                modificationAnnonceDTO.getAnnonce().setEtatAnnonce(annonceAffichageDTO.getAnnonce().getEtatAnnonce());
                modificationAnnonceDTO.getAnnonce().setDateCreation(annonceAffichageDTO.getAnnonce().getDateCreation());
                modificationAnnonceDTO.getAnnonce().setHashID(idAnnonce);
                modificationAnnonceDTO.getAnnonce().setNbConsultation(annonceAffichageDTO.getAnnonce().getNbConsultation());
                modificationAnnonceDTO.getAnnonce().setNbDevis(annonceAffichageDTO.getAnnonce().getNbDevis());


                //Appel du service
                Integer codeRetourService = annonceServiceREST.modifierAnnonce(modificationAnnonceDTO);

                if (codeRetourService.equals(CodeRetourService.RETOUR_OK)) {
                    PageParameters params = new PageParameters();
                    params.add(ParamsConstant.ID_ANNONCE_PARAM, idAnnonce);
                    params.add(ParamsConstant.IS_MODIF_PARAM, "OK");
                    this.setResponsePage(Annonce.class, params);
                } else {
                    feedBackPanelGeneral.sendMessageAndGoToTop("Problème lors de l'enregistrement de l'annonce, veuillez réessayer ultérieurement", FeedbackMessageLevel.ERROR, modificationAnnonceEvent.getTarget());
                }
                modificationAnnonceEvent.getTarget().add(feedBackPanelGeneral);
            } else {
                feedBackPanelGeneral.sendMessageAndGoToTop(MotCle.ERROR_MESSAGE, FeedbackMessageLevel.ERROR, modificationAnnonceEvent.getTarget());
                modificationAnnonceEvent.getTarget().add(feedBackPanelGeneral);
            }
        }

        if (event.getPayload() instanceof SuppressionPhotoEvent) {
            photoServiceAjaxLogic.suppressionPhotoAnnonce(event);
            photosContainer.updatePhotoContainer(((SuppressionPhotoEvent) event.getPayload()).getTarget());
        }

        if (event.getPayload() instanceof AjoutPhotoEvent) {
            photoServiceAjaxLogic.ajoutPhotoAnnonce(event);
            photosContainer.updatePhotoContainer(((AjoutPhotoEvent) event.getPayload()).getTarget());
        }
    }
}
