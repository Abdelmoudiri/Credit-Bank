# 🏦 Système de Scoring de Crédit - Microfinance

## 📋 Description du Projet

Ce système de scoring de crédit est conçu pour les institutions de microfinance afin d'automatiser l'évaluation des demandes de crédit et la prise de décision. Il utilise un algorithme de scoring à 5 composants métier pour analyser la solvabilité des clients (employés et professionnels) et génère des décisions automatisées.

## 🎯 Fonctionnalités Principales

### 🔍 Système de Scoring (5 Composants)
1. **Stabilité Professionnelle** - Analyse de la situation d'emploi et de l'ancienneté
2. **Capacité Financière** - Évaluation des revenus et de la capacité de remboursement
3. **Historique de Paiement** - Analyse des antécédents de crédit et incidents
4. **Relation Client** - Évaluation de la fidélité et de l'engagement
5. **Patrimoine** - Analyse des biens et garanties

### ⚡ Moteur de Décision Automatisé
- **Accord Immédiat** : Score élevé (> 75%)
- **Étude Manuelle** : Score moyen (50-75%)
- **Refus Automatique** : Score faible (< 50%)

### 🗄️ Gestion Complète des Données
- Gestion des clients (Employés et Professionnels)
- Suivi des crédits et échéances
- Historique des incidents de paiement
- Audit trail complet des décisions

## 🏗️ Architecture Technique

### Structure du Projet
```
src/main/java/com/microfinance/scoring/
├── Main.java                          # Point d'entrée de l'application
├── config/
│   └── ConnectionDB.java              # Gestionnaire de connexion BD (Singleton)
├── model/
│   ├── Personne.java                  # Classe abstraite de base
│   ├── Employe.java                   # Modèle employé
│   ├── Professionnel.java             # Modèle professionnel
│   ├── Credit.java                    # Modèle de crédit
│   ├── Echeance.java                  # Modèle d'échéance
│   ├── Incident.java                  # Modèle d'incident
│   ├── ResultatDemandeCredit.java     # Résultat de traitement
│   └── enums/                         # Énumérations métier
├── repository/
│   ├── EmployeRepository.java         # Repository spécialisé employés
│   ├── ProfessionnelRepository.java   # Repository spécialisé professionnels
│   ├── CreditRepository.java          # Gestion des crédits
│   ├── EcheanceRepository.java        # Gestion des échéances
│   └── IncidentRepository.java        # Gestion des incidents
├── service/
│   ├── ScoringService.java            # Service de calcul de score
│   ├── DecisionEngine.java            # Moteur de décision
│   ├── CreditService.java             # Orchestration des crédits
│   ├── EmployeService.java            # Services métier employés
│   └── AnalyticsService.java          # Services d'analyse
└── ui/
    ├── MainApp.java                   # Interface principale
    ├── MenuClient.java                # Menu gestion clients
    └── MenuCredit.java                # Menu gestion crédits
```

### Technologies Utilisées
- **Java 8+** - Langage principal
- **JDBC** - Connectivité base de données
- **MySQL** - Base de données relationnelle
- **Pattern Repository** - Architecture de données
- **Pattern Singleton** - Gestion des connexions
- **Pattern Service** - Logique métier

## 🚀 Installation et Configuration

### Prérequis
- Java JDK 8 ou supérieur
- MySQL 5.7 ou supérieur
- IDE Java (IntelliJ IDEA, Eclipse, etc.)
- Connecteur MySQL JDBC

### Configuration Base de Données

1. **Créer la base de données** :
```sql
-- Exécuter le script database_creation_script.sql
-- Contient toutes les tables nécessaires avec les contraintes
```

2. **Configurer la connexion** :
```java
// Dans ConnectionDB.java, adapter les paramètres :
private static final String URL = "jdbc:mysql://localhost:3306/microfinance_db";
private static final String USERNAME = "votre_username";
private static final String PASSWORD = "votre_password";
```

### Lancement de l'Application

1. **Compilation** :
```bash
javac -cp "lib/*" src/main/java/com/microfinance/scoring/*.java
```

