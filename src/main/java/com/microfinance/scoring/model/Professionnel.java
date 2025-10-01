package com.microfinance.scoring.model;

import java.util.Date;

public class Professionnel extends Personne {
    private double revenu;
    private String immatriculationFiscale;
    private String secteurActivite;
    private String activite;

    public Professionnel()
    {
        super();
    }
    public Professionnel(double revenu, String immatriculationFiscale, String secteurActivite, String activite) {
        this.revenu = revenu;
        this.immatriculationFiscale = immatriculationFiscale;
        this.secteurActivite = secteurActivite;
        this.activite = activite;
    }

    public Professionnel(String nom, String prenom, String ville, Date dateNaissance, int nombreEnfants, String investissement, String placement, String situationFamiliale, Date createdAt, int score, double revenu, String immatriculationFiscale, String secteurActivite, String activite) {
        super(nom, prenom, ville, dateNaissance, nombreEnfants, investissement, placement, situationFamiliale, createdAt, score);
        this.revenu = revenu;
        this.immatriculationFiscale = immatriculationFiscale;
        this.secteurActivite = secteurActivite;
        this.activite = activite;
    }

    public double getRevenu() {
        return revenu;
    }

    public void setRevenu(double revenu) {
        this.revenu = revenu;
    }

    public String getImmatriculationFiscale() {
        return immatriculationFiscale;
    }

    public void setImmatriculationFiscale(String immatriculationFiscale) {
        this.immatriculationFiscale = immatriculationFiscale;
    }

    public String getSecteurActivite() {
        return secteurActivite;
    }

    public void setSecteurActivite(String secteurActivite) {
        this.secteurActivite = secteurActivite;
    }

    public String getActivite() {
        return activite;
    }

    public void setActivite(String activite) {
        this.activite = activite;
    }
}