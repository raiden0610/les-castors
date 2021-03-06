package fr.castor.test.ws.service;

import fr.castor.core.constant.CodeRetourService;
import fr.castor.dto.*;
import fr.castor.dto.aggregate.*;
import fr.castor.dto.constant.Categorie;
import fr.castor.dto.enums.*;
import fr.castor.ws.client.service.AnnonceServiceREST;
import fr.castor.ws.dao.AnnonceDAO;
import fr.castor.ws.dao.ArtisanDAO;
import fr.castor.ws.dao.ClientDAO;
import fr.castor.ws.entity.Annonce;
import fr.castor.ws.entity.Client;
import fr.castor.ws.entity.Image;
import fr.castor.ws.enums.PropertiesFileWS;
import fr.castor.ws.service.NotificationService;
import fr.castor.ws.service.PhotoService;
import org.junit.Assert;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Classe contenant le code de test quand celui ci est utilisé a plusieurs endroits.
 * <p/>
 * <p/>
 * Created by Casaucau on 30/04/2015.
 */
@ApplicationScoped
@Named
public class AnnonceServiceTest {

    @Inject
    private ClientDAO clientDAO;

    @Inject
    private AnnonceDAO annonceDAO;

    @Inject
    private NotificationService notificationService;

    @Inject
    private ArtisanDAO artisanDAO;

    @Inject
    private AnnonceServiceREST annonceServiceREST;

    @Inject
    private AnnonceServiceTest annonceServiceTest;

    @Inject
    private PhotoService photoService;

    public List<ImageDTO> testSuppressionPhoto(String loginDemandeur, boolean allowed) throws IOException {

        List<Image> images = null;
        String hashID = "88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21";
        if (allowed) {
            testAjoutPhoto(loginDemandeur, 3);
            //On prends tjr les photos de pebronne pour le cas ou on test avec l'admin
            images = photoService.getImagesByHashIDByLoginDemandeur("particulier", hashID, "pebronne");
            Assert.assertEquals(3, images.size());
        }
        
        ImageDTO imageDTO = new ImageDTO();

        if (!allowed) {
            imageDTO.setUrl("https://res.cloudinary.com/lescastors/image/upload/v1427874120/test/zbeod6tici6yrphpco39.jpg");
        } else {
            imageDTO.setUrl(images.get(0).getUrl());
        }

        SuppressionPhotoDTO suppressionPhotoDTO = new SuppressionPhotoDTO();
        suppressionPhotoDTO.setLoginDemandeur(loginDemandeur);
        suppressionPhotoDTO.setId(hashID);
        suppressionPhotoDTO.setImageASupprimer(imageDTO);

        List<ImageDTO> imageDTOs = annonceServiceREST.suppressionPhoto(suppressionPhotoDTO);

        images = photoService.getImagesByHashIDByLoginDemandeur("particulier", hashID, "pebronne");
        Assert.assertEquals(2, images.size());

        return imageDTOs;
    }

    public List<ImageDTO> testGetPhoto(String login) {
        DemandeAnnonceDTO demandeAnnonceDTO = new DemandeAnnonceDTO();
        demandeAnnonceDTO.setId("88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21");
        demandeAnnonceDTO.setLoginDemandeur(login);

        return annonceServiceREST.getPhotos(demandeAnnonceDTO);
    }

    public void testAjoutPhoto(String login, int nbImageAttendu) throws IOException {

        // On recupére la photo dans les ressources de la webapp de test
        Properties seleniumProperties = PropertiesFileWS.IMAGE.getProperties();
        StringBuilder adresseToImg = new StringBuilder(seleniumProperties.getProperty("app.temp.img.dir.test"));
        adresseToImg.append("castor.jpg");
        File castorFile = new File(adresseToImg.toString());

        AjoutPhotoDTO ajoutPhotoDTO = new AjoutPhotoDTO();
        ajoutPhotoDTO.setLoginDemandeur(login);
        ajoutPhotoDTO.setId("88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21");
        ajoutPhotoDTO.getImages().add(castorFile);

        List<ImageDTO> imageDTOs = annonceServiceREST.ajouterPhoto(ajoutPhotoDTO);

        Assert.assertNotNull(imageDTOs);
        Assert.assertEquals(nbImageAttendu, imageDTOs.size());
    }

