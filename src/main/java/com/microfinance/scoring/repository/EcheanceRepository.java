package com.microfinance.scoring.repository;

import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Echeance;
import com.microfinance.scoring.model.enums.StatutPaiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EcheanceRepository {
    
    private ConnectionDB connectionDB;
    
    public EcheanceRepository() {
        this.connectionDB = ConnectionDB.getInstance();
    }
    
    // CREATE
    public boolean save(Echeance echeance) {
        String sql = "INSERT INTO echeances (id, date_echeance, mensualite, date_paiement, statut_paiement, credit_id) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = connectionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, echeance.getId().toString());
            stmt.setDate(2, new java.sql.Date(echeance.getDateEcheance().getTime()));
            stmt.setDouble(3, echeance.getMensualite());
            stmt.setDate(4, echeance.getDatePaiement() != null ? 
                        new java.sql.Date(echeance.getDatePaiement().getTime()) : null);
            stmt.setString(5, echeance.getStatutPaiement());

            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // READ - Find by ID
    public Optional<Echeance> findById(UUID id) {
        String sql = "SELECT * FROM echeances WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToEcheance(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // READ - Find all
    public List<Echeance> findAll() {
        List<Echeance> echeances = new ArrayList<>();
        String sql = "SELECT * FROM echeances";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return echeances;
    }
    
    // READ - Find by Credit ID
    public List<Echeance> findByCreditId(UUID creditId) {
        List<Echeance> echeances = new ArrayList<>();
        String sql = "SELECT * FROM echeances WHERE credit_id = ? ORDER BY date_echeance";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, creditId.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return echeances;
    }
    
    // READ - Find overdue payments
    public List<Echeance> findOverduePayments() {
        List<Echeance> echeances = new ArrayList<>();
        String sql = "SELECT * FROM echeances WHERE date_echeance < CURRENT_DATE AND statut_paiement IN ('ENRETARD', 'IMPAYENONREGLE')";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                echeances.add(mapResultSetToEcheance(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return echeances;
    }
    
    // UPDATE
    public boolean update(Echeance echeance) {
        String sql = "UPDATE echeances SET date_echeance = ?, mensualite = ?, date_paiement = ?, statut_paiement = ? WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, new java.sql.Date(echeance.getDateEcheance().getTime()));
            stmt.setDouble(2, echeance.getMensualite());
            stmt.setDate(3, echeance.getDatePaiement() != null ? 
                        new java.sql.Date(echeance.getDatePaiement().getTime()) : null);
            stmt.setString(4, echeance.getStatutPaiement());
            stmt.setString(5, echeance.getId().toString());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // DELETE
    public boolean delete(UUID id) {
        String sql = "DELETE FROM echeances WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to map ResultSet to Echeance
    private Echeance mapResultSetToEcheance(ResultSet rs) throws SQLException {
        Echeance echeance = new Echeance();
        
        echeance.setDateEcheance(rs.getDate("date_echeance"));
        echeance.setMensualite(rs.getDouble("mensualite"));
        echeance.setDatePaiement(rs.getDate("date_paiement"));
        echeance.setStatutPaiement(rs.getString("statut_paiement"));
        
        return echeance;
    }
}
