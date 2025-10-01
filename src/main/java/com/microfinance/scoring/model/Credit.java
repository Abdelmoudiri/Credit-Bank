package com.microfinance.scoring.model;

import com.microfinance.scoring.model.enums.TypeCredit;
import com.microfinance.scoring.model.enums.Decision;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Credit {
    private UUID id;
    private Date dateCredit;
    private double montantDemande;
    private double montantOctroye;
    private double tauxInteret;
    private int dureeMois;
    private TypeCredit typeCredit;
    private Decision decision;

    private List<Echeance> echeances;

    public Credit()
    {

    }
    public Credit( Date dateCredit, double montantDemande, double montantOctroye, double tauxInteret, int dureeMois, TypeCredit typeCredit, Decision decision, List<Echeance> echeances) {
        this.id = UUID.randomUUID();
        this.dateCredit = dateCredit;
        this.montantDemande = montantDemande;
        this.montantOctroye = montantOctroye;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.typeCredit = typeCredit;
        this.decision = decision;
        this.echeances = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }



    public Date getDateCredit() {
        return dateCredit;
    }

    public void setDateCredit(Date dateCredit) {
        this.dateCredit = dateCredit;
    }

    public double getMontantDemande() {
        return montantDemande;
    }

    public void setMontantDemande(double montantDemande) {
        this.montantDemande = montantDemande;
    }

    public double getMontantOctroye() {
        return montantOctroye;
    }

    public void setMontantOctroye(double montantOctroye) {
        this.montantOctroye = montantOctroye;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public int getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(int dureeMois) {
        this.dureeMois = dureeMois;
    }

    public TypeCredit getTypeCredit() {
        return typeCredit;
    }

    public void setTypeCredit(TypeCredit typeCredit) {
        this.typeCredit = typeCredit;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(    Decision decision) {
        this.decision = decision;
    }

    public List<Echeance> getEcheances() {
        return echeances;
    }

    public void setEcheances(List<Echeance> echeances) {
        this.echeances = echeances;
    }
}