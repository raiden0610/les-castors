package fr.castor.ws.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import fr.castor.core.constant.QueryJPQL;

/**
 * Entité Avis : Symbolise la notation en base de données. Permet de noté le
 * travail effectué par l'artisan.
 *
 * @author Casaucau Cyril
 */
@Entity
@Table(name = "Avis")
@NamedQueries(value = {@NamedQuery(name = QueryJPQL.NOTATION_BY_CLIENT_LOGIN,
        query = "SELECT n, n.annonce.entrepriseSelectionnee.nomComplet FROM Avis AS n WHERE n.annonce.demandeur.login = :login AND n.annonce.etatAnnonce != 4 ORDER BY n.dateAvis DESC"),
        @NamedQuery(name = QueryJPQL.NOTATION_BY_ENTREPRISE_SIRET,
                query = "SELECT n FROM Avis AS n WHERE n.artisan.entreprise.siret = :siret")})
public class Avis extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -1038954593364210382L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double score;
    @Column(length = 500, nullable = false)
    private String commentaire;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAvis;
    @OneToOne(mappedBy = "avis", fetch = FetchType.LAZY)
    @JoinColumn(name = "avisannonce_id")
    private Annonce annonce;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_fk")
    private Artisan artisan;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * @return the commentaire
     */
    public String getCommentaire() {
        return commentaire;
    }

    /**
     * @param commentaire the commentaire to set
     */
    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    /**
     * @return the annonce
     */
    public Annonce getAnnonce() {
        return annonce;
    }

    /**
     * @param annonce the annonce to set
     */
    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    /**
     * @return the artisan
     */
    public Artisan getArtisan() {
        return artisan;
    }

    /**
     * @param artisan the artisan to set
     */
    public void setArtisan(Artisan artisan) {
        this.artisan = artisan;
    }

    /**
     * @return the dateNotation
     */
    public Date getDateAvis() {
        return dateAvis;
    }

    /**
     * @param dateNotation the dateNotation to set
     */
    public void setDateAvis(Date dateNotation) {
        this.dateAvis = dateNotation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(Objects.hash(this.score, this.commentaire));
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

        if (object instanceof Avis) {
            Avis other = (Avis) object;
            return Objects.equals(this.score, other.score) && Objects.equals(this.commentaire, other.commentaire);
        }
        return false;
    }

}
