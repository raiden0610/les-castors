package fr.castor.web.client.extend.member.client;

import fr.castor.dto.AnnonceDTO;
import fr.castor.dto.DemandeMesAnnoncesDTO;
import fr.castor.dto.NotificationDTO;
import fr.castor.dto.aggregate.MesAnnoncesAnnonceDTO;
import fr.castor.dto.aggregate.MesAnnoncesNotificationDTO;
import fr.castor.dto.enums.TypeCompte;
import fr.castor.web.app.constants.JSConstant;
import fr.castor.web.app.constants.ParamsConstant;
import fr.castor.web.app.enums.FeedbackMessageLevel;
import fr.castor.web.app.security.Authentication;
import fr.castor.web.app.security.RolesUtils;
import fr.castor.web.client.component.Commentaire;
import fr.castor.web.client.component.ContactezNous;
import fr.castor.web.client.component.LinkLabel;
import fr.castor.web.client.component.Profil;
import fr.castor.web.client.extend.connected.Annonce;
import fr.castor.web.client.extend.connected.Entreprise;
import fr.castor.web.client.extend.connected.RechercheAnnonce;
import fr.castor.web.client.extend.nouveau.devis.NouveauDevis;
import fr.castor.web.client.master.MasterPage;
import fr.castor.ws.client.service.UtilisateurServiceREST;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Page ou l'utilisateur pourra consulter son compte ainsi que l'avancement de
 * ces differents boulot/devis/notes, etc
 *
 * @author Casaucau Cyril
 */

public final class MesAnnonces extends MasterPage {

    public static final Integer NB_ANNONCE_PAR_PAGE = 3;
    public static final Integer NB_NOTIFICATION_PAR_PAGE = 3;

