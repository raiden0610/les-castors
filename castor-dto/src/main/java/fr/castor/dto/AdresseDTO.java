package fr.castor.dto;

import fr.castor.dto.constant.ValidatorConstant;
import org.modelmapper.ModelMapper;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class AdresseDTO extends AbstractDTO {

    private static final long serialVersionUID = 3446506172762761335L;

    @NotNull
    @Size(min = ValidatorConstant.ADRESSE_MIN, max = ValidatorConstant.ADRESSE_MAX)
    private String adresse;
    @Size(max = ValidatorConstant.COMPLEMENT_ADRESSE_MAX)
    private String complementAdresse;
    @NotNull
    @Pattern(message = "Format code postal invalide", regexp = ValidatorConstant.CODE_POSTAL_REGEX)
    private String codePostal;
    @NotNull
    @Size(max = ValidatorConstant.VILLE_MAX)
    private String ville;
    @NotNull
    @Min(value = ValidatorConstant.DEPARTEMENT_MIN)
    @Max(value = ValidatorConstant.DEPARTEMENT_MAX)
    private Integer departement;

    /**
     * @return the adresse
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * @return the complementAdresse
     */
    public String getComplementAdresse() {
        return complementAdresse;
    }

    /**
     * @return the codePostal
     */
    public String getCodePostal() {
        return codePostal;
    }

    /**
     * @return the ville
     */
    public String getVille() {
        return ville;
    }

    /**
     * @return the departement
     */
    public Integer getDepartement() {
        return departement;
    }

    /**
     * @param adresse
     *            the adresse to set
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * @param complementAdresse
     *            the complementAdresse to set
     */
    public void setComplementAdresse(String complementAdresse) {
        this.complementAdresse = complementAdresse;
    }

    /**
     * @param codePostal
     *            the codePostal to set
     */
    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    /**
     * @param ville
     *            the ville to set
     */
    public void setVille(String ville) {
        this.ville = ville;
    }

    /**
     * @param departement
     *            the departement to set
     */
    public void setDepartement(Integer departement) {
        this.departement = departement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(Objects.hash(this.getAdresse(), this.getCodePostal(), this.getVille()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof AdresseDTO) {
            AdresseDTO other = (AdresseDTO) object;
            return Objects.equals(this.getAdresse(), other.getAdresse())
                    && Objects.equals(this.getCodePostal(), other.getCodePostal())
                    && Objects.equals(this.getVille(), other.getVille());
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AdresseDTO{");
        sb.append("adresse='").append(adresse).append('\'');
        sb.append(", complementAdresse='").append(complementAdresse).append('\'');
        sb.append(", codePostal='").append(codePostal).append('\'');
        sb.append(", ville='").append(ville).append('\'');
        sb.append(", departement=").append(departement);
        sb.append('}');
        return sb.toString();
    }
    public static AdresseDTO copy(AdresseDTO adresseSource) {
        ModelMapper mapper = new ModelMapper();
        return  mapper.map(adresseSource, AdresseDTO.class);
    }

}