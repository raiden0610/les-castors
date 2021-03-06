package fr.castor.ws.dao;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import fr.castor.core.constant.EmailConstant;
import fr.castor.core.enums.PropertiesFileGeneral;
import fr.castor.core.exception.EmailException;
import fr.castor.ws.enums.PropertiesFileWS;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Classe de formatage et d'envoi d'email, l'envoi de mail est realisé par
 * mandrillapp
 *
 * @author Casaucau Cyril
 */
@Stateless(name = "EmailDAO")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmailDAO {

    private final MandrillApi mandrillApi;
    private String apiKey;
    private boolean emailActive;

    public EmailDAO() {
        getMessageProperties();
        mandrillApi = new MandrillApi(apiKey);
    }

    /**
     * Prepare l'email : régle les differentes options, ajoute l'emetteur, etc
     *
     * @param subject L'objet du mail, null dans le cas d'un template
     * @return
     */
    public MandrillMessage prepareEmail(String subject) {

        // create your message
        MandrillMessage message = new MandrillMessage();
        message.setSubject(subject);
        message.setAutoText(true);
        message.setFromEmail(EmailConstant.EMAIL_CASTOR_NOTIF);
        message.setFromName(EmailConstant.EMAIL_FROM_NAME);

        return message;
    }

    /**
     * Ajoute le ou les recepteurs au mail.
     *
     * @param message            Le message qui sera envoyé
     * @param recipients         Les recepteurs sous forme de map
     * @param preserveRecipients est ce que les recepteurs doivent se voir entre eux?
     */
    public void prepareRecipient(MandrillMessage message, Map<String, String> recipients, boolean preserveRecipients) {

        List<Recipient> to = new LinkedList<Recipient>();

        for (Entry<String, String> recipient : recipients.entrySet()) {
            Recipient recipientFormatted = new Recipient();
            recipientFormatted.setName(recipient.getKey());
            recipientFormatted.setEmail(recipient.getValue());
            to.add(recipientFormatted);
        }

        message.setTo(to);
        message.setPreserveRecipients(preserveRecipients);
    }

    /**
     * Ajoute le contenu au mail <br/>
     * A utiliser dans le cas de l'envoi sans template.
     *
     * @param message
     * @param htmlContent
     */
    public void prepareContent(MandrillMessage message, String htmlContent) {
        // set the html message
        // "<html>The apache logo - <img src=\"cid:" + "\"></html>"
        message.setHtml(htmlContent);
    }

    /**
     * Envoi d'un message sans template
     * <p/>
     * Méthode prepareContent a utiliser obligatoirement en passant par cette
     * methode.
     *
     * @param message le message a envoyé
     * @return vrai si pas d'erreur
     * @throws MandrillApiError
     * @throws IOException
     * @throws EmailException
     */
    public boolean sendEmail(MandrillMessage message) throws MandrillApiError, IOException, EmailException {
        if (emailActive) {
            MandrillMessageStatus[] messagesStatus = mandrillApi.messages().send(message, false);
            return checkErrorOnSentEmail(messagesStatus);
        } else {
            return true;
        }

    }

    /**
     * Envoi de mail avec template
     *
     * @param message         le mail que l'on va envoyer
     * @param templateName    Le nom du template présent sur mandrillapp
     * @param templateContent Le contenu dynamic à remplacer.
     * @return vrai si pas d'erreur
     * @throws MandrillApiError
     * @throws IOException
     * @throws EmailException
     */
    public boolean sendEmailTemplate(MandrillMessage message, String templateName, Map<String, String> templateContent)
            throws MandrillApiError, IOException, EmailException {

        if (emailActive) {
            remplirFooter(message);
            MandrillMessageStatus[] messagesStatus = mandrillApi.messages().sendTemplate(templateName, templateContent,
                    message, false);

            return checkErrorOnSentEmail(messagesStatus);
        } else {
            return true;
        }
    }

    /**
     * Charge le contenu à remplacer dynamiquement dans le mail
     * <p/>
     * Tag à remplacer dans le mail qui ne sont pas des balises mais peuvent être n'importe quoi a remplacer dans le mail.
     * <p/>
     * Exemple : *|TOTO|*
     *
     * @param mail Le mail
     */
    public void remplirFooter(MandrillMessage mail) {
        // On charge les variables dynamique à remplacer
        Properties urlProperties = PropertiesFileGeneral.URL.getProperties();
        String urlFront = urlProperties.getProperty("url.frontend.web");
        String urlFb = urlProperties.getProperty("url.fb");
        String urlTwitter = urlProperties.getProperty("url.twitter");
        String urlGooglePlus = urlProperties.getProperty("url.google.plus");

        List<MandrillMessage.MergeVar> mergevars = new LinkedList<>();

        MandrillMessage.MergeVar mergeVarsHome = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_HOME, urlFront);
        MandrillMessage.MergeVar mergeVarsEspaceClient = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_ESPACE_CLIENT, urlFront);
        MandrillMessage.MergeVar mergeVarsFB = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_FB, urlFb);
        MandrillMessage.MergeVar mergeVarsTwitter = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_TWITTER, urlTwitter);
        MandrillMessage.MergeVar mergeVarsGooglePlus = new MandrillMessage.MergeVar(EmailConstant.TAG_EMAIL_GOOGLEPLUS, urlGooglePlus);

        mergevars.add(mergeVarsHome);
        mergevars.add(mergeVarsEspaceClient);
        mergevars.add(mergeVarsFB);
        mergevars.add(mergeVarsTwitter);
        mergevars.add(mergeVarsGooglePlus);

        if(mail.getGlobalMergeVars() != null){
            mail.getGlobalMergeVars().addAll(mergevars);
        }else{
            mail.setGlobalMergeVars(mergevars);
        }
    }

    /**
     * Regarde si des erreurs reviennent lors de l'envoi des mails
     *
     * @param messagesStatus Tableau d'erreurs d'envoi de mail
     * @return vrai si pas d'erreur.
     * @throws EmailException
     */
    private boolean checkErrorOnSentEmail(MandrillMessageStatus[] messagesStatus) throws EmailException {

        boolean noError = true;
        // On verifie que tout s'est bien passé.
        for (int i = 0; i < messagesStatus.length; i++) {
            if (!EmailConstant.EMAIL_SENT.equals(messagesStatus[i].getStatus())) {
                noError = false;
                throw new EmailException("Certain mails n'ont pas été distribués correctement",
                        messagesStatus[i].getEmail(), messagesStatus[i].getStatus());
            }
        }
        return noError;
    }

    /**
     * Charge les parametres pour communiquer avec mandrillapp
     */
    private void getMessageProperties() {
        final Properties appProperties = PropertiesFileWS.EMAIL.getProperties();
        apiKey = appProperties.getProperty("mandrill.api.key");
        emailActive = Boolean.valueOf(appProperties.getProperty("email.active"));
    }
}