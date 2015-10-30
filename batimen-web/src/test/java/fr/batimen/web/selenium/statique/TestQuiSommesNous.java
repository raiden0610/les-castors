package fr.batimen.web.selenium.statique;

import fr.batimen.core.constant.UrlPage;
import fr.batimen.web.selenium.common.AbstractITTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test selenium de la page qui sommes nous ?
 *
 * @author Casaucau Cyril
 */
public class TestQuiSommesNous extends AbstractITTest {
    @Override
    public void prepareDB() throws Exception {

    }

    @Test
    public void testQuiSommesNousNominale() {
        driver.get(appUrl + UrlPage.ACCUEIL_URL);
        driver.findElement(By.linkText("Qui sommes nous?")).click();
        Boolean checkCondition = (new WebDriverWait(driver, AbstractITTest.TEMPS_ATTENTE_AJAX))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h1.title"),
                        "QUI SOMMES NOUS ?"));
        Assert.assertTrue(checkCondition);
    }
}
