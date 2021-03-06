package fr.castor.web.selenium.dataset;

import com.ninja_squad.dbsetup.operation.Operation;
import fr.castor.dto.constant.Categorie;
import fr.castor.dto.enums.*;

import static com.ninja_squad.dbsetup.Operations.insertInto;

public class AnnonceDataset {

    public static final Operation INSERT_ANNONCE_DATA = insertInto("annonce")
            .columns("id", "datecreation", "datemaj", "delaiintervention", "description", "etatannonce",
                    "nbconsultation", "typecontact", "hashID", "selHashID", "typeTravaux", "adressechantier_id",
                    "demandeur_fk", "entreprise_selectionnee_fk", "avis_id")
            .values(200010, "2014-01-10", "2014-01-10", DelaiIntervention.LE_PLUS_RAPIDEMENT_POSSIBLE,
                    "Construction compliqué qui necessite des connaissance en geologie", EtatAnnonce.ACTIVE,
                    0, TypeContact.EMAIL, "toto", "tata", TypeTravaux.NEUF, 200005, 100001, null, null)
            .values(200011, "2014-01-10", "2014-01-10", DelaiIntervention.LE_PLUS_RAPIDEMENT_POSSIBLE,
                    "Construction compliqué qui necessite des connaissance en geologie", EtatAnnonce.ACTIVE,
                    0, TypeContact.EMAIL, "lolmdr", "tata", TypeTravaux.NEUF, 200014, 100001, 200009, 200013)
            .values(200012, "2014-01-10", "2014-01-10", DelaiIntervention.LE_PLUS_RAPIDEMENT_POSSIBLE,
                    "Construction compliqué qui necessite des connaissance en geologie", EtatAnnonce.ACTIVE,
                    0, TypeContact.EMAIL, "lolxd", "titi", TypeTravaux.NEUF, 200015, 100001, null, null)
            .values(200013, "2014-01-10", "2014-01-10", DelaiIntervention.LE_PLUS_RAPIDEMENT_POSSIBLE,
                    "Construction compliqué qui necessite des connaissance en geologie", EtatAnnonce.DONNER_AVIS,
                    0, TypeContact.EMAIL, "lolmdrxD", "titi", TypeTravaux.NEUF, 200016, 100001, 200009, null).build();

    public static final Operation INSERT_ARTISAN_DATA = insertInto("artisan")
            .columns("id", "civilite", "email", "nom", "prenom", "login", "password", "numeroTel", "dateInscription",
                    "isActive", "cleActivation", "entreprise_id")
            .values(200008, Civilite.MONSIEUR, "artisan.castor@outlook.fr", "PebronArtisan", "Toto", "pebron",
                    "$s0$54040$h99gyX0NNTBvETrAdfjtDw==$fo2obQTG56y7an9qYl3aEO+pv3eH6p4hLzK1xt8EuoY=", "0614125698",
                    "2014-01-10", true, "lolmdr07", 200009).build();

    public static final Operation INSERT_ENTREPRISE_DATA = insertInto("entreprise")
            .columns("id", "nomcomplet", "statutjuridique", "siret", "datecreation", "adresse_id", "isverifier")
            .values(200009, "Entreprise de toto", StatutJuridique.SARL, "43394298400017", "2014-03-23", 200006, true).build();

    public static final Operation INSERT_NOTIFICATION_DATA = insertInto("notification")
            .columns("id", "dateNotification", "typeNotification", "pourquinotification", "statutnotification",
                    "id_artisan", "id_client", "id_annonce")
            .values(200006, "2014-02-10", TypeNotification.INSCRIT_A_ANNONCE, 4, 0, 200008, 100001, 200010)
            .values(200007, "2014-02-10", TypeNotification.INSCRIT_A_ANNONCE, 4, 0, 200008, 100001, 200011).build();

    public static final Operation INSERT_ADRESSE_DATA = insertInto("adresse")
            .columns("id", "adresse", "codepostal", "complementadresse", "ville", "departement")
            .values(200005, "254 chemin du test", "06700", "Residence du test", "ST LAURENT DU VAR", 06)
            .values(200006, "260 chemin des lol", "06700", "Residence des lol", "ST LAURENT DU VAR", 07)
            .values(200014, "254 chemin du test", "06700", "Residence du test", "ST LAURENT DU VAR", 06)
            .values(200015, "270 chemin du mdr", "06700", "Residence du mdr", "ST LAURENT DU VAR", 8)
            .values(200016, "280 chemin du lolmdrxD", "06700", "Residence du lolmdrxD", "ST LAURENT DU VAR", 9).build();

    public static final Operation INSERT_AVIS_DATA = insertInto("avis")
            .columns("id", "commentaire", "dateavis", "score", "artisan_fk")
            .values(200012, "Ké buenos, Artisan très sympatique, travail bien fait", "2014-03-23 22:00:00.0", 4, 200008)
            .values(200013, "Artisan moins sympatique", "2014-12-01 22:00:00.0", 3, 200008).build();

    public static final Operation INSERT_ARTISAN_PERMISSION = insertInto("permission")
            .columns("typecompte", "artisan_fk").values(TypeCompte.ARTISAN, 200008).build();

    public static final Operation INSERT_ANNONCE_ARTISAN = insertInto("annonce_artisan")
            .columns("annonce_id", "artisans_id").values(200010, 200008).values(200011, 200008).values(200013, 200008)
            .build();

    public static final Operation INSERT_ANNONCE_IMAGE = insertInto("image")
            .columns("id", "url", "id_annonce")
            .values(10001,
                    "http://res.cloudinary.com/lescastors/image/upload/q_27/v1430908935/test/srwdbvzwlvhxoytsheha.jpg",
                    200011).build();

    public static final Operation INSERT_ANNONCE_MOT_CLE = insertInto("motcle")
            .columns("id", "motcle", "annonce_fk")
            .values(10001, "Salles de bains", 200010)
            .values(10002, "Salles de bains", 200011)
            .values(10003, "Salles de bains", 200012)
            .values(10004, "Salles de bains", 200013).build();

    public static final Operation INSERT_CATEGORIE_METIER = insertInto("categoriemetier")
            .columns("id", "categoriemetier", "entreprise_fk", "motcle_fk")
            .values(10001, Categorie.ELECTRICITE_CODE, null, 10001)
            .values(10002, Categorie.DECORATION_MACONNERIE_CODE, null, 10001)
            .values(10003, Categorie.ELECTRICITE_CODE, null, 10002)
            .values(10004, Categorie.DECORATION_MACONNERIE_CODE, null, 10002)
            .values(10005, Categorie.ELECTRICITE_CODE, null, 10003)
            .values(10006, Categorie.DECORATION_MACONNERIE_CODE, null, 10003)
            .values(10007, Categorie.ELECTRICITE_CODE, null, 10004)
            .values(10008, Categorie.DECORATION_MACONNERIE_CODE, null, 10004).build();
}
