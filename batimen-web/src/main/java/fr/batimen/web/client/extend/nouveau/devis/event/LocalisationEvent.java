package fr.batimen.web.client.extend.nouveau.devis.event;

import fr.batimen.dto.LocalisationDTO;
import fr.batimen.web.client.event.AbstractEvent;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.util.List;

/**
 * Event wicket qui declenche l'envoi de la DTO conternant les informations de localisation.
 *
 * @author Casaucau Cyril
 */
public class LocalisationEvent extends AbstractEvent {

    private final List<LocalisationDTO> localisationDTOMemeCodePostal;

    public LocalisationEvent(AjaxRequestTarget target, List<LocalisationDTO> localisationDTO) {
        super(target);
        this.localisationDTOMemeCodePostal = localisationDTO;
    }

    public List<LocalisationDTO> getLocalisationDTOMemeCodePostal() {
        return localisationDTOMemeCodePostal;
    }
}