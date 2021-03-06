package fr.castor.test.ws;

import fr.castor.ws.client.WsConnector;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupStrategy;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@RunWith(Arquillian.class)
//@ApplyScriptBefore(value = "datasets/cleanup/before.sql")
@Cleanup(phase = TestExecutionPhase.BEFORE, strategy = CleanupStrategy.STRICT)
public abstract class AbstractBatimenWsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBatimenWsTest.class);
    @Rule
    public TestRule watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            LOGGER.info("Début Test : " + description.getDisplayName());
        }

        @Override
        protected void finished(Description description) {
            LOGGER.info("Fin Test : " + description.getDisplayName());
        }
    };
    @Inject
    private WsConnector wsConnector;

    @Deployment
    public static WebArchive createTestArchive() {

        MavenResolverSystem resolver = Maven.resolver();

        // Creation de l'archive
        WebArchive batimenWsTest = ShrinkWrap.create(WebArchive.class, "castor-ws-test.war");

        // Ajout des dépendences
        batimenWsTest
                .addPackages(true, "fr/castor/ws/dao")
                .addPackages(true, "fr/castor/ws/entity")
                .addPackages(true, "fr/castor/ws/enums")
                .addPackages(true, "fr/castor/ws/facade")
                .addPackages(true, "fr/castor/ws/helper")
                .addPackages(true, "fr/castor/ws/interceptor")
                .addPackages(true, "fr/castor/ws/quartz")
                .addPackages(true, "fr/castor/ws/service")
                .addPackages(true, "fr/castor/ws/utils")
                .addPackages(true, "fr/castor/ws/quartz")
                .addPackages(true, "fr/castor/ws/mapper")
                .addPackages(true, "fr/castor/test/ws")
                .addAsLibraries(
                        resolver.loadPomFromFile("pom.xml").importCompileAndRuntimeDependencies()
                                .resolve("fr.castor.app:castor-client").withTransitivity().asFile());
        // Seul dependence a specifier car elle ne fait pas partie du
        // pom ws
        //

        // Ajout des ressources
        batimenWsTest.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("logback-test.xml", "classes/logback.xml")
                .addAsWebInfResource("jboss-web.xml", "jboss-web.xml").setWebXML("web.xml")
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsResource("email-test.properties", "email.properties")
                .addAsResource("url.properties", "url.properties")
                .addAsResource("castor.properties", "castor.properties")
                .addAsResource("jobs.properties", "jobs.properties")
                .addAsResource("quartz.properties", "quartz.properties")
                .addAsResource("image.properties", "image.properties");

        return batimenWsTest;
    }

    @Before
    public void initWsConnectorForTest() {
        wsConnector.setTest(true);
    }

}
