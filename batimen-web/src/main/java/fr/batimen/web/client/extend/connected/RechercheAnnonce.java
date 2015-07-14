package fr.batimen.web.client.extend.connected;

import fr.batimen.dto.aggregate.SearchAnnonceDTO;
import fr.batimen.web.app.security.Authentication;
import fr.batimen.web.client.component.CastorDatePicker;
import fr.batimen.web.client.component.Commentaire;
import fr.batimen.web.client.component.ContactezNous;
import fr.batimen.web.client.component.Profil;
import fr.batimen.web.client.master.MasterPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.DateValidator;

import javax.inject.Inject;
import java.util.Date;

/**
 * Page de recherche des annonce, destinée au Artisan
 *
 * @author Casaucau Cyril
 */
public class RechercheAnnonce extends MasterPage {

    @Inject
    private Authentication authentication;

    private CastorDatePicker castorDatePicker;

    private static final  String REFRESH_TOOLTIP_HELP_SEARCH = "$('#helper-search').tooltip()";

    public RechercheAnnonce() {
        super("", "", "Recherche d'annonce", true, "img/bg_title1.jpg");
        initComposants();
        initVosCriteres();
    }

    public RechercheAnnonce(PageParameters params) {
        this();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssContentHeaderItem.forUrl("css/font_icons8.css"));
        response.render(CssHeaderItem.forUrl("//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css"));

        //FuelUX Librairie
        response.render(JavaScriptHeaderItem.forUrl("//www.fuelcdn.com/fuelux/2.6.1/loader.min.js"));
        response.render(CssContentHeaderItem.forUrl("//www.fuelcdn.com/fuelux/2.6.1/css/fuelux.min.css"));
        response.render(CssContentHeaderItem.forUrl("//www.fuelcdn.com/fuelux/2.6.1/css/fuelux-responsive.css"));
        //Datepicker, on ne le mets pas dans le composant car probleme de conflit avec fuelux
        response.render(JavaScriptHeaderItem.forUrl("js/bootstrap-datepicker.js"));
        response.render(CssContentHeaderItem.forUrl("css/datepicker.css"));

        response.render(OnDomReadyHeaderItem.forScript(REFRESH_TOOLTIP_HELP_SEARCH));
    }

    private void initComposants() {
        Profil profil = new Profil("profil", authentication.getEntrepriseUserInfo().getIsVerifier());
        ContactezNous contactezNous = new ContactezNous("contactezNous");
        Commentaire commentaire = new Commentaire("commentaire");

        add(profil, contactezNous, commentaire);
    }

    private void initVosCriteres() {
        SearchAnnonceDTO searchAnnonceDTO = new SearchAnnonceDTO();

        CheckBox electricite = new CheckBox("electricite", new Model<Boolean>());
        CheckBox plomberie = new CheckBox("plomberie", new Model<Boolean>());
        CheckBox espaceVert = new CheckBox("espaceVert", new Model<Boolean>());
        CheckBox maconnerie = new CheckBox("maconnerie", new Model<Boolean>());

        castorDatePicker = new CastorDatePicker("aPartirdu", "rechercheDate", true);
        castorDatePicker.add(DateValidator.maximum(new Date(), "dd/MM/yyyy"));

        TextField<Integer> departement = new TextField<>("departement");

        AjaxLink<Void> rechercher = new AjaxLink<Void>("rechercher") {
            @Override
            public void onClick(AjaxRequestTarget target) {

            }
        };

        Form searchForm = new Form<>("searchForm", new CompoundPropertyModel<>(searchAnnonceDTO));
        searchForm.add(electricite, plomberie, espaceVert, maconnerie, castorDatePicker, departement, rechercher);

        add(searchForm);
    }

}