package fr.castor.dto;

import fr.castor.dto.constant.ValidatorConstant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Date;
import java.util.Objects;

public class AvisDTO extends AbstractDTO {

    private static final long serialVersionUID = 2210249579834795935L;

    @NotNull
    private Double score;
    @NotNull
    @Size(min = ValidatorConstant.NOTATION_MIN_COMMENTAIRE, max = ValidatorConstant.NOTATION_MAX_COMMENTAIRE)
    private String commentaire;
    @NotNull
    private String nomEntreprise;
    @NotNull
    private String nomPrenomOrLoginClient;
    private Date dateAvis;

    /**
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * @return the commentaire
     */
    public String getCommentaire() {
        return commentaire;
    }

    /**
     * @param score
     *            the score to set
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * @param commentaire
     *            the commentaire to set
     */
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    /**
     * @return the nomEntreprise
     */
    public String getNomEntreprise() {
        return nomEntreprise;
    }

    /**
     * @param nomEntreprise
     *            the nomEntreprise to set
     */
    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getNomPrenomOrLoginClient() {
        return nomPrenomOrLoginClient;
    }

    public void setNomPrenomOrLoginClient(String nomPrenomOrLoginClient) {
        this.nomPrenomOrLoginClient = nomPrenomOrLoginClient;
    }

    public Date getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(Date dateAvis) {
        this.dateAvis = dateAvis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvisDTO that = (AvisDTO) o;
        return Objects.equals(score, that.score) &&
                Objects.equals(commentaire, that.commentaire) &&
                Objects.equals(nomEntreprise, that.nomEntreprise) &&
                Objects.equals(nomPrenomOrLoginClient, that.nomPrenomOrLoginClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, commentaire, nomEntreprise, nomPrenomOrLoginClient);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AvisDTO{");
        sb.append("score=").append(score);
        sb.append(", commentaire='").append(commentaire).append('\'');
        sb.append(", nomEntreprise='").append(nomEntreprise).append('\'');
        sb.append(", nomPrenomOrLoginClient='").append(nomPrenomOrLoginClient).append('\'');
        sb.append('}');
        return sb.toString();
    }
}