package com.microfinance.scoring.repository;

import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Incident;
import com.microfinance.scoring.model.enums.TypeIncident;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IncidentRepository {
    
    private ConnectionDB connectionDB;
    
    public IncidentRepository() {
        this.connectionDB = ConnectionDB.getInstance();
    }
    
    // CREATE
    public boolean save(Incident incident) {
        String sql = "INSERT INTO incidents (id, date_incident, score, type_incident, echeance_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, incident.getId().toString());
            stmt.setDate(2, new java.sql.Date(incident.getDateIncident().getTime()));
            stmt.setInt(3, incident.getScore());
            stmt.setString(4, incident.getTypeIncident());
            // stmt.setString(5, incident.getEcheanceId().toString()); // À ajouter selon votre modèle
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // READ - Find by ID
    public Optional<Incident> findById(UUID id) {
        String sql = "SELECT * FROM incidents WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToIncident(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // READ - Find all
    public List<Incident> findAll() {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM incidents ORDER BY date_incident DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return incidents;
    }
    
    // READ - Find by Echeance ID
    public List<Incident> findByEcheanceId(UUID echeanceId) {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM incidents WHERE echeance_id = ? ORDER BY date_incident DESC";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, echeanceId.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return incidents;
    }
    
    // READ - Find recent incidents (last 6 months)
    public List<Incident> findRecentIncidents() {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM incidents WHERE date_incident >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH) ORDER BY date_incident DESC";
        
        try (Connection conn = connectionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return incidents;
    }
    
    // READ - Find incidents by type
    public List<Incident> findByType(String typeIncident) {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM incidents WHERE type_incident = ? ORDER BY date_incident DESC";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, typeIncident);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return incidents;
    }
    
    // UPDATE
    public boolean update(Incident incident) {
        String sql = "UPDATE incidents SET date_incident = ?, score = ?, type_incident = ? WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, new java.sql.Date(incident.getDateIncident().getTime()));
            stmt.setInt(2, incident.getScore());
            stmt.setString(3, incident.getTypeIncident());
            stmt.setString(4, incident.getId().toString());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // DELETE
    public boolean delete(UUID id) {
        String sql = "DELETE FROM incidents WHERE id = ?";
        
        try (Connection conn = connectionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to map ResultSet to Incident
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        Incident incident = new Incident();
        incident.setId(UUID.fromString(rs.getString("id")));
        incident.setDateIncident(rs.getDate("date_incident"));
        incident.setScore(rs.getInt("score"));
        incident.setTypeIncident(rs.getString("type_incident"));
        
        return incident;
    }
}
