package fr.castor.web.selenium.client;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.operation.Operation;
import fr.castor.web.selenium.common.AbstractITTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static fr.castor.web.selenium.dataset.ModifierMonProfilDataset.*;
import static org.junit.Assert.assertTrue;

/**
 * Test d'intégration pour la page de modification d'un profil utilisateur <br/>
 * L'utilisateur se connecte à l'application et se dirige vers la page de
 * modification qui se trouve dans son profil
 *
 * @author Casaucau Cyril
 */
public class TestModifierMonProfil extends AbstractITTest {

    @Override
    public void prepareDB() throws Exception {
        Operation operation = sequenceOf(DELETE_ALL, INSERT_USER_DATA, INSERT_USER_PERMISSION,
                INSERT_ADRESSE_DATA, INSERT_ENTREPRISE_DATA,
                INSERT_ARTISAN_DATA, INSERT_ARTISAN_PERMISSION, INSERT_AVIS_DATA,
                INSERT_ANNONCE_DATA, INSERT_NOTIFICATION_DATA, INSERT_ANNONCE_ARTISAN);
        DbSetup dbSetup = new DbSetup(getDriverManagerDestination(), operation);
        dbSetup.launch();
    }

    /**
     * Cas de test : L'utilisateur modifie son login grace au formulaire de
     * modification des informations.
     */
    @Test
    public void modifyInformationsProfilInitial() {
        modifyInformationsProfilInformation("raiden", "raiden06", "0512458596", "Vos données ont bien été mises à jour", false);
    }

    /**
     * Cas de test : l'utilisateur modifie de maniere correcte son mot de passe
     */
    @Test
    public void modifyInformationsProfilPasswordInformation() {
        driver.get(appUrl);
        connexionApplication("raiden", BON_MOT_DE_PASSE, Boolean.TRUE);
        findElement(By.id("connexionlbl")).click();
        findElement(By.linkText("Modifier le profil")).click();
        assertModificationPage();
        modificationMotDePasse("lollollol");
        Boolean checkConditionModificationInformation = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.box_type4"),
                        "Vos données ont bien été mises à jour"));
        assertTrue(checkConditionModificationInformation);
    }

    /**
     * Cas de test : l'utilisateur modifie de maniere incorrecte son mot de
     * passe, l'appli lui signale
     */
    @Test
    public void modifyInformationsProfilPasswordInformationWrong() {
        driver.get(appUrl);
        connexionApplication("raiden", BON_MOT_DE_PASSE, Boolean.TRUE);
        findElement(By.id("connexionlbl")).click();
        findElement(By.linkText("Modifier le profil")).click();
        assertModificationPage();
        modificationMotDePasse("lollol");
        Boolean checkConditionModificationInformation = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.box_type6"),
                        "Ancien mot de passe invalide"));
        assertTrue(checkConditionModificationInformation);
    }

    /**
     * Cas de test : L'artisan modifie son login grace au formulaire de
     * modification des informations.
     */
    @Test
    public void modifyInformationsProfilInitialArtisan() {
        modifyInformationsProfilInformation("pebron", "pebron06", "0512458596", "Vos données ont bien été mises à jour", true);
    }


    private void modifyInformationsProfilInformation(String login, String loginVoulu, String numeroTel, String messageAttendu, boolean isArtisan) {
        driver.get(appUrl);
        connexionApplication(login, BON_MOT_DE_PASSE, Boolean.TRUE);
        findElement(By.id("connexionlbl")).click();
        if (isArtisan) {
            findElement(By.linkText("Modifier mes informations")).click();
        } else {
            findElement(By.linkText("Modifier le profil")).click();
        }

        assertModificationPage();

        findElement(By.id("login")).clear();
        findElement(By.id("login")).sendKeys(loginVoulu);
        findElement(By.id("numeroTel")).clear();
        findElement(By.id("numeroTel")).sendKeys(numeroTel);
        findElement(By.id("validateInscription")).click();
        Boolean checkConditionModificationInformation = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("span.box_type4"),
                        messageAttendu));
        assertTrue(checkConditionModificationInformation);
    }

    private void modificationMotDePasse(String motDePasse) {
        findElement(By.id("oldpassword")).clear();
        findElement(By.id("oldpassword")).sendKeys(motDePasse);
        findElement(By.id("password")).clear();
        findElement(By.id("password")).sendKeys("lollollol06");
        findElement(By.id("confirmPassword")).clear();
        findElement(By.id("confirmPassword")).sendKeys("lollollol06");
        findElement(By.id("validateInscription")).click();
    }

    private void assertModificationPage() {
        Boolean checkConditionModifierMonProfilPresent = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4.headInModule"),
                        "MODIFIER MON PROFIL"));
        assertTrue(checkConditionModifierMonProfilPresent);
    }
}
