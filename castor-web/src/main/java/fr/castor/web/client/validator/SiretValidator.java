package fr.castor.web.client.validator;

import fr.castor.dto.helper.SiretValidatorHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Valide un siret
 * 
 * @author Casaucau Cyril
 * 
 */
public class SiretValidator implements IValidator<String> {

    private static final long serialVersionUID = -9143693623488715025L;

    @Override
    public void validate(IValidatable<String> validatable) {

        String siretToValidate = StringUtils.deleteWhitespace(validatable.getValue());

        if (siretToValidate.isEmpty() || siretToValidate.length() != SiretValidatorHelper.SIRET_LENGTH || !SiretValidatorHelper.isSiretLuhnValide(siretToValidate)) {
            ValidationError error = new ValidationError(this);
            error.setVariable("siret", validatable.getValue());
            validatable.error(decorate(error, validatable));
        }
    }

    /**
     * Allows subclasses to decorate reported errors
     * 
     * @param error
     * @param validatable
     * @return decorated error
     */
    protected ValidationError decorate(ValidationError error, IValidatable<String> validatable) {
        return error;
    }

}
