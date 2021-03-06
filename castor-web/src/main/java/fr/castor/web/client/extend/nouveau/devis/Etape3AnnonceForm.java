package fr.castor.web.client.extend.nouveau.devis;

import fr.castor.dto.PermissionDTO;
import fr.castor.dto.aggregate.CreationAnnonceDTO;
import fr.castor.dto.constant.ValidatorConstant;
import fr.castor.dto.enums.DelaiIntervention;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.dto.enums.TypeContact;
import fr.castor.dto.enums.TypeTravaux;
import fr.castor.web.app.enums.Etape;
import fr.castor.web.app.enums.FeedbackMessageLevel;
import fr.castor.web.app.constants.ParamsConstant;
import fr.castor.web.client.behaviour.ErrorHighlightBehavior;
import fr.castor.web.client.behaviour.FileFieldValidatorAndLoaderBehaviour;
import fr.castor.web.client.behaviour.border.RequiredBorderBehaviour;
import fr.castor.web.client.component.MotCle;
import fr.castor.web.client.event.FeedBackPanelEvent;
import fr.castor.web.client.event.ModificationAnnonceEvent;
import fr.castor.web.client.extend.connected.Annonce;
import fr.castor.web.client.extend.nouveau.communs.JSCommun;
import fr.castor.web.client.extend.nouveau.devis.event.ChangementEtapeClientEvent;
import fr.castor.web.client.validator.TelephonePresentValidator;
import fr.castor.web.client.validator.VilleValidator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Form de l'etape 3 de création d'annonce.
 *
 * @author Casaucau Cyril
 */
public class Etape3AnnonceForm extends Form<CreationAnnonceDTO> {

