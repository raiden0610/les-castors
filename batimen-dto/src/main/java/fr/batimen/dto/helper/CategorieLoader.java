package fr.batimen.dto.helper;

import java.util.ArrayList;
import java.util.List;

import fr.batimen.dto.CategorieMetierDTO;
import fr.batimen.dto.SousCategorieMetierDTO;

/**
 * Chargeur de catégorie, initialise et donne les differentes categorie / sous
 * catégorie metier.
 * 
 * @author Casaucau Cyril
 * 
 */
public class CategorieLoader {

    private static CategorieMetierDTO electricite;
    private static CategorieMetierDTO plomberie;
    private static CategorieMetierDTO espaceVert;
    private static CategorieMetierDTO decorationMaconnerie;
    private static CategorieMetierDTO grosOeuvre;
    private static CategorieMetierDTO equipement;
    private static CategorieMetierDTO toutesCategories;

    private CategorieLoader() {

    }

    public static synchronized CategorieMetierDTO getCategorieElectricite() {
        if (electricite == null) {
            electricite = new CategorieMetierDTO("Electricité", (short) 0);
            SousCategorieMetierDTO sousElectrique = new SousCategorieMetierDTO("Installation électrique");
            electricite.addSousCategorie(sousElectrique);
        }
        return electricite;
    }

    public static synchronized CategorieMetierDTO getCategoriePlomberie() {
        if (plomberie == null) {
            plomberie = new CategorieMetierDTO("Plomberie", (short) 1);
            SousCategorieMetierDTO sousPlomberie = new SousCategorieMetierDTO("Tuyauterie");
            plomberie.addSousCategorie(sousPlomberie);
        }
        return plomberie;
    }

    public static synchronized CategorieMetierDTO getCategorieEspaceVert() {
        if (espaceVert == null) {
            espaceVert = new CategorieMetierDTO("Espace Vert", (short) 2);
            SousCategorieMetierDTO sousEspaceVert = new SousCategorieMetierDTO("Jardinage");
            espaceVert.addSousCategorie(sousEspaceVert);
        }
        return espaceVert;
    }

    public static synchronized CategorieMetierDTO getCategorieDecorationMaconnerie() {
        if (decorationMaconnerie == null) {
            decorationMaconnerie = new CategorieMetierDTO("Décoration / Maçonnerie", (short) 3);
            SousCategorieMetierDTO sousDecorationMaconnerie = new SousCategorieMetierDTO("Peinture");
            decorationMaconnerie.addSousCategorie(sousDecorationMaconnerie);
        }
        return decorationMaconnerie;
    }

    public static synchronized CategorieMetierDTO getCategorieGrosOeuvre() {
        if (grosOeuvre == null) {
            grosOeuvre = new CategorieMetierDTO("Gros oeuvre", (short) 4);
            SousCategorieMetierDTO sousGrosOeuvre = new SousCategorieMetierDTO("Fondation");
            grosOeuvre.addSousCategorie(sousGrosOeuvre);
        }
        return grosOeuvre;
    }

    public static synchronized CategorieMetierDTO getCategorieEquipement() {
        if (equipement == null) {
            equipement = new CategorieMetierDTO("Equipement", (short) 5);
            SousCategorieMetierDTO sousEquipement = new SousCategorieMetierDTO("Alarme");
            equipement.addSousCategorie(sousEquipement);
        }
        return equipement;
    }

    public static synchronized CategorieMetierDTO getCategorieAll() {
        if (toutesCategories == null) {
            toutesCategories = new CategorieMetierDTO("Toutes les catégories", (short) 6);
        }
        return toutesCategories;
    }

    public static synchronized List<CategorieMetierDTO> getAllCategories() {
        List<CategorieMetierDTO> allCategories = new ArrayList<CategorieMetierDTO>();
        allCategories.add(getCategorieDecorationMaconnerie());
        allCategories.add(getCategorieElectricite());
        allCategories.add(getCategorieEquipement());
        allCategories.add(getCategorieEspaceVert());
        allCategories.add(getCategorieGrosOeuvre());
        allCategories.add(getCategoriePlomberie());

        return allCategories;
    }

}
