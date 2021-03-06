package fr.castor.ws.service;

import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import fr.castor.core.constant.EmailConstant;
import fr.castor.core.constant.UrlPage;
import fr.castor.core.enums.PropertiesFileGeneral;
import fr.castor.core.exception.EmailException;
import fr.castor.dto.ContactMailDTO;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.ws.dao.EmailDAO;
import fr.castor.ws.entity.Annonce;
import fr.castor.ws.entity.Notification;
import fr.castor.ws.enums.PropertiesFileWS;
import fr.castor.ws.utils.ClientUtils;
import fr.castor.ws.utils.EmailUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * Classe de gestion d'envoi de mail.
 *
 * @author Casaucau Cyril
 * @author Adnane
 */
@Stateless(name = "EmailService")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmailService {

    @Inject
    private EmailDAO emailDAO;

    @Inject
    private EmailUtils emailUtils;

    /**
     * Preparation et envoi d'un mail de confirmation, dans le but d'informer
     * l'utilisateur que l'annonce a correctement été enregistrée.
     *
     * @param nouvelleAnnonce L'objet que l'on a recu du frontend
     * @return vrai si l'envoi s'est bien passé.
     * @throws EmailException
     * @throws MandrillApiError
     * @throws IOException
     */
    public boolean envoiMailConfirmationCreationAnnonce(Annonce nouvelleAnnonce) throws EmailException,
            MandrillApiError, IOException {

        // On prepare l'entete, on ne mets pas de titre.
        MandrillMessage confirmationAnnonceMessage = emailDAO.prepareEmail(null);

        StringBuilder nomDestinataire = new StringBuilder();
        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<String, String>();

        ClientUtils.chooseNomClient(nouvelleAnnonce.getDemandeur().getNom(), nouvelleAnnonce.getDemandeur().getPrenom(),
                nouvelleAnnonce.getDemandeur().getLogin(), nomDestinataire);
        recipients.put(nomDestinataire.toString(), nouvelleAnnonce.getDemandeur().getEmail());

        String motclesForMail = emailUtils.transformMotsClesToCSV(nouvelleAnnonce.getMotcles());

        String categorieForMail = emailUtils.transformCategorieToCSV(nouvelleAnnonce.getMotcles());

        // On charge le contenu
        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(EmailConstant.TAG_EMAIL_USERNAME, nouvelleAnnonce.getDemandeur().getLogin());
        templateContent.put(EmailConstant.TAG_EMAIL_MOT_CLE, motclesForMail);
        templateContent.put(EmailConstant.TAG_EMAIL_CATEGORIE_METIER, categorieForMail);
        templateContent.put(EmailConstant.TAG_EMAIL_DELAI_INTERVENTION, nouvelleAnnonce.getDelaiIntervention()
                .getText());
        templateContent.put(EmailConstant.TAG_EMAIL_TYPE_CONTACT, nouvelleAnnonce.getTypeContact().getAffichage());

        // On charge les recepteurs
        emailDAO.prepareRecipient(confirmationAnnonceMessage, recipients, true);

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(confirmationAnnonceMessage,
                EmailConstant.TEMPLATE_CONFIRMATION_ANNONCE, templateContent);

        return noError;
    }

    /**
     * Envoi d'un mail d'activation pour les nouveaux clients.
     *
     * @param cleActivation La clé d'activation généré
     * @param url           L'url du front
     * @return True si l'envoi du mail s'est bien passé
     * @throws EmailException
     * @throws MandrillApiError
     * @throws IOException
     */
    public boolean envoiMailActivationCompte(String nom, String prenom, String login, String email,
                                             String cleActivation, String url) throws EmailException, MandrillApiError, IOException {

        // On prepare l'entete, on ne mets pas de titre (il est géré par
        // mandrillApp).
        MandrillMessage activationCompteMessage = emailDAO.prepareEmail(null);

        StringBuilder nomDestinataire = new StringBuilder();
        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<String, String>();

        ClientUtils.chooseNomClient(nom, prenom, login, nomDestinataire);
        recipients.put(nomDestinataire.toString(), email);

        // On charge les recepteurs
        emailDAO.prepareRecipient(activationCompteMessage, recipients, true);

        // On charge le contenu
        Map<String, String> templateContent = new HashMap<String, String>();
        templateContent.put(EmailConstant.TAG_EMAIL_USERNAME, login);

        StringBuilder lienActivation = new StringBuilder(url);
        lienActivation.append(UrlPage.ACTIVATION_URL.replace("/", ""));
        lienActivation.append("?key=");
        lienActivation.append(cleActivation);

        // On charge les variables dynamique à remplacer
        List<MergeVar> mergevars = new LinkedList<>();
        MergeVar mergeVar = new MergeVar(EmailConstant.TAG_EMAIL_ACTIVATION_LINK, lienActivation.toString());
        mergevars.add(mergeVar);
        activationCompteMessage.setGlobalMergeVars(mergevars);

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(activationCompteMessage, EmailConstant.TEMPLATE_ACTIVATION_COMPTE,
                templateContent);

        return noError;
    }

    /**
     * Envoi un email de contact contenant l'information contenue dans le DTO
     *
     * @param mail DTO contenant l'information du mail de contact saisie par le
     *             client
     * @return état d'envoi du mail
     */
    public boolean envoiEmailContact(ContactMailDTO mail) throws EmailException, MandrillApiError, IOException {
        // On prepare l'entete, on ne mets pas de titre (il est géré par
        // mandrillApp).
        MandrillMessage contactMessage = emailDAO.prepareEmail(null);

        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<String, String>();

        recipients.put(EmailConstant.EMAIL_FROM_NAME, EmailConstant.EMAIL_CASTOR_CONTACT);

        // On charge les recepteurs
        emailDAO.prepareRecipient(contactMessage, recipients, true);

        // On charge le contenu
        Map<String, String> templateContent = new HashMap<String, String>();
        templateContent.put(EmailConstant.TAG_EMAIL_CONTACT_NAME, mail.getName());
        templateContent.put(EmailConstant.TAG_EMAIL_CONTACT_SUBJECT, mail.getSubject());
        templateContent.put(EmailConstant.TAG_EMAIL_CONTACT_EMAIL, mail.getEmail());
        templateContent.put(EmailConstant.TAG_EMAIL_CONTACT_MESSAGE, mail.getMessage());

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(contactMessage, EmailConstant.TEMPLATE_EMAIL_CONTACT,
                templateContent);

        return noError;
    }

    /**
     * Envoi un email d'accusé de reception du message de contact
     *
     * @param mail DTO contenant l'information du mail de contact saisie par le
     *             client
     * @return état d'envoi du mail
     */
    public boolean envoiEmailAccuseReception(ContactMailDTO mail) throws EmailException, MandrillApiError, IOException {
        // On prepare l'entete, on ne mets pas de titre (il est géré par
        // mandrillApp).
        MandrillMessage accuseReception = emailDAO.prepareEmail(null);

        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<String, String>();
        recipients.put(mail.getName(), mail.getEmail());

        // On charge les recepteurs
        emailDAO.prepareRecipient(accuseReception, recipients, true);

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(accuseReception, EmailConstant.TEMPLATE_ACCUSE_RECEPTION, null);

        return noError;
    }

    /**
     * Envoi un email de notification
     *
     * @param notification contient toutes les infos (le type, le destinataire, etc) de
     *                     la notification
     * @return état d'envoi du mail
     */
    public boolean envoiEmailNotification(Notification notification) throws EmailException, MandrillApiError,
            IOException {
        // On prepare l'entete, on ne mets pas de titre (il est géré par
        // mandrillApp).
        MandrillMessage notificationMail = emailDAO.prepareEmail(null);

        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<>();

        if (notification.getPourQuiNotification().equals(TypeCompte.CLIENT)) {
            recipients.put(notification.getClientNotifier().getEmail(), notification.getClientNotifier().getEmail());
        } else {
            recipients.put(notification.getArtisanNotifier().getEmail(), notification.getArtisanNotifier().getEmail());
        }

        // On charge le contenu
        Map<String, String> templateContent = generateAndSaveContentForNotificationMail(notification, notificationMail);
        // On charge les recepteurs
        emailDAO.prepareRecipient(notificationMail, recipients, true);

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(notificationMail, notification.getTypeNotification().getEmailTemplate(), templateContent);

        return noError;
    }

    /**
     * Suivant le type de notification on rajoute le contenu qui sera remplacé dans l'email template
     * <p>
     * Deux type de tags sont présents ceux qui remplacent une balise via mc:edit (templateContent) et ceux qui remplacent un tag du type *|TOTO|* (mergedVars) dans le template
     *
     * @param notification     La notification qui va etre générée
     * @param notificationMail L'objet qui symbolise le mail
     * @return Une map contenant en clé le nom du tag dans le template avec sa valeur associée
     */
    private Map<String, String> generateAndSaveContentForNotificationMail(Notification notification, MandrillMessage notificationMail) {
        StringBuilder nomClient = new StringBuilder();
        ClientUtils.chooseNomClient(notification.getClientNotifier().getNom(), notification.getClientNotifier().getPrenom(), notification.getClientNotifier().getLogin(), nomClient);

        StringBuilder nomArtisan = new StringBuilder();
        ClientUtils.chooseNomClient(notification.getArtisanNotifier().getNom(), notification.getArtisanNotifier().getPrenom(), notification.getArtisanNotifier().getLogin(), nomArtisan);

        Properties urlProperties = PropertiesFileGeneral.URL.getProperties();
        String urlFrontend = urlProperties.getProperty("url.frontend.web");

        List<MandrillMessage.MergeVar> mergevars = new LinkedList<>();
        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(EmailConstant.TAG_EMAIL_NOTIFICATION_CLIENT, nomClient.toString());

        switch (notification.getTypeNotification()) {
            case A_NOTER_ENTREPRISE:
                MandrillMessage.MergeVar mergeVarsHomeANoter = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_NOTIFICATION_URL_FRONT, urlFrontend);
                mergevars.add(mergeVarsHomeANoter);
                templateContent.put(EmailConstant.TAG_EMAIL_NOTIFICATION_ARTISAN, nomArtisan.toString());
                break;
            case REPONDU_A_ANNONCE:
                MandrillMessage.MergeVar mergeVarsHomeARepondu = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_NOTIFICATION_URL_FRONT, urlFrontend);
                mergevars.add(mergeVarsHomeARepondu);
                templateContent.put(EmailConstant.TAG_EMAIL_NOTIFICATION_NOM_ENTREPRISE, notification.getArtisanNotifier().getEntreprise().getNomComplet());
                break;
            case A_CHOISI_ENTREPRISE:
                MandrillMessage.MergeVar mergeVarsEspaceClientARepondu = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_ESPACE_CLIENT, urlFrontend);
                mergevars.add(mergeVarsEspaceClientARepondu);
                templateContent.put(EmailConstant.TAG_EMAIL_NOTIFICATION_ARTISAN, nomArtisan.toString());
                break;
        }

        notificationMail.setGlobalMergeVars(mergevars);
        return templateContent;
    }

    /**
     * Preparation et envoi d'un mail de confirmation, dans le but d'informer
     * l'utilisateur que l'annonce a correctement été enregistrée.
     *
     * @param nouvelleAnnonce L'objet que l'on a recu du frontend
     * @return vrai si l'envoi s'est bien passé.
     * @throws EmailException
     * @throws MandrillApiError
     * @throws IOException
     */
    public boolean envoiMailAEquipeCreationAnnonce(Annonce nouvelleAnnonce) throws EmailException,
            MandrillApiError, IOException {

        // On prepare l'entete, on ne mets pas de titre.
        MandrillMessage notificationEquipe = emailDAO.prepareEmail(null);

        // On construit les recepteurs
        Map<String, String> recipients = new HashMap<>();
        recipients.put("Equipe les castors", PropertiesFileWS.EMAIL.getProperties().getProperty("email.box.team"));

        StringBuilder lienAccesAnnonce = new StringBuilder();
        lienAccesAnnonce.append(PropertiesFileGeneral.URL.getProperties().getProperty("url.frontend.web"));
        lienAccesAnnonce.append(UrlPage.ANNONCE.replace("/", ""));
        lienAccesAnnonce.append("?idAnnonce=");
        lienAccesAnnonce.append(nouvelleAnnonce.getHashID());

        // On charge les variables dynamique à remplacer
        List<MergeVar> mergevars = new LinkedList<>();
        MergeVar mergeVar = new MergeVar(EmailConstant.TAG_EMAIL_NOTIFICATION_EQUIPE_NOUVELLE_ANNONCE_ID, lienAccesAnnonce.toString());
        mergevars.add(mergeVar);
        notificationEquipe.setGlobalMergeVars(mergevars);

        // On charge les recepteurs
        emailDAO.prepareRecipient(notificationEquipe, recipients, true);

        // On envoi le mail
        boolean noError = emailDAO.sendEmailTemplate(notificationEquipe,
                EmailConstant.TEMPLATE_NOTIFICATION_EQUIPE_NOUVELLE_ANNONCE, new HashMap<>());

        return noError;
    }
}