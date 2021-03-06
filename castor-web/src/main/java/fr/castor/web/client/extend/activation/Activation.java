package fr.castor.web.client.extend.activation;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import fr.castor.core.constant.CodeRetourService;
import fr.castor.web.client.master.MasterPage;
import fr.castor.ws.client.service.UtilisateurServiceREST;

/**
 * Page de l'application qui sert à activer et à informer le client à propos de
 * l'activation de son compte.
 * 
 * @author Casaucau Cyril
 * 
 */
public class Activation extends MasterPage {

    private static final long serialVersionUID = 99813244363266423L;

    @Inject
    private UtilisateurServiceREST utilisateurServiceREST;

    private String cleActivation;
    private final Label messageActivationCompte;
    private final Label messageActivationCompte2;
    private final WebMarkupContainer confirmationImg;

    public Activation() {
        super("Activation d'un compte lescastors.fr", "activation compte lescastors.fr", "Activation de votre compte",
                false, "");
        this.setVersioned(false);
        messageActivationCompte = new Label("messageActivationCompte", new Model<String>());
        messageActivationCompte2 = new Label("messageActivationCompte2", new Model<String>());
        confirmationImg = new WebMarkupContainer("confirmationImg");

        this.add(messageActivationCompte);
        this.add(messageActivationCompte2);
    }

    public Activation(PageParameters params) {
        this();
        cleActivation = params.get("key").toString();
        if (!cleActivation.isEmpty()) {

            Integer codeRetourService = utilisateurServiceREST.activateAccount(cleActivation);

            if (codeRetourService.equals(CodeRetourService.RETOUR_OK)) {
                messageActivationCompte.setDefaultModelObject("Votre compte est activé !!");
                messageActivationCompte2
                        .setDefaultModelObject("Vous pouvez maintenant vous connecter à notre espace membre");
                confirmationImg.add(new AttributeModifier("class", "confirmationactivationok"));
            } else if (codeRetourService.equals(CodeRetourService.RETOUR_KO)) {
                messageActivationCompte.setDefaultModelObject("Problème d'accès au service.");
                messageActivationCompte2.setDefaultModelObject("Si le probleme persiste contactez nous.");
                confirmationImg.add(new AttributeModifier("class", "confirmationactivationko"));
            } else if (codeRetourService.equals(CodeRetourService.ANNONCE_RETOUR_DEJA_ACTIF)) {
                messageActivationCompte.setDefaultModelObject("Votre compte est déjà actif");
                messageActivationCompte2.setDefaultModelObject("");
                confirmationImg.add(new AttributeModifier("class", "confirmationactivationko"));
            } else if (codeRetourService.equals(CodeRetourService.ANNONCE_RETOUR_COMPTE_INEXISTANT)) {
                getMessageIncorrectKey();
            }
        } else {
            getMessageIncorrectKey();
        }

        this.addOrReplace(confirmationImg);

    }

    private void getMessageIncorrectKey() {
        messageActivationCompte.setDefaultModelObject("Clé d'activation incorrecte");
        messageActivationCompte2
                .setDefaultModelObject("Veuillez suivre le lien contenu dans le mail de confirmation d'inscription");
        confirmationImg.add(new AttributeModifier("class", "confirmationactivationko"));
    }
}
