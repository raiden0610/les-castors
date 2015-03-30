package fr.batimen.ws.client;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

import fr.batimen.core.constant.Constant;
import fr.batimen.ws.client.enums.PropertiesFileWsClient;
import fr.batimen.ws.client.ssl.TrustManagerSingleton;

/**
 * Classe qui permet de préparer les requètes qui seront envoyées pour
 * interroger le webservice
 * 
 * @author Casaucau Cyril
 * 
 */
@Singleton
public class WsConnector implements Serializable {

    private static final long serialVersionUID = 4898933306261359715L;

    private final transient Client client;
    private String ipServeur;
    private String portServeur;
    private String nomWs;
    private String nomWsTest;
    private boolean isTest = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(WsConnector.class);

    public WsConnector() {
        getWsProperties();
        // Consomme bcp de ressource pour creer le client, il est thread
        // safe. Voir si ca ne pose pas de probleme de perf de l'avoir mis
        // en singleton
        ClientConfig clientConfig = configSSL();
        // Fix pour le serv d'integration
        clientConfig.getClasses().add(MultiPartWriter.class);
        client = Client.create(clientConfig);
        client.setFollowRedirects(true);
        // Authentification du client
        client.addFilter(new HTTPBasicAuthFilter(Constant.BATIMEN_USERS_WS, Constant.BATIMEN_PWD_WS));
        client.setConnectTimeout(Constant.CONNECT_TIMEOUT);
    }

    /**
     * Permet de requeter le Webservice, toutes les requetes passent en POST
     * 
     * @param controller
     *            Le controlleur que l'on veut interroger (voir WsPath)
     * @param method
     *            La methode que l'on veut interroger (voir WsPath)
     * @param object
     *            l'objet que l'on veut transmettre au WS
     * @return Une chaine de caractere encodée JSON
     */
    public String sendRequestJSON(String controller, String method, Object object) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Construction de la requete ws....");
        }

        WebResource call = generateWebResource(controller, method);

        String json = serializeToJSON(object);

        String reponse = call.accept(MediaType.APPLICATION_JSON_TYPE).header("ACCEPT-CHARSET", "UTF-8")
                .entity(json, MediaType.APPLICATION_JSON_TYPE).post(String.class);

        // Au cas ou : dans le cas d'une requete en get ne pas oublier de faire
        // un response.readentity pour fermer la requete HTTP
        return reponse;
    }

    public String sendRequestWithFile(String controller, String method, List<File> files, Object object) {
        WebResource call = generateWebResource(controller, method);

        String json = serializeToJSON(object);

        FormDataMultiPart form = new FormDataMultiPart();

        FormDataBodyPart jsonContent = new FormDataBodyPart("content", json, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(jsonContent);

        for (File file : files) {
            FileDataBodyPart fileBodyPart = new FileDataBodyPart(file.getName(), file,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);
            form.bodyPart(fileBodyPart);
        }

        String reponse = call.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
                .accept(MediaType.MULTIPART_FORM_DATA).post(String.class, form);

        return reponse;
    }

    private WebResource generateWebResource(String controller, String method) {
        StringBuilder adresseService = new StringBuilder("https://");
        adresseService.append(ipServeur);
        adresseService.append(":");
        adresseService.append(portServeur);
        adresseService.append("/");
        if (isTest) {
            adresseService.append(nomWsTest);
        } else {
            adresseService.append(nomWs);
        }
        adresseService.append("/");
        adresseService.append(controller);
        adresseService.append("/");
        adresseService.append(method);

        return client.resource(adresseService.toString());
    }

    private String serializeToJSON(Object object) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Serialization en json.....");
        }

        // Preparation à la serialization en JSON
        GsonBuilder builder = new GsonBuilder().serializeNulls();
        builder.setPrettyPrinting();
        builder.setDateFormat(DateFormat.LONG);

        builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

            @Override
            public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc) {
                return new JsonPrimitive(t.getTime());
            }
        });

        Gson gson = builder.create();
        String jsonRequest = gson.toJson(object);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Serialization en json.....OK");
        }

        return jsonRequest;

    }

    private void getWsProperties() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Récuperation des properties....");
        }

        Properties wsProperties = PropertiesFileWsClient.WS.getProperties();
        ipServeur = wsProperties.getProperty("ws.ip");
        portServeur = wsProperties.getProperty("ws.port");
        nomWs = wsProperties.getProperty("ws.name");
        nomWsTest = wsProperties.getProperty("ws.name.test.arquillian");
    }

    /**
     * Configure et initialise les differents composants permettant de
     * comuniquer en SSL avec le webservice
     * 
     * @return Le ClientConfig avec le SSL correctement configurer
     */
    private ClientConfig configSSL() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Début de la configuration du SSL (init du context).....");
        }

        SSLContext context = null;

        // On initialise le context avec le bon trust manager qui activera ou
        // non la verification du certificat.
        try {
            context = SSLContext.getInstance("SSLv3");
            context.init(null, TrustManagerSingleton.getTrustedCertificate(), TrustManagerSingleton.secureRandomOrNot());

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Config de client config.....");
            }

            ClientConfig config = new DefaultClientConfig();
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new HTTPSProperties(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }, context));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Config de client config.....OK");
            }

            return config;
        } catch (KeyManagementException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Problème de chargement de certificat", e);
            }
            return null;
        } catch (NoSuchAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Algorithme SSL introuvable", e);
            }
            return null;
        }
    }

    /**
     * @param isTest
     *            the isTest to set
     */
    public void setTest(boolean isTest) {
        this.isTest = isTest;
    }

}