2. **Exécution** :
```bash
java -cp "lib/*:src/main/java" com.microfinance.scoring.Main
```

## 🔧 Utilisation

### Traitement d'une Demande de Crédit

```java
// Exemple d'utilisation du CreditService
CreditService creditService = new CreditService();

// Traiter une demande
ResultatDemandeCredit resultat = creditService.traiterDemandeCredit(
    clientId, 
    montantDemande, 
    dureeEnMois, 
    typeCredit
);

// Analyser le résultat
if (resultat.estApprouve()) {
    System.out.println("Crédit approuvé automatiquement");
    System.out.println("Montant : " + resultat.getMontantApprouve());
} else if (resultat.estEnEtudeManuelle()) {
    System.out.println("Dossier en étude manuelle");
} else {
    System.out.println("Crédit refusé : " + resultat.getMotifDecision());
}
```

### Calcul de Score Personnalisé

```java
// Utilisation directe du ScoringService
ScoringService scoringService = new ScoringService();
double score = scoringService.calculerScore(clientId, montantDemande);
```

## 📊 Algorithme de Scoring

### Pondération des Composants
- **Stabilité Professionnelle** : 25%
- **Capacité Financière** : 30%
- **Historique de Paiement** : 25%
- **Relation Client** : 10%
- **Patrimoine** : 10%

### Critères d'Évaluation

#### Stabilité Professionnelle
- Ancienneté dans l'emploi
- Type de contrat (CDI favorisé)
- Secteur d'activité
- Stabilité des revenus

#### Capacité Financière
- Ratio dette/revenu
- Revenus nets mensuels
- Charges fixes
- Capacité de remboursement

#### Historique de Paiement
- Nombre d'incidents de paiement
- Ancienneté des incidents
- Montants impliqués
- Régularité des paiements

## 🗃️ Modèle de Données

### Tables Principales
- `personnes` - Données de base des clients
- `employes` - Informations spécifiques aux employés
- `professionnels` - Informations spécifiques aux professionnels
- `credits` - Données des crédits accordés
- `echeances` - Planning de remboursement
- `incidents` - Historique des incidents de paiement

### Relations
- Un client peut avoir plusieurs crédits
- Un crédit a plusieurs échéances
- Un client peut avoir plusieurs incidents
- Traçabilité complète des décisions

## 🔒 Sécurité et Bonnes Pratiques

### Gestion des Connexions
- Pattern Singleton pour `ConnectionDB`
- Fermeture automatique des ressources
- Gestion des transactions

### Validation des Données
- Validation des montants et durées
- Contrôle des types de crédit
- Vérification de l'existence des clients

### Logging et Audit
- Traçabilité des décisions de crédit
- Horodatage de toutes les opérations
- Conservation des motifs de décision

## 🎯 Roadmap et Améliorations Futures

### Version 2.0 (Prévue)
- [ ] Interface web avec Spring Boot
- [ ] API REST pour intégrations externes
- [ ] Système de notifications automatiques
- [ ] Rapports et tableaux de bord avancés

### Améliorations Techniques
- [ ] Tests unitaires complets
- [ ] Configuration externalisée
- [ ] Cache pour les calculs de score
- [ ] Monitoring et métriques

## 👥 Contribution

### Structure des Commits
```
feat: nouvelle fonctionnalité
fix: correction de bug
docs: mise à jour documentation
refactor: refactoring du code
test: ajout de tests
```

### Guidelines de Développement
1. Respecter les patterns existants
2. Documenter les méthodes publiques
3. Ajouter des tests pour les nouvelles fonctionnalités
4. Suivre les conventions de nommage Java

## 📞 Support

Pour toute question ou problème :
- Consulter la documentation du code
- Vérifier les logs d'application
- Contacter l'équipe de développement

## 📄 Licence

Ce projet est développé dans le cadre d'un brief académique pour l'apprentissage des systèmes de scoring financier.

---

**Développé avec ❤️ pour l'automatisation du crédit microfinance**