    private static final long serialVersionUID = 6521295805432818556L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Etape3AnnonceForm.class);

    private final CreationAnnonceDTO nouvelleAnnonce;

    private FileFieldValidatorAndLoaderBehaviour fileFieldValidatorBehaviour;
    private String idAnnonce;
    //private String initVilleTypeAhead;

    @Inject
    private TelephonePresentValidator telephonePresentValidator;

    //@Inject
    //private VilleValidator villeValidator;

    private MotCle motCleComposant;

    private boolean forModification = false;

    /**
     * Constructeur utile pour la modification d'une annonce.
     * <p>
     * Particularité : on charge le composant de selection des sous categories differement car à la creation de l'annonce celui ci est rempli grace a un event ajax que nous n'avons pas ici.
     *
     * @param id    l'id wicket du composant sur la page
     * @param model Le model permettant l'affichage des données;
     */
    public Etape3AnnonceForm(final String id, IModel<CreationAnnonceDTO> model, boolean forModification) {
        this(id, model);
        this.forModification = forModification;
    }

    /**
     * Constructeur utilisé lors de la création d'annonce.
     *
     * @param id    l'id wicket du composant sur la page
     * @param model Le model permettant l'affichage des données;
     */
    public Etape3AnnonceForm(final String id, IModel<CreationAnnonceDTO> model) {
        super(id, model);
        // Mode Multipart pour l'upload de fichier.
        setMultiPart(true);
        setFileMaxSize(Bytes.megabytes(15));
        setMaxSize(Bytes.megabytes(30));

        setMarkupId("formEtape3");

        nouvelleAnnonce = model.getObject();

        TextArea<String> descriptionDevisField = new TextArea<String>("description");
        descriptionDevisField.setRequired(true);
        descriptionDevisField.setOutputMarkupId(true);
        descriptionDevisField.add(StringValidator.lengthBetween(ValidatorConstant.ANNONCE_DESCRIPTION_MIN,
                ValidatorConstant.ANNONCE_DESCRIPTION_MAX));
        descriptionDevisField.add(new ErrorHighlightBehavior());
        descriptionDevisField.add(new RequiredBorderBehaviour());

        DropDownChoice<TypeContact> typeContactField = new DropDownChoice<TypeContact>("typeContact",
                Arrays.asList(TypeContact.values()));
        typeContactField.setRequired(true);
        typeContactField.setMarkupId("typeContactField");
        typeContactField.add(new ErrorHighlightBehavior());
        typeContactField.add(new RequiredBorderBehaviour());
        typeContactField.add(telephonePresentValidator);
        typeContactField.setOutputMarkupId(true);

        DropDownChoice<DelaiIntervention> delaiInterventionField = new DropDownChoice<DelaiIntervention>(
                "delaiIntervention", Arrays.asList(DelaiIntervention.values()));
        delaiInterventionField.setRequired(true);
        delaiInterventionField.setMarkupId("delaiInterventionField");
        delaiInterventionField.add(new ErrorHighlightBehavior());
        delaiInterventionField.add(new RequiredBorderBehaviour());
        delaiInterventionField.setOutputMarkupId(true);

        RadioGroup<TypeTravaux> typeTravaux = new RadioGroup<>("typeTravaux");
        Radio<TypeTravaux> neuf = new Radio<>("typeTravaux.neuf", new Model<TypeTravaux>(TypeTravaux.NEUF));
        Radio<TypeTravaux> renovation = new Radio<>("typeTravaux.renovation", new Model<TypeTravaux>(
                TypeTravaux.RENOVATION));
        typeTravaux.add(neuf);
        typeTravaux.add(renovation);
        typeTravaux.setRequired(true);
        typeTravaux.add(new RequiredBorderBehaviour());
        typeTravaux.setMarkupId("typeTravaux");

        Label lblPhoto = new Label("lblPhoto", new Model<String>());

        if (forModification) {
            lblPhoto.setDefaultModelObject("Ajouter des photos à votre devis: ");
        } else {
            lblPhoto.setDefaultModelObject("Souhaitez-vous ajouter des photos à votre devis ?");
        }

        final FileUploadField photoField = new FileUploadField("photos");
        photoField.setMarkupId("photoField");
        fileFieldValidatorBehaviour = new FileFieldValidatorAndLoaderBehaviour();
        photoField.add(fileFieldValidatorBehaviour);

        WebMarkupContainer containerPhoto = new WebMarkupContainer("containerPhoto") {
            @Override
            public boolean isVisible() {
                return !forModification;
            }
        };

        containerPhoto.add(lblPhoto, photoField);

        TextField<String> adresseField = new TextField<String>("adresse");
        adresseField.setRequired(false);
        adresseField.setMarkupId("adresseField");
        adresseField.add(StringValidator.lengthBetween(ValidatorConstant.ADRESSE_MIN, ValidatorConstant.ADRESSE_MAX));
        adresseField.add(new ErrorHighlightBehavior());

        TextField<String> adresseComplementField = new TextField<String>("complementAdresse");
        adresseComplementField.setMarkupId("adresseComplementField");
        adresseComplementField.add(StringValidator.maximumLength(ValidatorConstant.COMPLEMENT_ADRESSE_MAX));
        adresseComplementField.add(new ErrorHighlightBehavior());

        TextField<String> codePostalField = new TextField<String>("codePostal");
        codePostalField.setRequired(true);
        codePostalField.setMarkupId("codePostalField");
        codePostalField.add(new PatternValidator(ValidatorConstant.CODE_POSTAL_REGEX));
        codePostalField.add(new ErrorHighlightBehavior());
        codePostalField.add(new RequiredBorderBehaviour());

        TextField<String> villeField = new TextField<String>("ville");
        villeField.setRequired(true);
        villeField.setMarkupId("villeField");
        villeField.add(StringValidator.maximumLength(ValidatorConstant.VILLE_MAX));
        villeField.add(new ErrorHighlightBehavior());
        villeField.add(new RequiredBorderBehaviour());
       // villeValidator.setCodepostalField(codePostalField);
        //villeField.add(villeValidator);

        WebMarkupContainer containerField = new WebMarkupContainer("containerField");
        containerField.setOutputMarkupId(true);

        AjaxSubmitLink validateQualification = new AjaxSubmitLink("validateQualification") {

            private static final long serialVersionUID = -4417031301033032959L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (forModification) {
                    ModificationAnnonceEvent modificationAnnonceEvent = new ModificationAnnonceEvent(target, nouvelleAnnonce);
                    this.send(target.getPage(), Broadcast.BREADTH, modificationAnnonceEvent);
                } else {
                    nouvelleAnnonce.setPhotos(new ArrayList<File>());
                    //On enregistre les fichiers dans la DTO
                    for (FileUpload photo : photoField.getFileUploads()) {
                        try {
                            nouvelleAnnonce.getPhotos().add(photo.writeToTempFile());
                        } catch (IOException e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("Problème durant l'écriture de la photo sur le disque", e);
                            }
                        }
                    }
                    if (!fileFieldValidatorBehaviour.isValidationOK()) {
                        target.getPage().send(target.getPage(), Broadcast.BREADTH, new FeedBackPanelEvent(target, "Validation erronnée de vos photos, veuillez corriger", FeedbackMessageLevel.ERROR));
                    } else {
                        nouvelleAnnonce.setNumeroEtape(4);
                        ChangementEtapeClientEvent changementEtapeEventClient = new ChangementEtapeClientEvent(target,
                                nouvelleAnnonce);
                        PermissionDTO permissionDTO = new PermissionDTO();
                        permissionDTO.setTypeCompte(TypeCompte.CLIENT);
                        nouvelleAnnonce.getClient().getPermissions().add(permissionDTO);
                        this.send(target.getPage(), Broadcast.BREADTH, changementEtapeEventClient);
                    }
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                send(target.getPage(), Broadcast.BREADTH, new FeedBackPanelEvent(target));
                target.add(containerField);
            }

        };
        validateQualification.setMarkupId("validateQualification");

        AjaxLink<Void> etapePrecedente3 = new AjaxLink<Void>("etapePrecedente3") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (forModification) {
                    PageParameters params = new PageParameters();
                    params.add(ParamsConstant.ID_ANNONCE_PARAM, idAnnonce);
                    this.setResponsePage(Annonce.class, params);
                } else {
                    NouveauUtils.sendEventForPreviousStep(target, Etape.ETAPE_3.ordinal() + 1);
                }
            }
        };

        etapePrecedente3.setOutputMarkupId(true);
        etapePrecedente3.setMarkupId("etapePrecedente3");

        this.setDefaultButton(validateQualification);

        containerField.add(descriptionDevisField, typeContactField, delaiInterventionField,
                adresseField, adresseComplementField, codePostalField, villeField, validateQualification, typeTravaux,
                etapePrecedente3, containerPhoto);

        add(containerField);
    }

    /*@Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        initVilleTypeAhead = JSCommun.buildSourceTypeAhead(nouvelleAnnonce.getVillesPossbles(), "#villeField");
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        initVilleTypeAhead = JSCommun.buildSourceTypeAhead(nouvelleAnnonce.getVillesPossbles(), "#villeField");
        response.render(OnDomReadyHeaderItem.forScript(initVilleTypeAhead));
    }*/

    public void setIdAnnonce(String idAnnonce) {
        this.idAnnonce = idAnnonce;
    }
}