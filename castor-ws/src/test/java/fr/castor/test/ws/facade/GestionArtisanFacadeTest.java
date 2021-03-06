package fr.castor.test.ws.facade;

import fr.castor.core.constant.CodeRetourService;
import fr.castor.core.security.HashHelper;
import fr.castor.dto.*;
import fr.castor.dto.aggregate.AjoutPhotoDTO;
import fr.castor.dto.aggregate.CreationPartenaireDTO;
import fr.castor.dto.aggregate.SuppressionPhotoDTO;
import fr.castor.dto.constant.Categorie;
import fr.castor.dto.enums.Civilite;
import fr.castor.dto.enums.StatutJuridique;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.test.ws.AbstractBatimenWsTest;
import fr.castor.ws.client.service.ArtisanServiceREST;
import fr.castor.ws.dao.ArtisanDAO;
import fr.castor.ws.dao.EntrepriseDAO;
import fr.castor.ws.entity.*;
import fr.castor.ws.enums.PropertiesFileWS;
import fr.castor.ws.service.PhotoService;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class GestionArtisanFacadeTest extends AbstractBatimenWsTest {

    @Inject
    private ArtisanDAO artisanDAO;

    @Inject
    private EntrepriseDAO entrepriseDAO;

    @Inject
    private ArtisanServiceREST artisanServiceREST;

    @Inject
    private PhotoService photoService;

    @Test
    public void nouveauPartenaireTestNominal() {

        CreationPartenaireDTO nouveauPartenaire = new CreationPartenaireDTO();
        // Adresse
        nouveauPartenaire.getAdresse().setAdresse("250 chemin du plombier");
        nouveauPartenaire.getAdresse().setComplementAdresse("res de la fuite");
        nouveauPartenaire.getAdresse().setCodePostal("06800");
        nouveauPartenaire.getAdresse().setVille("Tuyaux ville");
        nouveauPartenaire.getAdresse().setDepartement(50);

        // Artisan
        nouveauPartenaire.getArtisan().setCivilite(Civilite.MONSIEUR);
        nouveauPartenaire.getArtisan().setEmail("plombier@tuyaux.com");
        nouveauPartenaire.getArtisan().setLogin("plombier06");
        nouveauPartenaire.getArtisan().setNom("Dupont");
        nouveauPartenaire.getArtisan().setNumeroTel("0645789655");
        nouveauPartenaire.getArtisan().setPassword(HashHelper.hashScrypt("lolmdr06"));
        nouveauPartenaire.getArtisan().setPrenom("David");

        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setTypeCompte(TypeCompte.ARTISAN);

        nouveauPartenaire.getArtisan().getPermissions().add(permissionDTO);

        // Entreprise
        nouveauPartenaire.getEntreprise().setDateCreation(new Date());

        List<CategorieMetierDTO> categoriesMetier = new ArrayList<CategorieMetierDTO>();
        categoriesMetier.add(Categorie.getPlomberie());

        nouveauPartenaire.getEntreprise().getCategoriesMetier().addAll(categoriesMetier);
        nouveauPartenaire.getEntreprise().setNbEmployees(2);
        nouveauPartenaire.getEntreprise().setNomComplet("Entreprise de la plomberie");
        nouveauPartenaire.getEntreprise().setSiret("43394298400017");

        nouveauPartenaire.getEntreprise().setStatutJuridique(StatutJuridique.SARL);

        Integer retourService = artisanServiceREST.creationNouveauPartenaire(nouveauPartenaire);

        assertNotNull(retourService);
        assertEquals(CodeRetourService.RETOUR_OK, retourService);

        Artisan artisanEnregistre = artisanDAO.getArtisanByEmail(nouveauPartenaire.getArtisan().getEmail());
        assertNotNull(artisanEnregistre);
        assertEquals("plombier@tuyaux.com", artisanEnregistre.getEmail());

        Entreprise entreprise = entrepriseDAO.getEntrepriseByArtisan(artisanEnregistre.getLogin());
        assertNotNull(entreprise);
        assertEquals("Entreprise de la plomberie", entreprise.getNomComplet());

        Adresse adresseEntreprise = entreprise.getAdresse();
        assertNotNull(adresseEntreprise);
        assertEquals("250 chemin du plombier", adresseEntreprise.getAdresse());

        Set<Permission> permissions = artisanEnregistre.getPermission();

        assertNotNull(permissions.iterator().next());
        assertEquals(TypeCompte.ARTISAN, permissions.iterator().next().getTypeCompte());
    }

    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void getEntrepriseInformationNominal(){
        EntrepriseDTO entrepriseDTO = artisanServiceREST.getEntrepriseInformationByArtisanLogin("pebronneArtisanne");

        assertNotNull(entrepriseDTO);
        assertEquals("Pebronne enterprise", entrepriseDTO.getNomComplet());
        assertTrue(!entrepriseDTO.getCategoriesMetier().isEmpty());

        assertNotNull(entrepriseDTO.getAdresseEntreprise());
        assertEquals("106 chemin du pébron", entrepriseDTO.getAdresseEntreprise().getAdresse());

        assertNotNull(entrepriseDTO.getPhotosChantiersTemoins());
        assertFalse(entrepriseDTO.getPhotosChantiersTemoins().isEmpty());

    }

    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void getEntrepriseInformationLoginExistPas(){
        EntrepriseDTO entrepriseDTO = artisanServiceREST.getEntrepriseInformationByArtisanLogin("existpas");
        assertNotNull(entrepriseDTO);
        assertNull(entrepriseDTO.getAdresseEntreprise());
    }

    /**
     * Cas de test : l'utilisateur modifie ses informations via le formulaire de modification.
     * Tout se passe comme prévu.
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    @ShouldMatchDataSet(value = "datasets/out/modification_entreprise.yml",
            excludeColumns = {"id", "datemaj", "datecreation", "motcle_fk", "entreprise_fk"})
    public void saveEntrepriseInformation(){
        EntrepriseDTO entrepriseDTO = new EntrepriseDTO();
        entrepriseDTO.setNomComplet("Pebronne enterprise");
        entrepriseDTO.setStatutJuridique(StatutJuridique.SARL);
        entrepriseDTO.setSiret("43394298400017");
        entrepriseDTO.setNbEmployees(20);
        entrepriseDTO.setSpecialite("brique");

        Calendar cal = Calendar.getInstance();
        cal.set(2014, 03, 23, 22, 00, 00);
        entrepriseDTO.setDateCreation(cal.getTime());

        entrepriseDTO.getCategoriesMetier().add(Categorie.getPlomberie());
        entrepriseDTO.getCategoriesMetier().add(Categorie.getMaconnerie());

        AdresseDTO adresseDTO = new AdresseDTO();
        adresseDTO.setAdresse("2410 avenue de l'artisan");
        adresseDTO.setComplementAdresse("Res des artisans");
        adresseDTO.setCodePostal("06500");
        adresseDTO.setDepartement(6);
        adresseDTO.setVille("YOLO City");

        entrepriseDTO.setAdresseEntreprise(adresseDTO);

        Integer codeRetour = artisanServiceREST.saveEntrepriseInformation(entrepriseDTO);
        assertNotNull(codeRetour);
        assertEquals(CodeRetourService.RETOUR_OK, codeRetour);
    }

    /**
     * Cas de test : L'utilisateur essaye de modifier le code html pour changer des informations ne pouvant être modifié.
     * Le webservice claque une erreur.
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    @ShouldMatchDataSet(value = "datasets/in/entreprises_informations.yml", excludeColumns = { "id", "datemaj", "datecreation" })
    public void saveEntrepriseInformationNotExiste(){
        EntrepriseDTO entrepriseDTO = new EntrepriseDTO();
        entrepriseDTO.setNomComplet("existe pas");
        entrepriseDTO.setStatutJuridique(StatutJuridique.SARL);
        entrepriseDTO.setSiret("43394298400017");
        entrepriseDTO.setNbEmployees(20);

        Calendar cal = Calendar.getInstance();
        cal.set(2014, 03, 23, 22, 00, 00);
        entrepriseDTO.setDateCreation(cal.getTime());

        entrepriseDTO.getCategoriesMetier().add(Categorie.getPlomberie());
        entrepriseDTO.getCategoriesMetier().add(Categorie.getMaconnerie());

        AdresseDTO adresseDTO = new AdresseDTO();
        adresseDTO.setAdresse("106 chemin de la modification");
        adresseDTO.setComplementAdresse("Res de la modif");
        adresseDTO.setCodePostal("06600");
        adresseDTO.setDepartement(6);
        adresseDTO.setVille("Antibes");

        entrepriseDTO.setAdresseEntreprise(adresseDTO);

        Integer codeRetour = artisanServiceREST.saveEntrepriseInformation(entrepriseDTO);
        assertNotNull(codeRetour);
        assertEquals(CodeRetourService.RETOUR_KO, codeRetour);
    }

    /**
     * Cas de test : L'utilisateur récupère les informations d'une entreprise dans un but de consultation
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void getEntrepriseInformationBySiretNominal(){
        EntrepriseDTO entrepriseDTO = artisanServiceREST.getEntrepriseInformationBySiret("43394298400017");

        assertNotNull(entrepriseDTO);

        assertEquals("Pebronne enterprise", entrepriseDTO.getNomComplet());
        assertTrue(!entrepriseDTO.getCategoriesMetier().isEmpty());

        assertNotNull(entrepriseDTO.getAdresseEntreprise());
        assertEquals("106 chemin du pébron", entrepriseDTO.getAdresseEntreprise().getAdresse());

        assertFalse(entrepriseDTO.getIsVerifier());
        assertEquals(2, entrepriseDTO.getNotationsDTO().size());

        assertEquals(Double.valueOf("4.0"), entrepriseDTO.getMoyenneAvis());
        assertEquals(Integer.valueOf(3), entrepriseDTO.getNbAnnonce());

        assertNotNull(entrepriseDTO.getPhotosChantiersTemoins());
        assertFalse(entrepriseDTO.getPhotosChantiersTemoins().isEmpty());
    }

    /**
     * Cas de test : L'utilisateur veut voir plus d'avis concernant l'entreprise.
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void getNotationBySiretNominal(){
        List<AvisDTO> notations = artisanServiceREST.getEntrepriseNotationBySiret("43394298400017");
        assertEquals(3, notations.size());
    }

    /**
     * Cas de test : L'artisan cherche a rajouter des photos de chantier témoin
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void ajoutPhotoChantierTemoinNominal(){
        testAjoutPhotoChantier("pebronneArtisanne", 4);
    }

    /**
     * Cas de test : L'admin cherche a rajouter des photos de chantier témoin sur une entreprise.
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void ajoutPhotoChantierTemoinAdmin(){
        testAjoutPhotoChantier("admin", 4);
    }

    /**
     * Cas de test : L'admin cherche a rajouter des photos de chantier témoin sur une entreprise.
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    public void ajoutPhotoChantierTemoinAccesRefuse(){
        testAjoutPhotoChantier("pebronne", 0);
    }

    /**
     * Cas de test : L'artisan cherche a supprimer des photos de chantier témoin sur son entreprise.
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    @ShouldMatchDataSet(value = "datasets/out/suppression_photo_chantier_temoin.yml", excludeColumns = {"id",
            "datemaj", "datecreation", "datenotation", "datenotification", "url", "categoriemetier", "entreprise_fk", "motcle_fk"})
    public void suppressionPhotoChantierTemoinNominal(){
        testSuppressionPhoto("pebronneArtisanne", true);
    }

    /**
     * Cas de test : L'admin cherche a supprimer des photos de chantier témoin sur une entreprise.
     *
     */
    @Test
    @UsingDataSet("datasets/in/entreprises_informations.yml")
    @ShouldMatchDataSet(value = "datasets/out/suppression_photo_chantier_temoin.yml", excludeColumns = {"id",
            "datemaj", "datecreation", "datenotation", "datenotification", "url", "categoriemetier", "entreprise_fk", "motcle_fk"})
    public void suppressionPhotoChantierTemoinAdmin(){
        testSuppressionPhoto("admin", true);
    }

    public void testAjoutPhotoChantier(String login, int nbImageAttendu) {
        // On recupére la photo dans les ressources de la webapp de test
        Properties seleniumProperties = PropertiesFileWS.IMAGE.getProperties();
        StringBuilder adresseToImg = new StringBuilder(seleniumProperties.getProperty("app.temp.img.dir.test"));
        adresseToImg.append("castor.jpg");
        File castorFile = new File(adresseToImg.toString());

        AjoutPhotoDTO ajoutPhotoDTO = new AjoutPhotoDTO();
        ajoutPhotoDTO.setLoginDemandeur(login);
        ajoutPhotoDTO.setId("43394298400017");
        ajoutPhotoDTO.getImages().add(castorFile);

        List<ImageDTO> imageDTOs = artisanServiceREST.ajouterPhotosChantierTemoin(ajoutPhotoDTO);

        Assert.assertNotNull(imageDTOs);
        Assert.assertEquals(nbImageAttendu, imageDTOs.size());
    }

    public List<ImageDTO> testSuppressionPhoto(String loginDemandeur, boolean allowed) {

        List<Image> images = null;
        String siret = "43394298400017";
        ImageDTO imageDTO = new ImageDTO();
        if (allowed) {
            testAjoutPhotoChantier(loginDemandeur, 4);
            //On prends tjr les photos de pebronne pour le cas ou on test avec l'admin
            images = photoService.getImagesBySiretByLoginDemandeur("partenaire", siret, "pebronneArtisanne");
            Assert.assertEquals(4, images.size());
            imageDTO.setUrl(images.get(0).getUrl());
        } else {
            imageDTO.setUrl("https://res.cloudinary.com/lescastors/image/upload/v1427874120/test/zbeod6tici6yrphpco39.jpg");
        }

        SuppressionPhotoDTO suppressionPhotoDTO = new SuppressionPhotoDTO();
        suppressionPhotoDTO.setLoginDemandeur(loginDemandeur);
        suppressionPhotoDTO.setId(siret);
        suppressionPhotoDTO.setImageASupprimer(imageDTO);

        List<ImageDTO> imageDTOs = artisanServiceREST.suppressionPhotoChantierTemoin(suppressionPhotoDTO);
        Assert.assertEquals(3, imageDTOs.size());

        images = photoService.getImagesBySiretByLoginDemandeur("partenaire", siret, "pebronneArtisanne");
        Assert.assertEquals(3, images.size());

        return imageDTOs;
    }


}