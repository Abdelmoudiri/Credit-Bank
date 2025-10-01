package com.microfinance.scoring.service;

import com.microfinance.scoring.model.Personne;
import com.microfinance.scoring.model.Credit;
import com.microfinance.scoring.model.enums.Decision;
import com.microfinance.scoring.model.enums.TypeCredit;

/**
 * Moteur de décision automatique pour l'approbation des crédits
 * Règles: Score ≥80 → ACCORD_IMMEDIAT, 60-79 → ETUDE_MANUELLE, <60 → REFUS_AUTOMATIQUE
 */
public class DecisionEngine {
    
    private ScoringService scoringService;
    
    public DecisionEngine() {
        this.scoringService = new ScoringService();
    }
    
    /**
     * Prendre une décision automatique sur une demande de crédit
     * @param personne Le demandeur
     * @param montantDemande Montant demandé
     * @param typeCredit Type de crédit
     * @param isExistingClient true si client existant
     * @return Décision automatique
     */
    public Decision prendreDecision(Personne personne, double montantDemande, TypeCredit typeCredit, boolean isExistingClient) {
        // Calcul du score
//        int score = scoringService.calculerScoreGlobal(personne, isExistingClient);
        int score=0;
        // Vérification de l'éligibilité de base
        if (!scoringService.isEligible(score, isExistingClient)) {
            return Decision.REFUS_AUTOMATIQUE;
        }
        
        // Vérification de la capacité d'emprunt
        double capaciteMax = scoringService.calculerCapaciteEmprunt(personne, isExistingClient);
        if (montantDemande > capaciteMax) {
            return Decision.REFUS_AUTOMATIQUE;
        }
        
        // Décision selon le score
        if (score >= 80) {
            return Decision.ACCORD_IMMEDIAT;
        } else if (score >= 60 && score < 80) {
            return Decision.ETUDE_MANUELLE;
        } else {
            return Decision.REFUS_AUTOMATIQUE;
        }
    }
    
    /**
     * Calculer le montant à octroyer selon la décision et le profil
     * @param personne Le demandeur
     * @param montantDemande Montant demandé
     * @param decision Décision prise
     * @param isExistingClient true si client existant
     * @return Montant à octroyer
     */
    public double calculerMontantOctroye(Personne personne, double montantDemande, Decision decision, boolean isExistingClient) {
        if (decision == Decision.REFUS_AUTOMATIQUE) {
            return 0;
        }
        
        double capaciteMax = scoringService.calculerCapaciteEmprunt(personne, isExistingClient);
//        int score = scoringService.calculerScoreGlobal(personne, isExistingClient);
        int score=0;
        if (decision == Decision.ACCORD_IMMEDIAT) {
            // Accord immédiat: montant demandé ou capacité max si inférieur
            return Math.min(montantDemande, capaciteMax);
        } else {
            // Étude manuelle: réduction selon le score
            double facteurReduction = 0.8; // 80% du montant demandé par défaut
            
            if (score >= 75) {
                facteurReduction = 0.9; // 90% pour scores élevés dans la tranche
            } else if (score >= 70) {
                facteurReduction = 0.85; // 85% pour scores moyens
            } else {
                facteurReduction = 0.75; // 75% pour scores plus faibles
            }
            
            double montantProprose = montantDemande * facteurReduction;
            return Math.min(montantProprose, capaciteMax);
        }
    }
    
    /**
     * Calculer le taux d'intérêt selon le profil de risque
     * @param personne Le demandeur
     * @param typeCredit Type de crédit
     * @param decision Décision prise
     * @param isExistingClient true si client existant
     * @return Taux d'intérêt annuel
     */
    public double calculerTauxInteret(Personne personne, TypeCredit typeCredit, Decision decision, boolean isExistingClient) {
//        int score = scoringService.calculerScoreGlobal(personne, isExistingClient);
//        int score = scoringService.calculerScoreGlobal(personne, isExistingClient);
        int score=0;
        // Taux de base selon le type de crédit
        double tauxBase = getTauxBase(typeCredit);
        
        // Ajustement selon le score (prime de risque)
        double primeRisque = 0;
        
        if (score >= 80) {
            primeRisque = -0.5; // Réduction pour excellent score
        } else if (score >= 70) {
            primeRisque = 0; // Taux de base
        } else if (score >= 60) {
            primeRisque = 1.0; // Majoration pour score moyen
        } else {
            primeRisque = 2.0; // Forte majoration pour score faible
        }
        
        // Bonus client existant
        if (isExistingClient && score >= 70) {
            primeRisque -= 0.25; // Réduction de 0.25% pour clients fidèles
        }
        
        return Math.max(tauxBase + primeRisque, 3.0); // Taux minimum 3%
    }
    
