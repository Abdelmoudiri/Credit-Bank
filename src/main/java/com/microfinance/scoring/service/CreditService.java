package com.microfinance.scoring.service;

import com.microfinance.scoring.model.*;
import com.microfinance.scoring.model.enums.*;
import com.microfinance.scoring.repository.*;
import java.util.*;

/**
 * Service principal pour la gestion complète du workflow crédit
 * Orchestration: Demande → Scoring → Décision → Génération échéances
 */
public class CreditService {
    
    private ScoringService scoringService;
    private DecisionEngine decisionEngine;
    private CreditRepository creditRepository;
    private EcheanceRepository echeanceRepository;
    private EmployeRepository employeRepository;
    private ProfessionnelRepository professionnelRepository;
    
    public CreditService() {
        this.scoringService = new ScoringService();
        this.decisionEngine = new DecisionEngine();
        this.creditRepository = new CreditRepository();
        this.echeanceRepository = new EcheanceRepository();
        this.employeRepository = new EmployeRepository();
        this.professionnelRepository = new ProfessionnelRepository();
    }
    

    private Optional<Personne> findClientById(UUID clientId) {
        Optional<Employe> employe = employeRepository.findById(clientId);
        if (employe.isPresent()) {
            return Optional.of(employe.get());
        }

        Optional<Professionnel> professionnel = professionnelRepository.findById(clientId);
        if (professionnel.isPresent()) {
            return Optional.of(professionnel.get());
        }
        
        return Optional.empty();
    }
    


