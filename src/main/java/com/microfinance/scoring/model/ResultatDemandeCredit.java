package com.microfinance.scoring.model;

import com.microfinance.scoring.model.enums.Decision;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe représentant le résultat d'une demande de crédit après traitement
 */
public class ResultatDemandeCredit {
    private UUID creditId;
    private UUID clientId;
    private Decision decision;
    private double scoreCalcule;
    private String motifDecision;
    private LocalDateTime dateTraitement;
    private double montantApprouve;
    private String commentaires;

    // Constructeur
    public ResultatDemandeCredit(UUID creditId, UUID clientId, Decision decision, 
                                double scoreCalcule, String motifDecision, 
                                double montantApprouve) {
        this.creditId = creditId;
        this.clientId = clientId;
        this.decision = decision;
        this.scoreCalcule = scoreCalcule;
        this.motifDecision = motifDecision;
        this.montantApprouve = montantApprouve;
        this.dateTraitement = LocalDateTime.now();
    }

    // Getters
    public UUID getCreditId() {
        return creditId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public Decision getDecision() {
        return decision;
    }

    public double getScoreCalcule() {
        return scoreCalcule;
    }

    public String getMotifDecision() {
        return motifDecision;
    }

    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }

    public double getMontantApprouve() {
        return montantApprouve;
    }

    public String getCommentaires() {
        return commentaires;
    }

    // Setters
    public void setCreditId(UUID creditId) {
        this.creditId = creditId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public void setScoreCalcule(double scoreCalcule) {
        this.scoreCalcule = scoreCalcule;
    }

    public void setMotifDecision(String motifDecision) {
        this.motifDecision = motifDecision;
    }

    public void setDateTraitement(LocalDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public void setMontantApprouve(double montantApprouve) {
        this.montantApprouve = montantApprouve;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    // Méthodes utilitaires
    public boolean estApprouve() {
        return decision == Decision.ACCORD_IMMEDIAT;
    }

    public boolean estRejete() {
        return decision == Decision.REFUS_AUTOMATIQUE;
    }

    public boolean estEnEtudeManuelle() {
        return decision == Decision.ETUDE_MANUELLE;
    }

    @Override
    public String toString() {
        return "ResultatDemandeCredit{" +
                "creditId=" + creditId +
                ", clientId=" + clientId +
                ", decision=" + decision +
                ", scoreCalcule=" + scoreCalcule +
                ", motifDecision='" + motifDecision + '\'' +
                ", dateTraitement=" + dateTraitement +
                ", montantApprouve=" + montantApprouve +
                ", commentaires='" + commentaires + '\'' +
                '}';
    }
}