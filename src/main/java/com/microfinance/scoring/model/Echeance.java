package com.microfinance.scoring.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Echeance {
    private UUID id;
    private Date dateEcheance;
    private double mensualite;
    private Date datePaiement;
    private String statutPaiement;

    private List<Incident> incidents;

    public Echeance()
    {
        this.id=UUID.randomUUID();
        this.incidents = new ArrayList<>();
    }
    public Echeance(Date dateEcheance, double mensualite, Date datePaiement, String statutPaiement, List<Incident> incidents) {
        this.id=UUID.randomUUID();
        this.dateEcheance = dateEcheance;
        this.mensualite = mensualite;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.incidents = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }
    public Date getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(Date dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public double getMensualite() {
        return mensualite;
    }

    public void setMensualite(double mensualite) {
        this.mensualite = mensualite;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public List<Incident> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<Incident> incidents) {
        this.incidents = incidents;
    }
}