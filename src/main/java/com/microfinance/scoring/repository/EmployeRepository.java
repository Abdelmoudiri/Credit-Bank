package com.microfinance.scoring.repository;

import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Employe;
import com.microfinance.scoring.model.enums.TypeContrat;
import com.microfinance.scoring.model.enums.Secteur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeRepository {
    
    private ConnectionDB connectionDB;
    
    public EmployeRepository() {
        this.connectionDB = ConnectionDB.getInstance();
    }
    
    // CREATE
    public boolean save(Employe employe) {
        String sqlPersonne = "INSERT INTO personnes (id, nom, prenom, date_naissance, ville, nombre_enfants, investissement, placement, situation_familiale, created_at, score, type_client) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'EMPLOYE')";
        String sqlEmploye = "INSERT INTO employes (personne_id, salaire, anciennete, poste, type_contrat, secteur) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionDB.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne);
                 PreparedStatement stmtEmploye = conn.prepareStatement(sqlEmploye)) {
                
                stmtPersonne.setString(1, UUID.randomUUID().toString());
                stmtPersonne.setString(2, employe.getNom());
                stmtPersonne.setString(3, employe.getPrenom());
                stmtPersonne.setDate(4, new java.sql.Date(employe.getDateNaissance().getTime()));
                stmtPersonne.setString(5, employe.getVille());
                stmtPersonne.setInt(6, employe.getNombreEnfants());
                stmtPersonne.setString(7, employe.getInvestissement());
                stmtPersonne.setString(8, employe.getPlacement());
                stmtPersonne.setString(9, employe.getSituationFamiliale());
                stmtPersonne.setTimestamp(10, new Timestamp(employe.getCreatedAt().getTime()));
                stmtPersonne.setInt(11, employe.getScore());
                
                stmtPersonne.executeUpdate();
                
                stmtEmploye.setString(1, employe.getId().toString());
                stmtEmploye.setDouble(2, employe.getSalaire());
                stmtEmploye.setInt(3, employe.getAnciennete());
                stmtEmploye.setString(4, employe.getPoste());
                stmtEmploye.setString(5, employe.getTypeContrat());
                stmtEmploye.setString(6, employe.getSecteur());
                
                stmtEmploye.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // READ - Find by ID
    public Optional<Employe> findById(UUID id) {
        String sql = "SELECT p.*, e.salaire, e.anciennete, e.poste, e.type_contrat, e.secteur " +
                    "FROM personnes p " +
                    "JOIN employes e ON p.id = e.personne_id " +
                    "WHERE p.id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToEmploye(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // READ - Find all employes
    public List<Employe> findAll() {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT p.*, e.salaire, e.anciennete, e.poste, e.type_contrat, e.secteur " +
                    "FROM personnes p " +
                    "JOIN employes e ON p.id = e.personne_id " +
                    "ORDER BY p.created_at DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employes.add(mapResultSetToEmploye(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return employes;
    }
    
    // READ - Find employees eligible for immediate credit (score > 70, age 25-50, married, CDI, salary > 4000)
    public List<Employe> findEligibleForImmediateCreditEmployees() {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT p.*, e.salaire, e.anciennete, e.poste, e.type_contrat, e.secteur " +
                    "FROM personnes p " +
                    "JOIN employes e ON p.id = e.personne_id " +
                    "WHERE p.score > 70 " +
                    "AND YEAR(CURDATE()) - YEAR(p.date_naissance) BETWEEN 25 AND 50 " +
                    "AND p.situation_familiale = 'MARIE' " +
                    "AND e.type_contrat LIKE '%CDI%' " +
                    "AND e.salaire > 4000 " +
                    "ORDER BY p.score DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employes.add(mapResultSetToEmploye(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return employes;
    }
    
    // UPDATE
    public boolean update(Employe employe) {
        String sqlPersonne = "UPDATE personnes SET nom = ?, prenom = ?, ville = ?, nombre_enfants = ?, investissement = ?, placement = ?, situation_familiale = ?, score = ? WHERE id = ?";
        String sqlEmploye = "UPDATE employes SET salaire = ?, anciennete = ?, poste = ?, type_contrat = ?, secteur = ? WHERE personne_id = ?";
        
        try (Connection conn = connectionDB.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne);
                 PreparedStatement stmtEmploye = conn.prepareStatement(sqlEmploye)) {
                
                // Update personnes table
                stmtPersonne.setString(1, employe.getNom());
                stmtPersonne.setString(2, employe.getPrenom());
                stmtPersonne.setString(3, employe.getVille());
                stmtPersonne.setInt(4, employe.getNombreEnfants());
                stmtPersonne.setString(5, employe.getInvestissement());
                stmtPersonne.setString(6, employe.getPlacement());
                stmtPersonne.setString(7, employe.getSituationFamiliale());
                stmtPersonne.setInt(8, employe.getScore());
                stmtPersonne.setString(9, employe.getId().toString());
                
                stmtPersonne.executeUpdate();
                
                // Update employes table
                stmtEmploye.setDouble(1, employe.getSalaire());
                stmtEmploye.setInt(2, employe.getAnciennete());
                stmtEmploye.setString(3, employe.getPoste());
                stmtEmploye.setString(4, employe.getTypeContrat());
                stmtEmploye.setString(5, employe.getSecteur());
                stmtEmploye.setString(6, employe.getId().toString());
                
                stmtEmploye.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // DELETE
    public boolean delete(UUID id) {
        String sqlEmploye = "DELETE FROM employes WHERE personne_id = ?";
        String sqlPersonne = "DELETE FROM personnes WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtEmploye = conn.prepareStatement(sqlEmploye);
                 PreparedStatement stmtPersonne = conn.prepareStatement(sqlPersonne)) {
                
                // Delete from employes first (foreign key constraint)
                stmtEmploye.setString(1, id.toString());
                stmtEmploye.executeUpdate();
                
                // Delete from personnes
                stmtPersonne.setString(1, id.toString());
                stmtPersonne.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to map ResultSet to Employe
    private Employe mapResultSetToEmploye(ResultSet rs) throws SQLException {
        Employe employe = new Employe();
        
        // Set Personne fields
        employe.setId(UUID.fromString(rs.getString("id")));
        employe.setNom(rs.getString("nom"));
        employe.setPrenom(rs.getString("prenom"));
        employe.setDateNaissance(rs.getDate("date_naissance"));
        employe.setVille(rs.getString("ville"));
        employe.setNombreEnfants(rs.getInt("nombre_enfants"));
        employe.setInvestissement(rs.getString("investissement"));
        employe.setPlacement(rs.getString("placement"));
        employe.setSituationFamiliale(rs.getString("situation_familiale"));
        employe.setCreatedAt(rs.getTimestamp("created_at"));
        employe.setScore(rs.getInt("score"));
        
        // Set Employe specific fields
        employe.setSalaire(rs.getDouble("salaire"));
        employe.setAnciennete(rs.getInt("anciennete"));
        employe.setPoste(rs.getString("poste"));
        employe.setTypeContrat(rs.getString("type_contrat"));
        employe.setSecteur(rs.getString("secteur"));
        
        return employe;
    }
}
