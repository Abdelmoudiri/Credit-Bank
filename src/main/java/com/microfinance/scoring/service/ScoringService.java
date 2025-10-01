package com.microfinance.scoring.service;

import com.microfinance.scoring.model.Personne;
import com.microfinance.scoring.model.Employe;
import com.microfinance.scoring.model.Professionnel;
import com.microfinance.scoring.model.Incident;
import com.microfinance.scoring.repository.IncidentRepository;
import com.microfinance.scoring.repository.CreditRepository;
import java.util.Date;
import java.util.List;
import java.util.Calendar;


public class ScoringService {
    
    private IncidentRepository incidentRepository;
    private CreditRepository creditRepository;
    
    public ScoringService() {
        this.incidentRepository = new IncidentRepository();
        this.creditRepository = new CreditRepository();
    }





    private int calculerAge(Date dateNaissance) {
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(dateNaissance);

        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private int calculerAnnees(Date dateDebut, Date dateFin) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(dateDebut);
        cal2.setTime(dateFin);

        int years = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
            years--;
        }
        return years;
    }

    public boolean isEligible(int score, boolean isExistingClient) {
        if (isExistingClient) {
            return score >= 60;
        } else {
            return score >= 70;
        }
    }



    private int calculerHistoriquePaiement(Personne personne) {
        if (hasRecentIncidents(personne)) {
            return 30;
        }
        return 80;
    }


    private int calculerCapaciteFinanciere(Personne personne) {
        int score = 0;
        double revenu = 0;

        if (personne instanceof Employe) {
            revenu = ((Employe) personne).getSalaire();

        } else if (personne instanceof Professionnel) {
            revenu = ((Professionnel) personne).getRevenu();
        }

        if (revenu >= 10000) score += 70;
        else if (revenu >= 7000) score += 60;
        else if (revenu >= 5000) score += 50;
        else if (revenu >= 4000) score += 40;
        else if (revenu >= 3000) score += 30;
        else if (revenu >= 2000) score += 20;
        else score += 10;

        String investissement = personne.getInvestissement();
        String placement = personne.getPlacement();

        if (investissement != null && !investissement.isEmpty()) score += 15;
        if (placement != null && !placement.isEmpty()) score += 15;

        return Math.min(100, score);
    }



    private int calculerRelationClient(Personne personne, boolean isExistingClient) {
        int score = 0;
        
        if (isExistingClient) {
            Date createdAt = personne.getCreatedAt();
            if (createdAt != null) {
                int anneesRelation = calculerAnnees(createdAt, new Date());
                if (anneesRelation >= 3) score += 60;
                else if (anneesRelation >= 1) score += 40;
                else score += 20;
            }
            
            if (!hasRecentIncidents(personne)) {
                score += 40;
            }
        } else {
            score = 50;
        }
        
        return Math.min(100, score);
    }
    

    private int calculerCriteresComplementaires(Personne personne) {
        int score = 0;
        
        Date dateNaissance = personne.getDateNaissance();
        if (dateNaissance != null) {
            int age = calculerAge(dateNaissance);
            if (age >= 25 && age <= 50) score += 40;
            else if (age >= 18 && age < 25) score += 30;
            else if (age > 50 && age <= 60) score += 25;
            else score += 10;
        }
        
        String situationFamiliale = personne.getSituationFamiliale();
        if (situationFamiliale != null) {
            switch (situationFamiliale) {
                case "MARIE":
                    score += 30;
                    break;
                case "CELIBATAIRE":
                    score += 20;
                    break;
                case "DIVORCE":
                case "VEUF":
                    score += 15;
                    break;
            }
        }
        
        int nombreEnfants = personne.getNombreEnfants();
        if (nombreEnfants == 0) score += 30;
        else if (nombreEnfants <= 2) score += 25;
        else if (nombreEnfants <= 4) score += 15;
        else score += 5;
        
        return Math.min(100, score);
    }
    


    

    public double calculerCapaciteEmprunt(Personne personne, boolean isExistingClient) {
        double revenu = 0;
        
        if (personne instanceof Employe) {
            revenu = ((Employe) personne).getSalaire();
        } else if (personne instanceof Professionnel) {
            revenu = ((Professionnel) personne).getRevenu();
        }
        
        //int score = calculerScoreGlobal(personne, isExistingClient);

        int score=0;

        if (!isExistingClient) {
            return revenu * 4;
        } else {
            if (score > 80) {
                return revenu * 10;
            } else if (score >= 60) {
                return revenu * 7;
            }
        }
        
        return 0;
    }


    private boolean hasRecentIncidents(Personne personne) {


        return false;
    }

}