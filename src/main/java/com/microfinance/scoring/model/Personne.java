package com.microfinance.scoring.model;

import java.util.Date;
import java.util.UUID;

public abstract class Personne {
    protected UUID id;
    protected String nom;
    protected String prenom;
    protected String ville;
    protected Date dateNaissance;
    protected int nombreEnfants;
    protected String investissement;
    protected String placement;
    protected String situationFamiliale;
    protected Date createdAt;
    protected int score;


    public Personne()
    {
        this.id=UUID.randomUUID();
    }

    public Personne(String nom, String prenom, String ville, Date dateNaissance, int nombreEnfants, String investissement, String placement, String situationFamiliale, Date createdAt, int score) {
        this.id=UUID.randomUUID();
        this.nom = nom;
        this.prenom = prenom;
        this.ville = ville;
        this.dateNaissance = dateNaissance;
        this.nombreEnfants = nombreEnfants;
        this.investissement = investissement;
        this.placement = placement;
        this.situationFamiliale = situationFamiliale;
        this.createdAt = createdAt;
        this.score = score;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public int getNombreEnfants() {
        return nombreEnfants;
    }

    public void setNombreEnfants(int nombreEnfants) {
        this.nombreEnfants = nombreEnfants;
    }

    public String getInvestissement() {
        return investissement;
    }

    public void setInvestissement(String investissement) {
        this.investissement = investissement;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getSituationFamiliale() {
        return situationFamiliale;
    }

    public void setSituationFamiliale(String situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}