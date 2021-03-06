package fr.castor.web.client.extend.nouveau.artisan;

import fr.castor.core.security.HashHelper;
import fr.castor.dto.PermissionDTO;
import fr.castor.dto.aggregate.CreationPartenaireDTO;
import fr.castor.dto.constant.ValidatorConstant;
import fr.castor.dto.enums.Civilite;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.web.app.enums.Etape;
import fr.castor.web.app.utils.ProgrammaticBeanLookup;
import fr.castor.web.client.behaviour.ErrorHighlightBehavior;
import fr.castor.web.client.behaviour.border.RequiredBorderBehaviour;
import fr.castor.web.client.event.FeedBackPanelEvent;
import fr.castor.web.client.extend.nouveau.artisan.event.ChangementEtapeEventArtisan;
import fr.castor.web.client.extend.nouveau.devis.NouveauUtils;
import fr.castor.web.client.validator.EmailUniquenessValidator;
import fr.castor.web.client.validator.LoginUniquenessValidator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

import java.util.Arrays;

/**
 * Etape 2 de l'inscription d'un nouvel artisan : Informations du dirigeant
 * 
 * @author Casaucau Cyril
 * 
 */
public class Etape2PartenaireForm extends Form<CreationPartenaireDTO> {

    private static final long serialVersionUID = 8347005157184562826L;

    public Etape2PartenaireForm(String id, IModel<CreationPartenaireDTO> model) {
        super(id, model);

        final CreationPartenaireDTO nouveauPartenaire = model.getObject();

        this.setMarkupId("formPartenaireEtape2");

        DropDownChoice<Civilite> civilite = new DropDownChoice<Civilite>("artisan.civilite", Arrays.asList(Civilite
                .values()));
        civilite.setRequired(true);
        civilite.setMarkupId("civilite");
        civilite.add(new ErrorHighlightBehavior());
        civilite.add(new RequiredBorderBehaviour());

        TextField<String> nom = new TextField<String>("artisan.nom");
        nom.setRequired(true);
        nom.setMarkupId("nom");
        nom.add(StringValidator.lengthBetween(ValidatorConstant.CLIENT_NOM_MIN, ValidatorConstant.CLIENT_NOM_MAX));
        nom.add(new ErrorHighlightBehavior());
        nom.add(new RequiredBorderBehaviour());

        TextField<String> prenom = new TextField<String>("artisan.prenom");
        prenom.setRequired(true);
        prenom.setMarkupId("prenomField");
        prenom.add(StringValidator.lengthBetween(ValidatorConstant.CLIENT_PRENOM_MIN,
                ValidatorConstant.CLIENT_PRENOM_MAX));
        prenom.add(new ErrorHighlightBehavior());
        prenom.add(new RequiredBorderBehaviour());

        TextField<String> numeroTel = new TextField<String>("artisan.numeroTel");
        numeroTel.setRequired(true);
        numeroTel.setMarkupId("numeroTelField");
        numeroTel.add(new PatternValidator(ValidatorConstant.TELEPHONE_REGEX));
        numeroTel.add(new ErrorHighlightBehavior());
        numeroTel.add(new RequiredBorderBehaviour());

        TextField<String> email = new TextField<String>("artisan.email");
        email.setRequired(true);
        email.setMarkupId("emailField");
        email.add(EmailAddressValidator.getInstance());

        EmailUniquenessValidator emailUniquenessValidator = (EmailUniquenessValidator) ProgrammaticBeanLookup
                .lookup("emailUniquenessValidator");

        email.add(emailUniquenessValidator);
        email.add(new ErrorHighlightBehavior());
        email.add(new RequiredBorderBehaviour());

        TextField<String> identifiant = new TextField<String>("artisan.login");
        identifiant.setRequired(true);
        identifiant.setMarkupId("logintField");
        identifiant.add(StringValidator.lengthBetween(ValidatorConstant.CLIENT_LOGIN_RANGE_MIN,
                ValidatorConstant.CLIENT_LOGIN_RANGE_MAX));
        identifiant.add(new ErrorHighlightBehavior());
        identifiant.add(new RequiredBorderBehaviour());

        LoginUniquenessValidator loginUniquenessValidator = (LoginUniquenessValidator) ProgrammaticBeanLookup
                .lookup("loginUniquenessValidator");

        identifiant.add(loginUniquenessValidator);

        PasswordTextField passwordField = new PasswordTextField("artisan.password");
        passwordField.setMarkupId("password");
        passwordField.setRequired(true);
        passwordField.add(new RequiredBorderBehaviour());
        passwordField.add(new ErrorHighlightBehavior());
        passwordField.add(StringValidator.lengthBetween(ValidatorConstant.PASSWORD_RANGE_MIN,
                ValidatorConstant.PASSWORD_RANGE_MAX));

        PasswordTextField confirmPassword = new PasswordTextField("confirmPassword", new Model<String>());
        confirmPassword.setMarkupId("confirmPassword");
        confirmPassword.setRequired(true);
        confirmPassword.add(new RequiredBorderBehaviour());
        confirmPassword.add(new ErrorHighlightBehavior());
        confirmPassword.add(StringValidator.lengthBetween(ValidatorConstant.PASSWORD_RANGE_MIN,
                ValidatorConstant.PASSWORD_RANGE_MAX));

        this.add(new EqualPasswordInputValidator(passwordField, confirmPassword));

        AjaxLink<Void> etapePrecedenteNouveauArtisan2 = new AjaxLink<Void>("etapePrecedenteNouveauArtisan2") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                NouveauUtils.sendEventForPreviousStep(target, Etape.ETAPE_2.ordinal() + 1);
            }
        };

        etapePrecedenteNouveauArtisan2.setOutputMarkupId(true);
        etapePrecedenteNouveauArtisan2.setMarkupId("etapePrecedenteNouveauArtisan2");

        AjaxSubmitLink validateEtape2Partenaire = new AjaxSubmitLink("validateEtape2Partenaire") {
            private static final long serialVersionUID = -171673358382084307L;

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink#onError
             * (org.apache.wicket.ajax.AjaxRequestTarget,
             * org.apache.wicket.markup.html.form.Form)
             */
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(getForm());
                this.send(target.getPage(), Broadcast.BREADTH, new FeedBackPanelEvent(target));
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink#onSubmit
             * (org.apache.wicket.ajax.AjaxRequestTarget,
             * org.apache.wicket.markup.html.form.Form)
             */
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                nouveauPartenaire.setNumeroEtape(3);
                ChangementEtapeEventArtisan changementEtapeEvent = new ChangementEtapeEventArtisan(target,
                        nouveauPartenaire);
                String hashedPassword = HashHelper.hashScrypt(nouveauPartenaire.getArtisan().getPassword());
                nouveauPartenaire.getArtisan().setPassword(hashedPassword);
                PermissionDTO permissionDTO = new PermissionDTO();
                permissionDTO.setTypeCompte(TypeCompte.ARTISAN);
                nouveauPartenaire.getArtisan().getPermissions().add(permissionDTO);
                this.send(target.getPage(), Broadcast.BREADTH, changementEtapeEvent);

            }

        };
        validateEtape2Partenaire.setMarkupId("validateEtape2Partenaire");

        setDefaultButton(validateEtape2Partenaire);
        this.add(civilite, nom, prenom, numeroTel, email, identifiant, passwordField, confirmPassword,
                validateEtape2Partenaire, etapePrecedenteNouveauArtisan2);

    }
}