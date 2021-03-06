package fr.castor.dto.aggregate;

import fr.castor.dto.AbstractDTO;
import fr.castor.dto.CategorieMetierDTO;
import fr.castor.dto.constant.ValidatorConstant;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * DTO permettant de rechercher des annonces
 *
 * @author Casaucau Cyril
 */
public class SearchAnnonceDTOIn extends AbstractDTO {

    @Valid
    private List<CategorieMetierDTO> categoriesMetierDTO = new LinkedList<>();

    @NotNull
    @Min(value = ValidatorConstant.DEPARTEMENT_MIN)
    @Max(value = ValidatorConstant.DEPARTEMENT_MAX)
    private Integer departement;

    @NotNull
    @Size(min = ValidatorConstant.CLIENT_LOGIN_RANGE_MIN, max = ValidatorConstant.CLIENT_LOGIN_RANGE_MAX)
    private String loginDemandeur;

    @NotNull
    private Integer rangeDebut;
    @NotNull
    private Integer rangeFin;

    public List<CategorieMetierDTO> getCategoriesMetierDTO() {
        return categoriesMetierDTO;
    }

    public void setCategoriesMetierDTO(List<CategorieMetierDTO> categoriesMetierDTO) {
        this.categoriesMetierDTO = categoriesMetierDTO;
    }

    public Integer getDepartement() {
        return departement;
    }

    public void setDepartement(Integer departement) {
        this.departement = departement;
    }

    public String getLoginDemandeur() {
        return loginDemandeur;
    }

    public void setLoginDemandeur(String loginDemandeur) {
        this.loginDemandeur = loginDemandeur;
    }

    public Integer getRangeDebut() {
        return rangeDebut;
    }

    public void setRangeDebut(Integer rangeDebut) {
        this.rangeDebut = rangeDebut;
    }

    public Integer getRangeFin() {
        return rangeFin;
    }

    public void setRangeFin(Integer rangeFin) {
        this.rangeFin = rangeFin;
    }

    public void clear(){
        categoriesMetierDTO.clear();
        departement = 0;
        rangeDebut = 0;
        rangeFin = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAnnonceDTOIn that = (SearchAnnonceDTOIn) o;
        return Objects.equals(categoriesMetierDTO, that.categoriesMetierDTO) &&
                Objects.equals(departement, that.departement) &&
                Objects.equals(loginDemandeur, that.loginDemandeur) &&
                Objects.equals(rangeDebut, that.rangeDebut) &&
                Objects.equals(rangeFin, that.rangeFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoriesMetierDTO, departement, loginDemandeur, rangeDebut, rangeFin);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchAnnonceDTOIn{");
        sb.append("categoriesMetierDTO=").append(categoriesMetierDTO);
        sb.append(", departement=").append(departement);
        sb.append(", loginDemandeur='").append(loginDemandeur).append('\'');
        sb.append(", rangeDebut=").append(rangeDebut);
        sb.append(", rangeFin=").append(rangeFin);
        sb.append('}');
        return sb.toString();
    }
}