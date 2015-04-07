package fr.batimen.web.client.component;

import javax.inject.Inject;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import fr.batimen.dto.ClientDTO;
import fr.batimen.web.app.security.Authentication;
import fr.batimen.web.client.extend.member.client.ModifierMonProfil;
import fr.batimen.web.client.extend.member.client.MonProfil;

public class Profil extends Panel {

    private static final long serialVersionUID = -3775533895973607467L;

    @Inject
    private Authentication authentication;

    private final Link<String> monProfil;
    private final Link<String> modifierMonProfil;

    public Profil(String id) {
        super(id);

        monProfil = new Link<String>("monProfil") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                PageParameters parameters = new PageParameters();
                ClientDTO client = authentication.getCurrentUserInfo();
                parameters.add("login", client.getLogin());
                this.setResponsePage(MonProfil.class, parameters);
            }
        };

        modifierMonProfil = new Link<String>("modifierMonProfil") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                this.setResponsePage(ModifierMonProfil.class);
            }
        };

        this.add(monProfil);
        this.add(modifierMonProfil);
    }
}