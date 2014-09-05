package fr.batimen.web.selenium;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.batimen.web.utils.UtilsSelenium;

public class TestNouveauPartenaire extends AbstractITTest {

    private final String nouveauPartenaireURL = "/nouveaupartenaire/";

    @Override
    public void prepareDB() throws Exception {
        Operation operation = sequenceOf(DELETE_ALL);
        DbSetup dbSetup = new DbSetup(getDriverManagerDestination(), operation);
        dbSetup.launch();
    }

    @Test
    public void testInscriptioNouveauPartenaireNominal() {
        driver.get(appUrl + nouveauPartenaireURL);
        // On selectionne un departement
        UtilsSelenium.selectionDepartement(driver);

        new Select(driver.findElement(By.id("civilite"))).selectByVisibleText("Monsieur");
        driver.findElement(By.id("nom")).clear();
        driver.findElement(By.id("nom")).sendKeys("Dupont");
        driver.findElement(By.id("prenomField")).clear();
        driver.findElement(By.id("prenomField")).sendKeys("Xavier");
        driver.findElement(By.id("numeroTelField")).clear();
        driver.findElement(By.id("numeroTelField")).sendKeys("0493854578");
        driver.findElement(By.id("emailField")).clear();
        driver.findElement(By.id("emailField")).sendKeys("xavier.dupont@entreprise.com");
        driver.findElement(By.id("logintField")).clear();
        driver.findElement(By.id("logintField")).sendKeys("xavier06");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("lolmdr06");
        driver.findElement(By.id("confirmPassword")).clear();
        driver.findElement(By.id("confirmPassword")).sendKeys("lolmdr06");
        driver.findElement(By.id("validateEtape2Partenaire")).click();
        driver.findElement(By.id("nomComplet")).clear();
        driver.findElement(By.id("nomComplet")).sendKeys("Xav Entreprise");
        new Select(driver.findElement(By.id("statutJuridique"))).selectByVisibleText("SARL");
        driver.findElement(By.id("nbEmployeField")).clear();
        driver.findElement(By.id("nbEmployeField")).sendKeys("5");
        driver.findElement(By.id("dateCreationField")).click();
        driver.findElement(By.id("dateCreationField")).click();
        driver.findElement(By.linkText("1")).click();
        driver.findElement(By.id("siretField")).clear();
        driver.findElement(By.id("siretField")).sendKeys("43394298400017");
        driver.findElement(By.id("adresseField")).clear();
        driver.findElement(By.id("adresseField")).sendKeys("450 chemin du xav");
        driver.findElement(By.id("codePostalField")).clear();
        driver.findElement(By.id("codePostalField")).sendKeys("06800");
        driver.findElement(By.id("villeField")).clear();
        driver.findElement(By.id("villeField")).sendKeys("Xavier City");
        driver.findElement(By.cssSelector("#ajouterActivite > span")).click();
        Boolean checkCondition = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#ajouterActivite > span"),
                        "Faites votre choix"));
        assertTrue(checkCondition);
        new Select(driver.findElement(By.id("activiteField"))).selectByVisibleText("Décoration / Maçonnerie");
        driver.findElement(By.id("enregistrerCategorie")).click();
        driver.findElement(By.id("validateEtape3Partenaire")).click();

    }

}
