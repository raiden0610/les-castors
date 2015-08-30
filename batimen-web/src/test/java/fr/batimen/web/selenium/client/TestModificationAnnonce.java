package fr.batimen.web.selenium.client;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.operation.Operation;
import fr.batimen.core.constant.UrlPage;
import fr.batimen.web.selenium.common.AbstractITTest;
import fr.batimen.web.utils.UtilsSelenium;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static fr.batimen.web.selenium.dataset.AnnonceDataset.*;
import static org.junit.Assert.assertTrue;

/**
 * Test Selenium concernant la page de modification d'une annonce.
 *
 * @author Casaucau Cyril.
 *         <p>
 *         Created by Casaucau on 20/04/2015.
 */
public class TestModificationAnnonce extends AbstractITTest {

    @Override
    public void prepareDB() throws Exception {
        Operation operation = sequenceOf(DELETE_ALL, INSERT_USER_DATA, INSERT_USER_PERMISSION, INSERT_ADRESSE_DATA,
                INSERT_ENTREPRISE_DATA, INSERT_ARTISAN_DATA, INSERT_ARTISAN_PERMISSION, INSERT_AVIS_DATA,
                INSERT_ANNONCE_DATA, INSERT_NOTIFICATION_DATA, INSERT_ANNONCE_ARTISAN, INSERT_ANNONCE_IMAGE,
                INSERT_ANNONCE_MOT_CLE, INSERT_CATEGORIE_METIER);
        DbSetup dbSetup = new DbSetup(getDriverManagerDestination(), operation);
        dbSetup.launch();
    }

    @Before
    public void connectToAnnonceBeforeModification() {
        driver.get(appUrl);
        connexionApplication("raiden", AbstractITTest.BON_MOT_DE_PASSE, Boolean.TRUE);
        // On calcul l'url d'accés direct à l'annonce.
        StringBuilder appUrlAnnonce = new StringBuilder(appUrl);
        appUrlAnnonce.append(UrlPage.ANNONCE).append("?idAnnonce=").append("toto");
        driver.get(appUrlAnnonce.toString());
    }


    /**
     * Cas de test : Le client accede a son annonce en se connectant a les
     * castors, puis modifie les données de l'annonce
     */
    @Test
    public void testModificationAnnonceNominale() {
        driver.findElement(By.linkText("Modifier votre annonce")).click();
        driver.findElement(By.id("motCleField")).sendKeys("Piscine");
        driver.findElement(By.linkText("Piscine")).click();
        driver.findElement(By.id("radioTypeTravauxRenovation")).click();
        driver.findElement(By.id("typeTravauxRenovation")).click();
        driver.findElement(By.id("validateQualification")).click();
        Boolean checkUntilModifOK = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.box_type4"),
                        "Votre annonce a été modifiée avec succés !"));
        Assert.assertTrue(checkUntilModifOK);
        Boolean checkMotClePresent = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("div.informationAnnonce"),
                        "Salles de bain"));
        assertTrue(checkMotClePresent);
    }

    /**
     * Cas de test : Le client accede a son annonce en se connectant a les
     * castors, puis ne modifie pas les données de son annonce et clique sur le bouton revenir à l'annonce.
     */
    @Test
    public void testModificationAnnonceRetourAnnonce() {
        driver.findElement(By.linkText("Modifier votre annonce")).click();
        driver.findElement(By.id("etapePrecedente3")).click();
        WebElement checkUntilBackToAnnonce = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.headInModule")));
        Assert.assertNotNull(checkUntilBackToAnnonce);
    }

    /**
     * Cas de test : Le client accede a son annonce en se connectant a les
     * castors, puis rajoute une photo, tout doit bien se passer.
     */
    @Test
    public void testAjoutPhotoModificationAnnonce() throws InterruptedException {
        UtilsSelenium.testAjoutPhotoIT(driver, browser, true);
    }

    /**
     * Cas de test : Le client accede a son annonce en se connectant a les
     * castors, puis rajoute une photo, puis la supprime
     */
    @Test
    public void testSuppressionPhotoModificationAnnonce() throws InterruptedException {
        UtilsSelenium.testAjoutPhotoIT(driver, browser, true);
        UtilsSelenium.suppressionPhotoIT(driver);
    }

}