    public void testModificationAnnonce(TypeCompte typeCompte, String loginDemandeur, Integer codeRetourServiceAttendu) {
        DemandeAnnonceDTO demandeAnnonceDTO = createDemandeAnnonceDTO(
                "88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21",
                "pebronne", typeCompte);

        AnnonceAffichageDTO annonceAffichage = annonceServiceREST.getAnnonceByIDForAffichage(demandeAnnonceDTO);
        Assert.assertNotNull(annonceAffichage);

        ModificationAnnonceDTO modificationAnnonceDTO = new ModificationAnnonceDTO();
        modificationAnnonceDTO.setAnnonce(annonceAffichage.getAnnonce());
        modificationAnnonceDTO.getAnnonce().setHashID(demandeAnnonceDTO.getId());
        modificationAnnonceDTO.setAdresse(annonceAffichage.getAdresse());
        modificationAnnonceDTO.setLoginDemandeur(loginDemandeur);

        modificationAnnonceDTO.getAnnonce().setTypeContact(TypeContact.TELEPHONE);
        modificationAnnonceDTO.getAnnonce().setTypeTravaux(TypeTravaux.RENOVATION);
        modificationAnnonceDTO.getAnnonce().setDelaiIntervention(DelaiIntervention.SIX_MOIS);

        MotCleDTO motCleDTO = new MotCleDTO();
        motCleDTO.setMotCle("Piscine");

        CategorieMetierDTO categorieMetierDTO = new CategorieMetierDTO();
        categorieMetierDTO.setCategorieMetier(Categorie.PLOMBERIE_CODE);
        motCleDTO.getCategoriesMetier().add(categorieMetierDTO);

        modificationAnnonceDTO.getAnnonce().getMotCles().add(motCleDTO);

        Integer codeRetourOK = annonceServiceREST.modifierAnnonce(modificationAnnonceDTO);

        Assert.assertNotNull(codeRetourOK);
        Assert.assertEquals(codeRetourServiceAttendu, codeRetourOK);
    }

    public AvisArtisanDTO createNotationDTO(String loginDemandeur) {
        AvisDTO avisDTO = new AvisDTO();
        avisDTO.setScore((double) 4);
        avisDTO.setCommentaire("Bon travaux");
        avisDTO.setNomEntreprise("Pebronne enterprise");
        avisDTO.setNomPrenomOrLoginClient("pebronne");

        AvisArtisanDTO avisArtisanDTO = new AvisArtisanDTO();
        avisArtisanDTO.setNotation(avisDTO);
        avisArtisanDTO
                .setHashID("88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21");
        avisArtisanDTO.setLoginArtisan("pebronneArtisanne");
        avisArtisanDTO.setLoginDemandeur(loginDemandeur);

        return avisArtisanDTO;
    }

    public Annonce testDesinscriptionArtisan(String loginDemandeur) {
        DesinscriptionAnnonceDTO desinscriptionAnnonceDTO = new DesinscriptionAnnonceDTO();
        desinscriptionAnnonceDTO
                .setId("88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21");
        desinscriptionAnnonceDTO.setLoginDemandeur(loginDemandeur);
        desinscriptionAnnonceDTO.setLoginArtisan("pebronneArtisanne");

        Integer codeRetourOK = annonceServiceREST.desinscriptionArtisan(desinscriptionAnnonceDTO);

        Assert.assertEquals(CodeRetourService.RETOUR_OK, codeRetourOK);

        Annonce annonce = annonceDAO
                .getAnnonceByIDWithTransaction(
                        "88263227a51224d8755b21e729e1d10c0569b10f98749264ddf66fb65b53519fb863cf44092880247f2841d6335473a5d99402ae0a4d9d94f665d97132dcbc21",
                        false);

        return annonce;
    }

