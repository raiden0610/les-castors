package fr.castor.web.app.security;

import fr.castor.dto.ClientDTO;
import fr.castor.dto.EntrepriseDTO;
import fr.castor.dto.LoginDTO;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.ws.client.service.ArtisanServiceREST;
import fr.castor.ws.client.service.UtilisateurServiceREST;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Bean permettant la gestion de l'authentification d'un utilisateur
 *
 * @author Casaucau Cyril
 */
@RequestScoped
public class Authentication implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

    @Inject
    private UtilisateurServiceREST utilisateurServiceREST;

    @Inject
    private ArtisanServiceREST artisanServiceREST;

    @Inject
    private RolesUtils rolesUtils;

    private static final String CLIENT_KEY = "client";
    private static final String ENTREPRISE_KEY = "entreprise";

    /**
     * Méthode d'authentification appeler lorsqu'un utilisateur se connecte
     *
     * @param username L'username fournit par l'utilisateur
     * @param password Le password fournit par l'utilisateur
     * @return True si l'utilisateur a fourni le bon couple user / mdp
     */
    public Boolean authenticate(String username, String password) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();

        try {
            if (!subject.isAuthenticated()) {
                subject.login(token);
                subject.getSession(true);
            }

        } catch (UnknownAccountException uae) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Compte inconnu", uae);
            }
            return false;
        } catch (IncorrectCredentialsException ice) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Credential incorrect", ice);
            }
            return false;
        } catch (LockedAccountException lae) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Compte bloqué", lae);
            }
            return false;
        } catch (ExcessiveAttemptsException eae) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Trop d'essai de connexion", eae);
            }
            return false;
        } catch (AuthenticationException ae) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Probleme avec l'authentification", ae);
            }
            return false;
        }

        if (subject.isAuthenticated()) {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setLogin(username);
            ClientDTO client = utilisateurServiceREST.login(loginDTO);
            subject.getSession().setAttribute(CLIENT_KEY, client);

            if (rolesUtils.checkRoles(TypeCompte.ARTISAN)) {
                EntrepriseDTO entrepriseDTO = artisanServiceREST.getEntrepriseInformationByArtisanLogin(client.getLogin());
                subject.getSession().setAttribute(ENTREPRISE_KEY, entrepriseDTO);
            }
        }
        return subject.isAuthenticated();
    }

    /**
     * @return Les informations du client connecté
     */
    public ClientDTO getCurrentUserInfo() {
        return (ClientDTO) SecurityUtils.getSubject().getSession().getAttribute(CLIENT_KEY);
    }

    /**
     * Retourne les informations de l'entreprise si c'est un artisan
     *
     * @return Les informations de l'entreprise
     */
    public EntrepriseDTO getEntrepriseUserInfo() {
        EntrepriseDTO entrepriseDTO = (EntrepriseDTO) SecurityUtils.getSubject().getSession().getAttribute(ENTREPRISE_KEY);

        if (entrepriseDTO == null) {
            return new EntrepriseDTO();
        } else {
            return entrepriseDTO;
        }
    }

    /**
     * Stock en session les informations de l'entreprise de l'artisan
     *
     * @param entrepriseDTO les informations de l'entreprise
     */
    public void setEntrepriseUserInfo(EntrepriseDTO entrepriseDTO) {
        SecurityUtils.getSubject().getSession().setAttribute(ENTREPRISE_KEY, entrepriseDTO);
    }

    /**
     * Stocke en session les informations du client connecté
     *
     * @param clientDTO
     */
    public void setCurrentUserInfo(ClientDTO clientDTO) {
        SecurityUtils.getSubject().getSession().setAttribute(CLIENT_KEY, clientDTO);
    }

    /**
     * Renvoi le role principal de l'utilisateur
     *
     * @return
     * @see TypeCompte
     */
    public TypeCompte getCurrentUserRolePrincipal() {
        return getCurrentUserInfo().getPermissions().get(0).getTypeCompte();
    }

    /**
     * Indique si le client est connecté ou pas.
     *
     * @return
     */
    public boolean isAuthenticated() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

}
