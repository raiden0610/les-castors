package fr.castor.web.client.extend;

import fr.castor.core.constant.UrlPage;
import fr.castor.web.client.component.ContactezNous;
import fr.castor.web.client.master.MasterPage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

/**
 * Page nos engagements
 *
 * @author Casaucau  Cyril
 */
public class NosEngagements extends MasterPage {

    public NosEngagements() {
        super("Nos engagements", "Engagement qualité entreprise artisan particulier", "Nos engagements", true, "img/bg_title1.jpg");

        Link contactezNousArtisan = new Link("contactezNousArtisan") {
            @Override
            public void onClick() {
                setResponsePage(Contact.class);
            }
        };

        Link contactezNousClient = new Link("contactezNousClient") {
            @Override
            public void onClick() {
                setResponsePage(Contact.class);
            }
        };

        ContactezNous contactezNousComposant = new ContactezNous("contactezNous");

        add(contactezNousComposant, contactezNousArtisan, contactezNousClient);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        StringBuilder adresseEngagements = new StringBuilder();
        adresseEngagements.append("https://lescastors.fr").append(UrlPage.NOS_ENGAGEMENTS);

        //Opengraph tags
        response.render(addOpenGraphMetaResourcesToHeader("og:url", adresseEngagements.toString()));
        response.render(addOpenGraphMetaResourcesToHeader("og:type", "website"));
        response.render(addOpenGraphMetaResourcesToHeader("og:title", "Les engagements du site lescastors.fr"));
        response.render(addOpenGraphMetaResourcesToHeader("og:description", "Plus d'informations sur nos engagements qualités"));
        response.render(addOpenGraphMetaResourcesToHeader("og:image", UrlPage.LOGO));
    }

}