    public DemandeAnnonceDTO initAndGetDemandeAnnonceDTO(String hash, String loginDemandeur, TypeCompte typeCompte) {
        DemandeAnnonceDTO demandeAnnonceDTO = new DemandeAnnonceDTO();
        demandeAnnonceDTO.setId(hash);
        demandeAnnonceDTO.setLoginDemandeur(loginDemandeur);
        demandeAnnonceDTO.setTypeCompteDemandeur(typeCompte);
        return demandeAnnonceDTO;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void assertEntrepriseForChoixArtisanWithTransaction(boolean isSuppression) {
        Annonce annonce = annonceDAO.getAnnonceByIDWithTransaction("lolmdrp", true);
        Assert.assertNotNull(annonce);
        if (isSuppression) {
            Assert.assertNull(annonce.getEntrepriseSelectionnee());
        } else {
            Assert.assertNotNull(annonce.getEntrepriseSelectionnee());
            Assert.assertEquals("Pebronne enterprise choisi", annonce.getEntrepriseSelectionnee().getNomComplet());
            Assert.assertEquals(EtatAnnonce.DONNER_AVIS, annonce.getEtatAnnonce());
            List<NotificationDTO> notificationsDTO = notificationService.getNotificationByLogin(annonce
                    .getEntrepriseSelectionnee().getArtisan().getLogin(), TypeCompte.ARTISAN, 0, 15);

            for (NotificationDTO notification : notificationsDTO) {
                Assert.assertEquals(TypeNotification.A_CHOISI_ENTREPRISE, notification.getTypeNotification());
                Assert.assertEquals(StatutNotification.PAS_VUE, notification.getStatutNotification());
            }
        }
    }

    public DemandeAnnonceDTO createDemandeAnnonceDTO(String hashID, String login, TypeCompte typeCompte) {
        DemandeAnnonceDTO demandeAnnonceDTO = new DemandeAnnonceDTO();
        demandeAnnonceDTO.setId(hashID);
        demandeAnnonceDTO.setLoginDemandeur(login);
        demandeAnnonceDTO.setTypeCompteDemandeur(typeCompte);
        return demandeAnnonceDTO;
    }

    public void creationVerificationAnnonce(CreationAnnonceDTO creationAnnonceDTO) {
        String loginDeJohnny = "johnny06";
        Integer isCreationOK = annonceServiceREST.creationAnnonce(creationAnnonceDTO);

        // On utilise le DAO de l'annonce et de l'user pour vérifier le tout
        Client johnny = clientDAO.getClientByLoginName(loginDeJohnny);
        List<Annonce> annoncesDeJohnny = annonceDAO.getAnnoncesByLogin(loginDeJohnny);

        // On test que la creation de l'annonce et du client en bdd est ok
        Assert.assertTrue(isCreationOK == CodeRetourService.RETOUR_OK);
        // On check que le client est présent dans la BDD
        Assert.assertNotNull(johnny);
        // On regarde que ce soit le bon login
        Assert.assertTrue(loginDeJohnny.equals(johnny.getLogin()));
        // On s'assure que la liste d'annonce renvoyées n'est pas null
        Assert.assertNotNull(annoncesDeJohnny);
        // On test qu'il y a bien une annonce liée à Johnny boy
        Assert.assertTrue(annoncesDeJohnny.size() == 1);

        for (Annonce annonce : annoncesDeJohnny) {
            Assert.assertFalse(annonce.getHashID().isEmpty());
            Assert.assertFalse(annonce.getSelHashID().isEmpty());
        }
    }

    public void searchAnnonce(String login, List<CategorieMetierDTO> categorieToInclude, int numberAnnonceToAssert) {
        SearchAnnonceDTOIn searchAnnonceDTO = new SearchAnnonceDTOIn();
        searchAnnonceDTO.setRangeDebut(0);
        searchAnnonceDTO.setRangeFin(20);
        searchAnnonceDTO.setLoginDemandeur(login);
        searchAnnonceDTO.getCategoriesMetierDTO().addAll(categorieToInclude);
        searchAnnonceDTO.setDepartement(06);

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.set(2014, 02, 28);

        SearchAnnonceDTOOut searchAnnonceDTOOut = annonceServiceREST.searchAnnonce(searchAnnonceDTO);
        Assert.assertNotNull(searchAnnonceDTOOut);
        Assert.assertEquals(numberAnnonceToAssert, searchAnnonceDTOOut.getAnnonceDTOList().size());
        Assert.assertEquals(numberAnnonceToAssert, searchAnnonceDTOOut.getNbTotalResultat());
    }
}