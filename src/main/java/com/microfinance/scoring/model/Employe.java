package com.microfinance.scoring.model;

import java.util.Date;

public class Employe extends Personne {


    private double salaire;
    private int anciennete;
    private String poste;
    private String typeContrat;
    private String secteur;

    public Employe()
    {
        super();
    }

    public Employe(double salaire, int anciennete, String poste, String typeContrat, String secteur) {
        this.salaire = salaire;
        this.anciennete = anciennete;
        this.poste = poste;
        this.typeContrat = typeContrat;
        this.secteur = secteur;
    }

    public Employe(String nom, String prenom, String ville, Date dateNaissance, int nombreEnfants, String investissement, String placement, String situationFamiliale, Date createdAt, int score, double salaire, int anciennete, String poste, String typeContrat, String secteur) {
        super(nom, prenom, ville, dateNaissance, nombreEnfants, investissement, placement, situationFamiliale, createdAt, score);
        this.salaire = salaire;
        this.anciennete = anciennete;
        this.poste = poste;
        this.typeContrat = typeContrat;
        this.secteur = secteur;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public int getAnciennete() {
        return anciennete;
    }

    public void setAnciennete(int anciennete) {
        this.anciennete = anciennete;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }
}