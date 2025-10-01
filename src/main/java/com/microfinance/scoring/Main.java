package com.microfinance.scoring;


import com.microfinance.scoring.config.ConnectionDB;
import com.microfinance.scoring.model.Employe;
import com.microfinance.scoring.repository.EmployeRepository;
import com.microfinance.scoring.service.EmployeService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args)
    {
        ConnectionDB conn;
        conn=ConnectionDB.getInstance();
        Employe ee=new Employe("mohmaed","saadi","Safi",new Date(),4,"agri","safi","divorcé",new Date(),44,20.,10,"chef","CDI","agriculture");

        EmployeService es=new EmployeService();

        es.saveEmploye(ee);

//        System.out.println("hhghjilkjhg");
//
//        Employe ee=new Employe("mohmaed","saadi","Safi",new Date(),4,"agri","safi","divorcé",new Date(),44,20000000000.,new Date(),"chef","CDI","agriculture");
//        EmployeRepository em=new EmployeRepository();
//        em.save(ee);
//
//        List<Employe> ll=new ArrayList<>();
//
//        ll=em.findAll();
//
//        for(Employe e :ll){
//            System.out.println(e);
//        }
//

    }



}
