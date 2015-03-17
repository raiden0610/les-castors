package fr.batimen.ws.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import javax.persistence.Table;

import fr.batimen.core.constant.QueryJPQL;

/**
 * Entité client : symbolise un client en base de données.
 * 
 * @author Casaucau Cyril
 * 
 */
@Entity
@Table(name = "Client")
@NamedQueries(value = {
        @NamedQuery(name = QueryJPQL.CLIENT_LOGIN,
                query = "SELECT c FROM Client AS c LEFT OUTER JOIN FETCH c.permissions AS perm WHERE c.login = :login"),
        @NamedQuery(name = QueryJPQL.CLIENT_BY_EMAIL, query = "SELECT c FROM Client AS c WHERE c.email = :email"),
        @NamedQuery(name = QueryJPQL.CLIENT_BY_ACTIVATION_KEY,
                query = "SELECT c FROM Client AS c WHERE c.cleActivation = :cleActivation"),
        @NamedQuery(name = QueryJPQL.CLIENT_HASH_BY_LOGIN,
                query = "SELECT c.password FROM Client AS c WHERE c.login = :login"),
        @NamedQuery(name = QueryJPQL.CLIENT_STATUT_BY_LOGIN,
                query = "SELECT c.isActive FROM Client AS c WHERE c.login = :login") })
public class Client extends AbstractUser implements Serializable {

    private static final long serialVersionUID = -7591981472565360003L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @OneToMany(mappedBy = "demandeur",
            targetEntity = Annonce.class,
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    private List<Annonce> devisDemandes = new ArrayList<Annonce>();
    @OneToMany(mappedBy = "client",
            targetEntity = Permission.class,
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    protected List<Permission> permissions = new ArrayList<Permission>();
    @OneToMany(mappedBy = "clientNotifier",
            targetEntity = Notification.class,
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY)
    protected List<Notification> notifications = new ArrayList<Notification>();

    /**
     * @return the devisDemandes
     */
    public List<Annonce> getDevisDemandes() {
        return devisDemandes;
    }

    /**
     * @param devisDemandes
     *            the devisDemandes to set
     */
    public void setDevisDemandes(List<Annonce> devisDemandes) {
        this.devisDemandes = devisDemandes;
    }

    /**
     * @return the typeCompte
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

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
     * @return the notifications
     */
    public List<Notification> getNotifications() {
        return notifications;
    }

    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * @param notifications
     *            the notifications to set
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
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

        if (object instanceof Client) {
            Client other = (Client) object;
            return Objects.equals(this.login, other.login) && Objects.equals(this.email, other.email);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(Objects.hash(this.login, this.email));
    }
}
