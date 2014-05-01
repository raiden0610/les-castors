package fr.batimen.web.selenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Classe abstraite permettant de mettre en place les tests d'integration avec
 * selenium
 * 
 * @author Casaucau Cyril
 * 
 */
public abstract class AbstractSeleniumTest {

	protected WebDriver driver;
	protected String appUrl;
	protected boolean acceptNextAlert = true;
	protected StringBuilder verificationErrors = new StringBuilder();
	private String ipServeur;
	private String portServeur;
	private String nomApp;

	public final static String BON_MOT_DE_PASSE = "lollollol";
	public final static String MAUVAIS_MOT_DE_PASSE = "kikoulolmauvais";
	public final static int TEMPS_ATTENTE_AJAX = 20;

	private static final Logger logger = LoggerFactory.getLogger(AbstractSeleniumTest.class);

	@Before
	public void setUp() throws Exception {

		Properties wsProperties = new Properties();
		try {
			wsProperties.load(getClass().getClassLoader().getResourceAsStream("selenium.properties"));
			ipServeur = wsProperties.getProperty("app.ip");
			portServeur = wsProperties.getProperty("app.port");
			nomApp = wsProperties.getProperty("app.name");
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Erreur de récupération des properties de l'application web : " + e.getMessage());
			}
		}

		StringBuilder sbUrlApp = new StringBuilder("https://");
		sbUrlApp.append(ipServeur);
		sbUrlApp.append(":");
		sbUrlApp.append(portServeur);
		sbUrlApp.append("/");
		sbUrlApp.append(nomApp);

		// System.setProperty("webdriver.chrome.driver",
		// "C:\\selenium\\chromedriver.exe");
		driver = new FirefoxDriver();
		appUrl = sbUrlApp.toString();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);

	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	protected boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	protected boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	protected String closeAlertAndGetItsText() {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}

	protected void connexionApplication(String password) {
		driver.findElement(By.id("connexionlbl")).click();
		Boolean checkCondition = (new WebDriverWait(driver, 5)).until(ExpectedConditions
		        .textToBePresentInElementLocated(By.id("ui-id-1"), "Connexion à l'espace client / artisan"));
		assertTrue(checkCondition);
		driver.findElement(By.name("login")).click();
		driver.findElement(By.name("login")).clear();
		driver.findElement(By.name("login")).sendKeys("raiden");
		driver.findElement(By.name("password")).click();
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys(password);
		driver.findElement(By.id("signInButton")).click();
	}

}
