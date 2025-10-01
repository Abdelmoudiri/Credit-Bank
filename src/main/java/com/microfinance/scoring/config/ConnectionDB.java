package com.microfinance.scoring.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    private String url = "jdbc:mysql://localhost:3306/microfinance_scoring";
    private String usertName = "root";
    private String pass = "";

    public static ConnectionDB instance=null;
    private  Connection connection;

    private ConnectionDB()
    {
        try{
            connection= DriverManager.getConnection(url,usertName,pass);
            System.out.println("la connectioon est reussi");
        }catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    public static  ConnectionDB getInstance()
    {
        if(instance==null){
            instance = new ConnectionDB();
        }
        return  instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
}