    public ResultatDemandeCredit traiterDemandeCredit(UUID clientId, double montantDemande, int dureeMois, TypeCredit typeCredit) {
        try {
            Optional<Personne> optionalClient = findClientById(clientId);
            if (!optionalClient.isPresent()) {
                return new ResultatDemandeCredit(false, "Client non trouvé", null, null);
            }
            
            Personne client = optionalClient.get();
            
            // 2. Vérifier si client existant (a déjà des crédits)
            List<Credit> creditsExistants = creditRepository.findByClientId(clientId);
            boolean isExistingClient = !creditsExistants.isEmpty();
            
            // 3. Valider les critères spéciaux selon le type de crédit
            if (!decisionEngine.validerCriteresSpeciaux(client, typeCredit, montantDemande)) {
                return new ResultatDemandeCredit(false, "Critères spéciaux non respectés pour ce type de crédit", null, null);
            }
            
            // 4. Calculer le score et prendre la décision
            Decision decision = decisionEngine.prendreDecision(client, montantDemande, typeCredit, isExistingClient);
            
            // 5. Si refus, arrêter le processus
            if (decision == Decision.REFUS_AUTOMATIQUE) {
                String rapport = decisionEngine.genererRapportDecision(client, montantDemande, typeCredit, isExistingClient);
                return new ResultatDemandeCredit(false, "Demande refusée automatiquement", null, rapport);
            }
            
            // 6. Calculer montant octroyé et taux
            double montantOctroye = decisionEngine.calculerMontantOctroye(client, montantDemande, decision, isExistingClient);
            double tauxInteret = decisionEngine.calculerTauxInteret(client, typeCredit, decision, isExistingClient);
            
            // 7. Créer l'objet crédit
            Credit nouveauCredit = new Credit(
                new Date(), // Date de création
                montantDemande,
                montantOctroye,
                tauxInteret,
                dureeMois,
                typeCredit,
                decision,
                new ArrayList<>() // Liste d'échéances vide pour l'instant
            );
            
            // 8. Sauvegarder le crédit
            boolean creditSauvegarde = creditRepository.save(nouveauCredit);
            if (!creditSauvegarde) {
                return new ResultatDemandeCredit(false, "Erreur lors de la sauvegarde du crédit", null, null);
            }
            
            // 9. Générer les échéances si accord immédiat
            if (decision == Decision.ACCORD_IMMEDIAT) {
                List<Echeance> echeances = genererEcheances(nouveauCredit);
                for (Echeance echeance : echeances) {
                    echeanceRepository.save(echeance);
                }
                nouveauCredit.setEcheances(echeances);
            }
            
            // 10. Générer le rapport final
            String rapport = decisionEngine.genererRapportDecision(client, montantDemande, typeCredit, isExistingClient);
            
            return new ResultatDemandeCredit(true, "Demande traitée avec succès", nouveauCredit, rapport);
            
        } catch (Exception e) {
            return new ResultatDemandeCredit(false, "Erreur lors du traitement: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Générer les échéances pour un crédit approuvé
     * @param credit Le crédit approuvé
     * @return Liste des échéances
     */
    public List<Echeance> genererEcheances(Credit credit) {
        List<Echeance> echeances = new ArrayList<>();
        
        double montant = credit.getMontantOctroye();
        double tauxMensuel = credit.getTauxInteret() / 12 / 100; // Taux mensuel
        int nombreEcheances = credit.getDureeMois();
        
        // Calcul de la mensualité avec formule des annuités
        double mensualite = (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -nombreEcheances));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(credit.getDateCredit());
        
        for (int i = 1; i <= nombreEcheances; i++) {
            cal.add(Calendar.MONTH, 1); // Ajouter un mois
            
            Echeance echeance = new Echeance(
                cal.getTime(), // Date d'échéance
                mensualite,
                null, // Pas encore payée
                "EN_ATTENTE", // Statut initial
                new ArrayList<>() // Liste d'incidents vide
            );
            
            echeances.add(echeance);
        }
        
        return echeances;
    }
    
    /**
     * Consulter un crédit avec ses échéances
     * @param creditId ID du crédit
     * @return Crédit avec échéances ou null si non trouvé
     */
    public Credit consulterCredit(UUID creditId) {
        Optional<Credit> optionalCredit = creditRepository.findById(creditId);
        if (optionalCredit.isPresent()) {
            Credit credit = optionalCredit.get();
            // Charger les échéances
            List<Echeance> echeances = echeanceRepository.findByCreditId(creditId);
            credit.setEcheances(echeances);
            return credit;
        }
        return null;
    }
    
    /**
     * Lister tous les crédits d'un client
     * @param clientId ID du client
     * @return Liste des crédits
     */
    public List<Credit> listerCreditsClient(UUID clientId) {
        return creditRepository.findByClientId(clientId);
    }
    
    /**
     * Lister les crédits en attente d'étude manuelle
     * @return Liste des crédits en étude manuelle
     */
    public List<Credit> listerCreditsEtudeManuelle() {
        return creditRepository.findByDecision("ETUDE_MANUELLE");
    }
    
    /**
     * Approuver manuellement un crédit en étude
     * @param creditId ID du crédit
     * @param montantApprouve Montant approuvé après étude
     * @param tauxApprouve Taux approuvé après étude
     * @return true si succès
     */
    public boolean approuverManuellement(UUID creditId, double montantApprouve, double tauxApprouve) {
        Optional<Credit> optionalCredit = creditRepository.findById(creditId);
        if (optionalCredit.isPresent()) {
            Credit credit = optionalCredit.get();
            
            // Mettre à jour le crédit
            credit.setMontantOctroye(montantApprouve);
            credit.setTauxInteret(tauxApprouve);
            credit.setDecision(Decision.ACCORD_IMMEDIAT);
            
            // Sauvegarder
            boolean success = creditRepository.update(credit);
            
            if (success) {
                // Générer les échéances
                List<Echeance> echeances = genererEcheances(credit);
                for (Echeance echeance : echeances) {
                    echeanceRepository.save(echeance);
                }
            }
            
            return success;
        }
        return false;
    }
    
    /**
     * Refuser manuellement un crédit en étude
     * @param creditId ID du crédit
     * @param motifRefus Motif du refus
     * @return true si succès
     */
    public boolean refuserManuellement(UUID creditId, String motifRefus) {
        Optional<Credit> optionalCredit = creditRepository.findById(creditId);
        if (optionalCredit.isPresent()) {
            Credit credit = optionalCredit.get();
            credit.setDecision(Decision.REFUS_AUTOMATIQUE);
            // TODO: Ajouter champ motif_refus dans la base
            return creditRepository.update(credit);
        }
        return false;
    }
    
    /**
     * Calculer les statistiques de portefeuille
     * @return Statistiques du portefeuille de crédits
     */
    public StatistiquesPortefeuille calculerStatistiquesPortefeuille() {
        List<Credit> tousCredits = creditRepository.findAll();
        
        int totalCredits = tousCredits.size();
        int accordsImmediats = 0;
        int etudesManuelle = 0;
        int refus = 0;
        double montantTotalOctroye = 0;
        double montantTotalDemande = 0;
        
        for (Credit credit : tousCredits) {
            montantTotalDemande += credit.getMontantDemande();
            montantTotalOctroye += credit.getMontantOctroye();
            
            switch (credit.getDecision()) {
                case ACCORD_IMMEDIAT:
                    accordsImmediats++;
                    break;
                case ETUDE_MANUELLE:
                    etudesManuelle++;
                    break;
                case REFUS_AUTOMATIQUE:
                    refus++;
                    break;
            }
        }
        
        return new StatistiquesPortefeuille(
            totalCredits,
            accordsImmediats,
            etudesManuelle,
            refus,
            montantTotalOctroye,
            montantTotalDemande,
            totalCredits > 0 ? (double) accordsImmediats / totalCredits * 100 : 0
        );
    }
    
    // Classes internes pour les résultats
    public static class ResultatDemandeCredit {
        private boolean succes;
        private String message;
        private Credit credit;
        private String rapport;
        
        public ResultatDemandeCredit(boolean succes, String message, Credit credit, String rapport) {
            this.succes = succes;
            this.message = message;
            this.credit = credit;
            this.rapport = rapport;
        }
        
        // Getters
        public boolean isSucces() { return succes; }
        public String getMessage() { return message; }
        public Credit getCredit() { return credit; }
        public String getRapport() { return rapport; }
    }
    
    public static class StatistiquesPortefeuille {
        private int totalCredits;
        private int accordsImmediats;
        private int etudesManuelle;
        private int refus;
        private double montantTotalOctroye;
        private double montantTotalDemande;
        private double tauxApprobation;
        
        public StatistiquesPortefeuille(int totalCredits, int accordsImmediats, int etudesManuelle, 
                                      int refus, double montantTotalOctroye, double montantTotalDemande, 
                                      double tauxApprobation) {
            this.totalCredits = totalCredits;
            this.accordsImmediats = accordsImmediats;
            this.etudesManuelle = etudesManuelle;
            this.refus = refus;
            this.montantTotalOctroye = montantTotalOctroye;
            this.montantTotalDemande = montantTotalDemande;
            this.tauxApprobation = tauxApprobation;
        }
        
        // Getters
        public int getTotalCredits() { return totalCredits; }
        public int getAccordsImmediats() { return accordsImmediats; }
        public int getEtudesManuelle() { return etudesManuelle; }
        public int getRefus() { return refus; }
        public double getMontantTotalOctroye() { return montantTotalOctroye; }
        public double getMontantTotalDemande() { return montantTotalDemande; }
        public double getTauxApprobation() { return tauxApprobation; }
    }
}