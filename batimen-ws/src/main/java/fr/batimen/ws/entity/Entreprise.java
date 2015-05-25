package fr.batimen.ws.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import fr.batimen.core.constant.QueryJPQL;
import fr.batimen.dto.enums.StatutJuridique;

/**
 * Entité Entreprise : Symbolise l'entreprise de l'artisan en base de données.
 * 
 * @author Casaucau Cyril
 * 
 */
@Entity
@Table(name = "Entreprise")
@NamedQueries(value = {
        @NamedQuery(name = QueryJPQL.ENTREPRISE_BY_SIRET,
                query = "SELECT ent FROM Entreprise AS ent WHERE ent.siret = :siret"),
        @NamedQuery(name = QueryJPQL.ENTREPRISE_BY_ARTISAN,
                query = "SELECT ent FROM Entreprise AS ent WHERE ent.artisan.login = :login"),
        @NamedQuery(name = QueryJPQL.ENTREPRISE_BY_NOM_COMPLET_STATUT_SIRET_DEPARTEMENT,
                query = "SELECT ent FROM Entreprise AS ent WHERE ent.nomComplet = :entrepriseNomComplet AND ent.siret = :siret AND ent.statutJuridique = :entrepriseStatutJuridique AND ent.adresse.departement = :departement")})
public class Entreprise extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 8234078910852637284L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 26, nullable = false)
    private String nomComplet;
    @Column(nullable = false)
    private StatutJuridique statutJuridique;
    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateCreation;
    @Column(length = 14, nullable = false)
    private String siret;
    @Column(nullable = false)
    private Integer nbEmployees;
    @Column(length = 255, nullable = true)
    private String logo;
    @Column(length = 50, nullable = true)
    private String specialite;
    @OneToOne(mappedBy = "entreprise", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private Artisan artisan;
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Paiement paiement;
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private Adresse adresse;
    @OneToMany(mappedBy = "entrepriseSelectionnee",
            targetEntity = Annonce.class,
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    private Set<Annonce> annonceEntrepriseSelecionnee = new HashSet<Annonce>();
    @OneToMany(mappedBy = "entreprise",
            targetEntity = CategorieMetier.class,
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    private Set<CategorieMetier> categoriesMetier = new HashSet<CategorieMetier>();

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the nomComplet
     */
    public String getNomComplet() {
        return nomComplet == null ? "" : nomComplet;
    }

    /**
     * @param nomComplet
     *            the nomComplet to set
     */
    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    /**
     * @return the statutJuridique
     */
    public StatutJuridique getStatutJuridique() {
        return statutJuridique;
    }

    /**
     * @param statutJuridique
     *            the statutJuridique to set
     */
    public void setStatutJuridique(StatutJuridique statutJuridique) {
        this.statutJuridique = statutJuridique;
    }

    /**
     * @return the nbEmployees
     */
    public Integer getNbEmployees() {
        return nbEmployees;
    }

    /**
     * @param nbEmployees
     *            the nbEmployees to set
     */
    public void setNbEmployees(Integer nbEmployees) {
        this.nbEmployees = nbEmployees;
    }

    /**
     * @return the logo
     */
    public String getLogo() {
        return logo;
    }

    /**
     * @param logo
     *            the logo to set
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * @return the artisan
     */
    public Artisan getArtisan() {
        return artisan;
    }

    /**
     * @param artisan
     *            the artisan to set
     */
    public void setArtisan(Artisan artisan) {
        this.artisan = artisan;
    }

    /**
     * @return the paiement
     */
    public Paiement getPaiement() {
        return paiement;
    }

    /**
     * @param paiement
     *            the paiement to set
     */
    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
    }

    /**
     * @return the adresse
     */
    public Adresse getAdresse() {
        return adresse;
    }

    /**
     * @param adresse
     *            the adresse to set
     */
    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    /**
     * @return the siret
     */
    public String getSiret() {
        return siret;
    }

    /**
     * @param siret
     *            the siret to set
     */
    public void setSiret(String siret) {
        this.siret = siret;
    }

    /**
     * @return the categoriesMetier
     */
    public Set<CategorieMetier> getCategoriesMetier() {
        return categoriesMetier;
    }

    /**
     * @param categoriesMetier
     *            the categoriesMetier to set
     */
    public void setCategoriesMetier(Set<CategorieMetier> categoriesMetier) {
        this.categoriesMetier = categoriesMetier;
    }

    /**
     * @return the dateCreation
     */
    public Date getDateCreation() {
        return dateCreation;
    }

    /**
     * @param dateCreation
     *            the dateCreation to set
     */
    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * @return the specialite
     */
    public String getSpecialite() {
        return specialite;
    }

    /**
     * @param specialite
     *            the specialite to set
     */
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    /**
     * @return the annonceEntrepriseSelecionnee
     */
    public Set<Annonce> getAnnonceEntrepriseSelecionnee() {
        return annonceEntrepriseSelecionnee;
    }

    /**
     * @param annonceEntrepriseSelecionnee
     *            the annonceEntrepriseSelecionnee to set
     */
    public void setAnnonceEntrepriseSelecionnee(Set<Annonce> annonceEntrepriseSelecionnee) {
        this.annonceEntrepriseSelecionnee = annonceEntrepriseSelecionnee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(Objects.hash(this.nomComplet, this.statutJuridique, this.nbEmployees));
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

        if (object instanceof Entreprise) {
            Entreprise other = (Entreprise) object;
            return Objects.equals(this.nomComplet, other.nomComplet)
                    && Objects.equals(this.statutJuridique, other.statutJuridique)
                    && Objects.equals(this.nbEmployees, other.nbEmployees);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entreprise{");
        sb.append("id=").append(id);
        sb.append(", nomComplet='").append(nomComplet).append('\'');
        sb.append(", statutJuridique=").append(statutJuridique);
        sb.append(", dateCreation=").append(dateCreation);
        sb.append(", siret='").append(siret).append('\'');
        sb.append(", nbEmployees=").append(nbEmployees);
        sb.append(", logo='").append(logo).append('\'');
        sb.append(", specialite='").append(specialite).append('\'');
        sb.append(", artisan=").append(artisan);
        sb.append(", paiement=").append(paiement);
        sb.append(", adresse=").append(adresse);
        sb.append(", annonceEntrepriseSelecionnee=").append(annonceEntrepriseSelecionnee);
        sb.append(", categoriesMetier=").append(categoriesMetier);
        sb.append('}');
        return sb.toString();
    }
}
