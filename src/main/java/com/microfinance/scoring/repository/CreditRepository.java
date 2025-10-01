package com.microfinance.scoring.repository;

import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Credit;
import com.microfinance.scoring.model.enums.Decision;
import com.microfinance.scoring.model.enums.TypeCredit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CreditRepository {
    
    private ConnectionDB connectionDB;
    
    public CreditRepository() {
        this.connectionDB = ConnectionDB.getInstance();
    }
    
    // CREATE
    public boolean save(Credit credit) {
        String sql = "INSERT INTO credits (id, date_credit, montant_demande, montant_octroye, taux_interet, duree_mois, type_credit, decision, client_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, credit.getId().toString());
            stmt.setDate(2, new java.sql.Date(credit.getDateCredit().getTime()));
            stmt.setDouble(3, credit.getMontantDemande());
            stmt.setDouble(4, credit.getMontantOctroye());
            stmt.setDouble(5, credit.getTauxInteret());
            stmt.setInt(6, credit.getDureeMois());
            stmt.setString(7, credit.getTypeCredit().toString());
            stmt.setString(8, credit.getDecision().toString());
            // stmt.setString(9, credit.getClientId().toString()); // À ajouter selon votre modèle
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // READ - Find by ID
    public Optional<Credit> findById(UUID id) {
        String sql = "SELECT * FROM credits WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // READ - Find all
    public List<Credit> findAll() {
        List<Credit> credits = new ArrayList<>();
        String sql = "SELECT * FROM credits ORDER BY date_credit DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return credits;
    }
    
    // READ - Find by Client ID
    public List<Credit> findByClientId(UUID clientId) {
        List<Credit> credits = new ArrayList<>();
        String sql = "SELECT * FROM credits WHERE client_id = ? ORDER BY date_credit DESC";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientId.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return credits;
    }
    
    // READ - Find by Decision
    public List<Credit> findByDecision(String decision) {
        List<Credit> credits = new ArrayList<>();
        String sql = "SELECT * FROM credits WHERE decision = ? ORDER BY date_credit DESC";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, decision);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return credits;
    }
    
    // READ - Find active credits
    public List<Credit> findActiveCredits() {
        List<Credit> credits = new ArrayList<>();
        String sql = "SELECT c.* FROM credits c " +
                    "JOIN echeances e ON c.id = e.credit_id " +
                    "WHERE e.statut_paiement IN ('ENRETARD', 'IMPAYENONREGLE') " +
                    "OR e.date_echeance > CURRENT_DATE " +
                    "GROUP BY c.id " +
                    "ORDER BY c.date_credit DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return credits;
    }
    
    // READ - Find credits by type and amount range
    public List<Credit> findByTypeAndAmountRange(String typeCredit, double minAmount, double maxAmount) {
        List<Credit> credits = new ArrayList<>();
        String sql = "SELECT * FROM credits WHERE type_credit = ? AND montant_octroye BETWEEN ? AND ? ORDER BY date_credit DESC";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, typeCredit);
            stmt.setDouble(2, minAmount);
            stmt.setDouble(3, maxAmount);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                credits.add(mapResultSetToCredit(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return credits;
    }
    
    // READ - Statistics: Approval rate by contract type
    public List<Object[]> getApprovalRateByContractType() {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT e.type_contrat, " +
                    "COUNT(*) as total_clients, " +
                    "AVG(p.score) as average_score, " +
                    "AVG(e.salaire) as average_salary, " +
                    "COUNT(CASE WHEN c.decision = 'ACCORD_IMMEDIAT' THEN 1 END) * 100.0 / COUNT(*) as approval_rate " +
                    "FROM personnes p " +
                    "JOIN employes e ON p.id = e.personne_id " +
                    "LEFT JOIN credits c ON p.id = c.client_id " +
                    "GROUP BY e.type_contrat " +
                    "ORDER BY approval_rate DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("type_contrat"),
                    rs.getInt("total_clients"),
                    rs.getDouble("average_score"),
                    rs.getDouble("average_salary"),
                    rs.getDouble("approval_rate")
                };
                stats.add(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
    
    // UPDATE
    public boolean update(Credit credit) {
        String sql = "UPDATE credits SET montant_demande = ?, montant_octroye = ?, taux_interet = ?, duree_mois = ?, type_credit = ?, decision = ? WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, credit.getMontantDemande());
            stmt.setDouble(2, credit.getMontantOctroye());
            stmt.setDouble(3, credit.getTauxInteret());
            stmt.setInt(4, credit.getDureeMois());
            stmt.setString(5, credit.getTypeCredit().toString());
            stmt.setString(6, credit.getDecision().toString());
            stmt.setString(7, credit.getId().toString());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // DELETE
    public boolean delete(UUID id) {
        String sql = "DELETE FROM credits WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to map ResultSet to Credit
    private Credit mapResultSetToCredit(ResultSet rs) throws SQLException {
        Credit credit = new Credit();
        // credit.setId(UUID.fromString(rs.getString("id")));
        // credit.setDateCredit(rs.getDate("date_credit"));
        // credit.setMontantDemande(rs.getDouble("montant_demande"));
        // credit.setMontantOctroye(rs.getDouble("montant_octroye"));
        // credit.setTauxInteret(rs.getDouble("taux_interet"));
        // credit.setDureeMois(rs.getInt("duree_mois"));
        // credit.setTypeCredit(rs.getString("type_credit"));
        // credit.setDecision(rs.getString("decision"));
        
        // Note: Vous devrez ajuster selon vos getters/setters réels
        return credit;
    }
}
