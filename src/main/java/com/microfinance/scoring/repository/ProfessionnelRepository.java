package com.microfinance.scoring.repository;

import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Professionnel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des professionnels
 * Fournit les opérations CRUD et les requêtes métier spécialisées
 */
public class ProfessionnelRepository {
    
    private ConnectionDB connectionDB;
    
    public ProfessionnelRepository() {
        this.connectionDB = ConnectionDB.getInstance();
    }
    
    // CREATE - Sauvegarder un nouveau professionnel
    public boolean save(Professionnel professionnel) {
        String sqlPersonne = "INSERT INTO personnes (id, nom, prenom, date_naissance, ville, nombre_enfants, investissement, placement, situation_familiale, created_at, score, type_client) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PROFESSIONNEL')";
        String sqlProfessionnel = "INSERT INTO professionnels (personne_id, revenu, immatriculation_fiscale, secteur_activite, activite) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = connectionDB.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne);
                 PreparedStatement stmtProfessionnel = conn.prepareStatement(sqlProfessionnel)) {
                
                // Insert into personnes table
                stmtPersonne.setString(1, professionnel.getId().toString());
                stmtPersonne.setString(2, professionnel.getNom());
                stmtPersonne.setString(3, professionnel.getPrenom());
                stmtPersonne.setDate(4, new java.sql.Date(professionnel.getDateNaissance().getTime()));
                stmtPersonne.setString(5, professionnel.getVille());
                stmtPersonne.setInt(6, professionnel.getNombreEnfants());
                stmtPersonne.setString(7, professionnel.getInvestissement());
                stmtPersonne.setString(8, professionnel.getPlacement());
                stmtPersonne.setString(9, professionnel.getSituationFamiliale());
                stmtPersonne.setTimestamp(10, new Timestamp(professionnel.getCreatedAt().getTime()));
                stmtPersonne.setInt(11, professionnel.getScore());
                
                stmtPersonne.executeUpdate();
                
                // Insert into professionnels table
                stmtProfessionnel.setString(1, professionnel.getId().toString());
                stmtProfessionnel.setDouble(2, professionnel.getRevenu());
                stmtProfessionnel.setString(3, professionnel.getImmatriculationFiscale());
                stmtProfessionnel.setString(4, professionnel.getSecteurActivite());
                stmtProfessionnel.setString(5, professionnel.getActivite());
                
                stmtProfessionnel.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // READ - Trouver par ID
    public Optional<Professionnel> findById(UUID id) {
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "WHERE p.id = ?";
        
        Connection conn = connectionDB.getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // READ - Trouver tous les professionnels
    public List<Professionnel> findAll() {
        List<Professionnel> professionnels = new ArrayList<>();
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "ORDER BY p.created_at DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                professionnels.add(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return professionnels;
    }
    
    // READ - Trouver par secteur d'activité
    public List<Professionnel> findBySecteurActivite(String secteurActivite) {
        List<Professionnel> professionnels = new ArrayList<>();
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "WHERE pr.secteur_activite = ? " +
                    "ORDER BY pr.revenu DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, secteurActivite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                professionnels.add(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return professionnels;
    }
    
    // READ - Trouver professionnels éligibles aux prêts business
    public List<Professionnel> findEligibleForBusinessLoans() {
        List<Professionnel> professionnels = new ArrayList<>();
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "WHERE p.score >= 70 " +
                    "AND pr.revenu >= 5000 " +
                    "AND pr.immatriculation_fiscale IS NOT NULL " +
                    "AND pr.secteur_activite IN ('SERVICE', 'COMMERCE', 'SANTE', 'EDUCATION') " +
                    "ORDER BY p.score DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                professionnels.add(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return professionnels;
    }
    
    // READ - Trouver professionnels à hauts revenus (>= 8000 DH)
    public List<Professionnel> findHighIncomeProfessionnels() {
        List<Professionnel> professionnels = new ArrayList<>();
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "WHERE pr.revenu >= 8000 " +
                    "ORDER BY pr.revenu DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                professionnels.add(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return professionnels;
    }
    
    // READ - Trouver professionnels avec immatriculation fiscale
    public List<Professionnel> findWithFiscalRegistration() {
        List<Professionnel> professionnels = new ArrayList<>();
        String sql = "SELECT p.*, pr.revenu, pr.immatriculation_fiscale, pr.secteur_activite, pr.activite " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "WHERE pr.immatriculation_fiscale IS NOT NULL AND pr.immatriculation_fiscale != '' " +
                    "ORDER BY p.score DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                professionnels.add(mapResultSetToProfessionnel(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return professionnels;
    }
    
    // READ - Statistiques par secteur d'activité
    public List<Object[]> getStatisticsBySectorActivity() {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT pr.secteur_activite, " +
                    "COUNT(*) as total_professionnels, " +
                    "AVG(p.score) as average_score, " +
                    "AVG(pr.revenu) as average_revenue, " +
                    "COUNT(CASE WHEN pr.immatriculation_fiscale IS NOT NULL THEN 1 END) * 100.0 / COUNT(*) as fiscal_registration_rate " +
                    "FROM personnes p " +
                    "JOIN professionnels pr ON p.id = pr.personne_id " +
                    "GROUP BY pr.secteur_activite " +
                    "ORDER BY average_score DESC";
        
        Connection conn = connectionDB.getConnection();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("secteur_activite"),
                    rs.getInt("total_professionnels"),
                    rs.getDouble("average_score"),
                    rs.getDouble("average_revenue"),
                    rs.getDouble("fiscal_registration_rate")
                };
                stats.add(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
    
    // UPDATE - Mettre à jour un professionnel
    public boolean update(Professionnel professionnel) {
        String sqlPersonne = "UPDATE personnes SET nom = ?, prenom = ?, ville = ?, nombre_enfants = ?, investissement = ?, placement = ?, situation_familiale = ?, score = ? WHERE id = ?";
        String sqlProfessionnel = "UPDATE professionnels SET revenu = ?, immatriculation_fiscale = ?, secteur_activite = ?, activite = ? WHERE personne_id = ?";
        
        Connection conn = connectionDB.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne);
                 PreparedStatement stmtProfessionnelStmt = conn.prepareStatement(sqlProfessionnel)) {
                
                // Update personnes table
                stmtPersonne.setString(1, professionnel.getNom());
                stmtPersonne.setString(2, professionnel.getPrenom());
                stmtPersonne.setString(3, professionnel.getVille());
                stmtPersonne.setInt(4, professionnel.getNombreEnfants());
                stmtPersonne.setString(5, professionnel.getInvestissement());
                stmtPersonne.setString(6, professionnel.getPlacement());
                stmtPersonne.setString(7, professionnel.getSituationFamiliale());
                stmtPersonne.setInt(8, professionnel.getScore());
                stmtPersonne.setString(9, professionnel.getId().toString());
                
                stmtPersonne.executeUpdate();
                
                // Update professionnels table
                stmtProfessionnelStmt.setDouble(1, professionnel.getRevenu());
                stmtProfessionnelStmt.setString(2, professionnel.getImmatriculationFiscale());
                stmtProfessionnelStmt.setString(3, professionnel.getSecteurActivite());
                stmtProfessionnelStmt.setString(4, professionnel.getActivite());
                stmtProfessionnelStmt.setString(5, professionnel.getId().toString());
                
                stmtProfessionnelStmt.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // DELETE - Supprimer un professionnel
    public boolean delete(UUID id) {
        String sqlProfessionnel = "DELETE FROM professionnels WHERE personne_id = ?";
        String sqlPersonne = "DELETE FROM personnes WHERE id = ?";
        
        Connection conn = connectionDB.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtProfessionnel = conn.prepareStatement(sqlProfessionnel);
                 PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne)) {
                
                // Delete from professionnels first (foreign key constraint)
                stmtProfessionnel.setString(1, id.toString());
                stmtProfessionnel.executeUpdate();
                
                // Delete from personnes
                stmtPersonne.setString(1, id.toString());
                stmtPersonne.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Méthode utilitaire pour mapper ResultSet vers Professionnel
    private Professionnel mapResultSetToProfessionnel(ResultSet rs) throws SQLException {
        Professionnel professionnel = new Professionnel();
        
        // Set Personne fields
        professionnel.setId(UUID.fromString(rs.getString("id")));
        professionnel.setNom(rs.getString("nom"));
        professionnel.setPrenom(rs.getString("prenom"));
        professionnel.setDateNaissance(rs.getDate("date_naissance"));
        professionnel.setVille(rs.getString("ville"));
        professionnel.setNombreEnfants(rs.getInt("nombre_enfants"));
        professionnel.setInvestissement(rs.getString("investissement"));
        professionnel.setPlacement(rs.getString("placement"));
        professionnel.setSituationFamiliale(rs.getString("situation_familiale"));
        professionnel.setCreatedAt(rs.getTimestamp("created_at"));
        professionnel.setScore(rs.getInt("score"));
        
        // Set Professionnel specific fields
        professionnel.setRevenu(rs.getDouble("revenu"));
        professionnel.setImmatriculationFiscale(rs.getString("immatriculation_fiscale"));
        professionnel.setSecteurActivite(rs.getString("secteur_activite"));
        professionnel.setActivite(rs.getString("activite"));
        
        return professionnel;
    }
}