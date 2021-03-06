package fr.castor.dto.enums;

public enum TypeCompte {

    ADMINISTRATEUR_MANAGER("Administrateur", "admin, manager"), ADMINISTRATEUR_COMMERCIAL("Administrateur",
            "admin, commercial"), ARTISAN_PREMIUM("Artisan premium", "partenaire:premium"), ARTISAN("Artisan",
            "partenaire"), CLIENT("Client", "particulier"), ADMINISTRATEUR("Administrateur", "admin"), INCONNU("Inconnu", "inconnu");

    TypeCompte(String nomCompte, String roles) {
        this.nomCompte = nomCompte;
        this.roles = roles;
    }

    private String nomCompte;

    private String roles;

    public String getNomCompte() {
        return nomCompte;
    }

    /**
     * @return the roles
     */
    public String getRole() {
        return roles;
    }

    @Override
    public String toString() {
        return nomCompte;
    }
}