    private static final long serialVersionUID = 1902734649854998120L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MesAnnonces.class);

    @Inject
    private UtilisateurServiceREST utilisateurServiceREST;
    @Inject
    private Authentication authentication;
    @Inject
    private RolesUtils rolesUtils;

    private List<AnnonceDTO> annonces = new ArrayList<>();
    private List<NotificationDTO> notifications = new ArrayList<>();
    private Long nbAnnonceTotaleValeur, nbNotificationTotaleValeur;
    private Label infosNbAnnonce;
    private Label nbNotification;
    private Model<String> voirAnnonceModel;
    private Model<String> demandeDeDevisTitleModel;
    private Model<String> pasDeNotificationModel;
    private WebMarkupContainer annoncesContainer;
    private WebMarkupContainer notificationsContainer;
    private AjaxLink<Void> afficherAnciennesAnnonces;
    private AjaxLink<Void> afficherAnciennesNotifications;

    public MesAnnonces() {
        super("Mes annonces ", "", "Bienvenue sur lescastors.fr", true, "img/bg_title1.jpg");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init de la page mes annonces");
        }

        calculModelLabelByTypeCompte();
        initStaticComposant();
        loadMesAnnonces();
        loadMesNotifications();
        initRepeaterNotifications();
        initRepeaterAnnonces();
        initMesActionsLink();
        afficherAnciennesAnnonces();
        afficherAnciennesNotifications();
        
        this.setOutputMarkupId(true);
    }

    public MesAnnonces(String messageToFeedBack, FeedbackMessageLevel messageLevel) {
        this();
        this.feedBackPanelGeneral.sendMessage(messageToFeedBack, messageLevel);
    }

    private void initMesActionsLink() {

        String valeurModel = null;

        if (rolesUtils.checkRoles(TypeCompte.CLIENT)) {
            valeurModel = "Poster une annonce";
        } else {
            valeurModel = "Rechercher une annonce";
        }

        Model<String> modelActionLink = new Model<>(valeurModel);

        Link<String> actionLink = new Link<String>("actionLink") {
            @Override
            public void onClick() {
                if (rolesUtils.checkRoles(TypeCompte.CLIENT)) {
                    setResponsePage(NouveauDevis.class);
                } else if (rolesUtils.checkRoles(TypeCompte.ARTISAN) || rolesUtils.checkRoles(TypeCompte.ARTISAN_PREMIUM)) {
                    setResponsePage(RechercheAnnonce.class);
                }
            }
        };

        WebMarkupContainer i = new WebMarkupContainer("icon"){
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (rolesUtils.checkRoles(TypeCompte.ARTISAN) || rolesUtils.checkRoles(TypeCompte.ARTISAN_PREMIUM)) {
                    tag.put("class", "fa fa-search");
                } else if (rolesUtils.checkRoles(TypeCompte.CLIENT)){
                    tag.put("class", "fa fa-plus-circle");
                }
            }
        };

        Label labelActionLink = new Label("labelActionLink", modelActionLink);

        actionLink.add(i, labelActionLink);

        add(actionLink);
    }

    private void initStaticComposant() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init des composants statiques");
        }

        Profil profil = new Profil("profil", false);
        ContactezNous contactezNous = new ContactezNous("contactezNous");
        Commentaire commentaire = new Commentaire("commentaire");

        Label demandeDeDevisTitle = new Label("demandeDeDevisTitle", demandeDeDevisTitleModel);
        Label pasDeNotificationLbl = new Label("pasDeNotificationLbl", pasDeNotificationModel) {
            @Override
            public boolean isVisible() {
                return notifications.isEmpty();
            }
        };

        add(profil, commentaire, contactezNous, demandeDeDevisTitle, pasDeNotificationLbl);
    }

    private void initRepeaterNotifications() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init du repeater des notifications");
        }

        nbNotification = new Label("nbNotification", nbNotificationTotaleValeur);
        nbNotification.setOutputMarkupId(true);
        nbNotification.setMarkupId("nbNotification");

        ListView<NotificationDTO> listViewNotification = new ListView<NotificationDTO>("listNotification",
                notifications) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<NotificationDTO> item) {
                final NotificationDTO notification = item.getModelObject();

                final Model<String> parQuiModel = new Model<>(parQui(notification));

                LinkLabel linkParQui = new LinkLabel("linkParQui", parQuiModel) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        onclickParQuiLink(notification);
                    }
                };

                StringBuilder contenuNotification = new StringBuilder(" ");
                contenuNotification.append(notification.getTypeNotification().getAffichage()).append(" ");
                Label typeNotification = new Label("typeNotification", contenuNotification.toString());

                final Model<String> objetNotification = new Model<>(getObjetNotification(notification));

                LinkLabel linkObjetNotification = new LinkLabel("linkObjetNotification", objetNotification) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        onclickObjetNotification(notification);
                    }
                };

                linkObjetNotification.setOutputMarkupId(true);

                SimpleDateFormat dateNotificationFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Label dateNotification = new Label("dateNotification", dateNotificationFormatter.format(notification
                        .getDateNotification()));

                item.add(linkParQui);
                item.add(typeNotification);
                item.add(linkObjetNotification);
                item.add(dateNotification);
            }
        };

        notificationsContainer = new WebMarkupContainer("notificationsContainer");
        notificationsContainer.setOutputMarkupId(true);
        notificationsContainer.setMarkupId("notificationsContainer");
        notificationsContainer.add(listViewNotification);

        this.add(nbNotification);
        this.add(notificationsContainer);
    }

    private void initRepeaterAnnonces() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Init du repeater des annonces");
        }

        //Pas afficher si c'est un artisan
        Label nbDevisHeader = new Label("nbDevisHeader", "Nb devis") {

            @Override
            public boolean isVisible() {
                return rolesUtils.checkRoles(TypeCompte.CLIENT);
            }
        };

        Label nbAnnonce = new Label("nbAnnonce", nbAnnonceTotaleValeur);

        ListView<AnnonceDTO> listViewAnnonce = new ListView<AnnonceDTO>("listAnnonce", annonces) {
            private static final long serialVersionUID = 9041719964383711900L;

            @Override
            protected void populateItem(ListItem<AnnonceDTO> item) {
                final AnnonceDTO annonce = item.getModelObject();

                StringBuilder descriptionCutting = new StringBuilder();
                if (annonce.getDescription().length() > 30) {
                    descriptionCutting.append(annonce.getDescription().substring(0, 30));
                    descriptionCutting.append("...");
                } else {
                    descriptionCutting.append(annonce.getDescription());
                }

                Label description = new Label("description", descriptionCutting.toString());

                WebMarkupContainer progressBar = new WebMarkupContainer("progressBar");
                StringBuilder sizeCSSProgressBar = new StringBuilder("width: ");
                sizeCSSProgressBar.append(annonce.getEtatAnnonce().getPercentage()).append(";");
                progressBar.add(new AttributeModifier("style", sizeCSSProgressBar.toString()));

                Label nbDevis = new Label("nbDevis", annonce.getNbDevis()) {

                    @Override
                    public boolean isVisible() {
                        return rolesUtils.checkRoles(TypeCompte.CLIENT);
                    }
                };
                Label etatAnnonce = new Label("etatAnnonce", annonce.getEtatAnnonce().getType());

                LinkLabel voirAnnonce = new LinkLabel("voirAnnonce", voirAnnonceModel) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        PageParameters params = new PageParameters();
                        params.add(ParamsConstant.ID_ANNONCE_PARAM, annonce.getHashID());
                        this.setResponsePage(Annonce.class, params);
                    }
                };

                voirAnnonce.setOutputMarkupId(true);

                item.add(new AjaxEventBehavior("onclick") {
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        PageParameters params = new PageParameters();
                        params.add(ParamsConstant.ID_ANNONCE_PARAM, annonce.getHashID());
                        setResponsePage(Annonce.class, params);
                    }
                });
                item.add(description, nbDevis, etatAnnonce, voirAnnonce, progressBar);
            }
        };

        annoncesContainer = new WebMarkupContainer("annoncesContainer");
        annoncesContainer.setOutputMarkupId(true);
        annoncesContainer.setMarkupId("annoncesContainer");
        annoncesContainer.add(listViewAnnonce, nbDevisHeader);

        add(nbAnnonce, annoncesContainer);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JSConstant.fontAwesome);
    }

    private void afficherAnciennesAnnonces() {
        afficherAnciennesAnnonces = new AjaxLink<Void>("afficherAnciennesAnnonces") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                loadMesAnnonces();
                updateModelInfoNbAnnonce();
                target.add(annoncesContainer, infosNbAnnonce, afficherAnciennesAnnonces);
            }

            @Override
            public boolean isVisible() {
                return !annonces.isEmpty() && annonces.size() != nbAnnonceTotaleValeur;
            }
        };

        afficherAnciennesAnnonces.setOutputMarkupId(true);
        afficherAnciennesAnnonces.setMarkupId("afficherAnciennesAnnonces");

        infosNbAnnonce = new Label("infosNbAnnonce", new Model<>());
        infosNbAnnonce.setOutputMarkupId(true);
        infosNbAnnonce.setMarkupId("infosNbAnnonce");

        updateModelInfoNbAnnonce();

        add(afficherAnciennesAnnonces, infosNbAnnonce);
    }

    private void afficherAnciennesNotifications() {
        afficherAnciennesNotifications = new AjaxLink<Void>("afficherAnciennesNotifications") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                loadMesNotifications();
                target.add(notificationsContainer, afficherAnciennesNotifications, nbNotification);
            }

            @Override
            public boolean isVisible() {
                return !notifications.isEmpty() && notifications.size() != nbNotificationTotaleValeur;
            }
        };

        afficherAnciennesNotifications.setOutputMarkupId(true);
        afficherAnciennesNotifications.setMarkupId("afficherAnciennesNotifications");

        add(afficherAnciennesNotifications);
    }

    private void loadMesAnnonces() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Appel webservice pour la récupération des données à afficher (Annonces)");
        }
        DemandeMesAnnoncesDTO demandeMesAnnoncesDTO = computeDemandeMesAnnoncesDTO();

        MesAnnoncesAnnonceDTO mesAnnonces = utilisateurServiceREST.getAnnonceForMesAnnonces(demandeMesAnnoncesDTO);
        annonces.addAll(mesAnnonces.getAnnonces());
        nbAnnonceTotaleValeur = mesAnnonces.getNbTotalAnnonces();
    }

    private void loadMesNotifications() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Appel webservice pour la récupération des données à afficher (Notifications");
        }
        DemandeMesAnnoncesDTO demandeMesAnnoncesDTO = computeDemandeMesAnnoncesDTO();

        MesAnnoncesNotificationDTO mesNotifications = utilisateurServiceREST.getNotificationForMesAnnonces(demandeMesAnnoncesDTO);
        notifications.addAll(mesNotifications.getNotifications());
        nbNotificationTotaleValeur = mesNotifications.getNbTotalNotifications();
    }

    private DemandeMesAnnoncesDTO computeDemandeMesAnnoncesDTO() {
        DemandeMesAnnoncesDTO demandeMesAnnoncesDTO = new DemandeMesAnnoncesDTO();
        demandeMesAnnoncesDTO.setLoginDemandeur(authentication.getCurrentUserInfo().getLogin());
        demandeMesAnnoncesDTO.setLogin(authentication.getCurrentUserInfo().getLogin());
        demandeMesAnnoncesDTO.setRangeAnnoncesDebut(annonces.size());
        demandeMesAnnoncesDTO.setRangeAnnonceFin(NB_ANNONCE_PAR_PAGE);

        demandeMesAnnoncesDTO.setRangeNotificationsDebut(notifications.size());
        demandeMesAnnoncesDTO.setRangeNotificationsFin(NB_NOTIFICATION_PAR_PAGE);

        return demandeMesAnnoncesDTO;
    }

    private String parQui(NotificationDTO notificationDTO) {
        if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.ARTISAN)) {
            return notificationDTO.getNomEntreprise();
        } else if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.CLIENT)) {
            return notificationDTO.getClientLogin();
        }
        return "";
    }

    private void onclickParQuiLink(NotificationDTO notificationDTO) {
        if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.ARTISAN)) {
            PageParameters params = new PageParameters();
            params.add(ParamsConstant.ID_ENTREPRISE_PARAM, notificationDTO.getSiret());
            this.setResponsePage(Entreprise.class, params);
        } else if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.CLIENT)) {
            PageParameters parameters = new PageParameters();
            parameters.add("login", notificationDTO.getClientLogin());
            this.setResponsePage(MonProfil.class, parameters);
        }
    }

    private String getObjetNotification(NotificationDTO notificationDTO) {
        if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.ARTISAN)) {
            return "annonce";
        } else if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.CLIENT)) {
            return "entreprise";
        } else {
            return "";
        }
    }

    private void onclickObjetNotification(NotificationDTO notificationDTO) {
        if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.ARTISAN)) {
            PageParameters params = new PageParameters();
            params.add(ParamsConstant.ID_ANNONCE_PARAM, notificationDTO.getHashIDAnnonce());
            this.setResponsePage(Annonce.class, params);
        } else if (notificationDTO.getTypeNotification().getParQui().equals(TypeCompte.CLIENT)) {
            PageParameters params = new PageParameters();
            params.add(ParamsConstant.ID_ENTREPRISE_PARAM, notificationDTO.getSiret());
            this.setResponsePage(Entreprise.class, params);
        }
    }

    private void calculModelLabelByTypeCompte() {
        demandeDeDevisTitleModel = new Model<>();
        voirAnnonceModel = new Model<>();
        pasDeNotificationModel = new Model<>();
        if (rolesUtils.checkRoles(TypeCompte.ARTISAN)) {
            demandeDeDevisTitleModel.setObject("Les annonces où je suis inscris");
            voirAnnonceModel.setObject("Voir annonce");
            pasDeNotificationModel.setObject("Retrouvez ici toutes les notifications des annonces où vous êtes inscrit");
        } else if (rolesUtils.checkRoles(TypeCompte.CLIENT)) {
            demandeDeDevisTitleModel.setObject("Mes annonces");
            voirAnnonceModel.setObject("Voir / modifier annonce");
            pasDeNotificationModel.setObject("Retrouvez ici toutes les notifications sur vos demandes de devis");
        }
    }

    private void updateModelInfoNbAnnonce() {
        StringBuilder infoNbAnnonceValeur = new StringBuilder();
        infoNbAnnonceValeur.append(annonces.size()).append(" annonce(s) affichée(s) sur ").append(nbAnnonceTotaleValeur);
        infosNbAnnonce.setDefaultModelObject(infoNbAnnonceValeur.toString());
    }
}