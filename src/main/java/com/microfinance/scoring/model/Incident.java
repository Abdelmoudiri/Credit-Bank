package com.microfinance.scoring.model;

import java.util.Date;
import java.util.UUID;

public class Incident {
    private UUID id;
    private Date dateIncident;
    private String echeance;
    private int score;
    private String typeIncident;

    public Incident()
    {

    }
    public Incident(Date dateIncident, String echeance, int score, String typeIncident) {
        this.id=UUID.randomUUID();
        this.dateIncident = dateIncident;
        this.echeance = echeance;
        this.score = score;
        this.typeIncident = typeIncident;
    }


    public UUID getId() {
        return id;
    }

    public Date getDateIncident() {
        return dateIncident;
    }

    public void setDateIncident(Date dateIncident) {
        this.dateIncident = dateIncident;
    }

    public String getEcheance() {
        return echeance;
    }

    public void setEcheance(String echeance) {
        this.echeance = echeance;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTypeIncident() {
        return typeIncident;
    }

    public void setTypeIncident(String typeIncident) {
        this.typeIncident = typeIncident;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}