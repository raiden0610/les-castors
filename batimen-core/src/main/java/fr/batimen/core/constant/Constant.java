package fr.batimen.core.constant;

/**
 * Contient toutes les constantes comunes aux deux application frontend /
 * backend de batimen.
 * 
 * @author Casaucau Cyril
 * 
 */
public class Constant {

    private Constant() {

    }

    // Code retour service
    public static final Integer CODE_SERVICE_RETOUR_OK = 0;
    public static final Integer CODE_SERVICE_RETOUR_KO = 1;

    // Code retour propre aux annonces
    public static final Integer CODE_SERVICE_RETOUR_DUPLICATE = 10;

    // Roles accés WS (Glassfish AUTH)
    public static final String USERS_ROLE = "users";
    public static final String ADMIN_ROLE = "admins";

    // Accés User WS (Glassfish AUTH)
    public static final String BATIMEN_USERS_WS = "batimenuser";
    public static final String BATIMEN_PWD_WS = "Lolmdr06";

    // Timeout de connection au WS
    public static final int CONNECT_TIMEOUT = 10000;

    // Nom des pages web
    public static final String ACCUEIL_URL = "/accueil";
    public static final String AUTHENTIFICATION_URL = "/connexion";
    public static final String MON_COMPTE_URL = "/moncompte";
    public static final String QUI_SOMMES_NOUS_URL = "/quisommesnous";
    public static final String CONTACT_URL = "/contact";
    public static final String CGU_URL = "/cgu";
    public static final String NOUVEAU_DEVIS_URL = "/nouveaudevis";

    // Email
    public static final String EMAIL_FROM = "admin@lol.fr";
    public static final String EMAIL_FROM_NAME = "Admin";
    public static final String EMAIL_SENT = "sent";
    public static final String EMAIL_QUEUED = "queued";
    public static final String EMAIL_REJECTED = "rejected";
    public static final String EMAIL_INVALID = "invalid";

    // Template Email
    public static final String TEMPLATE_CONFIRMATION_ANNONCE = "confirmation_creation_annonce";

    // Creation annonce tags Email
    public static final String TAG_EMAIL_USERNAME = "username";
    public static final String TAG_EMAIL_TITRE = "titre";
    public static final String TAG_EMAIL_METIER = "metier";
    public static final String TAG_EMAIL_SOUS_CATEGORIE_METIER = "souscategoriemetier";
    public static final String TAG_EMAIL_TYPE_CONTACT = "typecontact";
    public static final String TAG_EMAIL_DELAI_INTERVENTION = "delaiintervention";

}