    /**
     * Valider les critères spéciaux selon le type de crédit
     * @param personne Le demandeur
     * @param typeCredit Type de crédit
     * @param montantDemande Montant demandé
     * @return true si critères respectés
     */
    public boolean validerCriteresSpeciaux(Personne personne, TypeCredit typeCredit, double montantDemande) {
        switch (typeCredit) {
            case IMMOBILIER:
                return validerCriteresImmobilier(personne, montantDemande);
            case AUTOMOBILE:
                return validerCriteresAutomobile(personne, montantDemande);
            case CONSOMMATION:
                return validerCriteresConsommation(personne, montantDemande);
            case MICRO_CREDIT:
                return validerCriteresicroCredit(personne, montantDemande);
            default:
                return true;
        }
    }
    
    /**
     * Générer un rapport de décision détaillé
     * @param personne Le demandeur
     * @param montantDemande Montant demandé
     * @param typeCredit Type de crédit
     * @param isExistingClient true si client existant
     * @return Rapport textuel
     */
    public String genererRapportDecision(Personne personne, double montantDemande, TypeCredit typeCredit, boolean isExistingClient) {
//        int score = scoringService.calculerScoreGlobal(personne, isExistingClient);
        int score=0;
        Decision decision = prendreDecision(personne, montantDemande, typeCredit, isExistingClient);
        double montantOctroye = calculerMontantOctroye(personne, montantDemande, decision, isExistingClient);
        double tauxInteret = calculerTauxInteret(personne, typeCredit, decision, isExistingClient);
        double capaciteMax = scoringService.calculerCapaciteEmprunt(personne, isExistingClient);
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DE DÉCISION CRÉDIT ===\n");
        rapport.append("Client: ").append(personne.getPrenom()).append(" ").append(personne.getNom()).append("\n");
        rapport.append("Type: ").append(isExistingClient ? "Client existant" : "Nouveau client").append("\n");
        rapport.append("Score calculé: ").append(score).append("/100\n");
        rapport.append("Capacité d'emprunt max: ").append(String.format("%.2f", capaciteMax)).append(" DH\n");
        rapport.append("Montant demandé: ").append(String.format("%.2f", montantDemande)).append(" DH\n");
        rapport.append("DÉCISION: ").append(decision.toString()).append("\n");
        
        if (decision != Decision.REFUS_AUTOMATIQUE) {
            rapport.append("Montant proposé: ").append(String.format("%.2f", montantOctroye)).append(" DH\n");
            rapport.append("Taux d'intérêt: ").append(String.format("%.2f", tauxInteret)).append("%\n");
        }
        
        rapport.append("===============================\n");
        
        return rapport.toString();
    }
    
    // Méthodes privées utilitaires
    private double getTauxBase(TypeCredit typeCredit) {
        switch (typeCredit) {
            case IMMOBILIER:
                return 4.5;
            case AUTOMOBILE:
                return 6.0;
            case CONSOMMATION:
                return 8.0;
            case MICRO_CREDIT:
                return 12.0;
            default:
                return 8.0;
        }
    }
    
    private boolean validerCriteresImmobilier(Personne personne, double montantDemande) {
        // Critères crédit immobilier: âge 25-50, revenus >4000, marié
        int age = calculerAge(personne.getDateNaissance());
        double revenu = getRevenu(personne);
        
        return age >= 25 && age <= 50 && 
               revenu >= 4000 && 
               "MARIE".equals(personne.getSituationFamiliale()) &&
               montantDemande <= revenu * 120; // Max 10 ans de revenus
    }
    
    private boolean validerCriteresAutomobile(Personne personne, double montantDemande) {
        double revenu = getRevenu(personne);
        return revenu >= 3000 && montantDemande <= revenu * 36; // Max 3 ans de revenus
    }
    
    private boolean validerCriteresConsommation(Personne personne, double montantDemande) {
        double revenu = getRevenu(personne);
        return revenu >= 2000 && montantDemande <= revenu * 12; // Max 1 an de revenus
    }
    
    private boolean validerCriteresicroCredit(Personne personne, double montantDemande) {
        return montantDemande <= 50000; // Plafond micro-crédit
    }
    
    private double getRevenu(Personne personne) {
        if (personne instanceof com.microfinance.scoring.model.Employe) {
            return ((com.microfinance.scoring.model.Employe) personne).getSalaire();
        } else if (personne instanceof com.microfinance.scoring.model.Professionnel) {
            return ((com.microfinance.scoring.model.Professionnel) personne).getRevenu();
        }
        return 0;
    }
    
    private int calculerAge(java.util.Date dateNaissance) {
        if (dateNaissance == null) return 0;
        
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar birthDate = java.util.Calendar.getInstance();
        birthDate.setTime(dateNaissance);
        
        int age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR);
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}