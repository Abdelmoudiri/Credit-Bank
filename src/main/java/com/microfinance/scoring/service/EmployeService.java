package com.microfinance.scoring.service;

import com.microfinance.scoring.model.Employe;
import com.microfinance.scoring.model.Personne;
import com.microfinance.scoring.repository.EmployeRepository;
import com.microfinance.scoring.repository.ProfessionnelRepository;

public class EmployeService {
   private EmployeRepository employeRepository;
   private ProfessionnelRepository professionnelRepository;

   public EmployeService()
   {
       employeRepository=new EmployeRepository();
       professionnelRepository=new ProfessionnelRepository();
   }

   public boolean saveEmploye(Employe p)
   {

           if(employeRepository.save(p))
           {
               return true;
           }
           return false;
   }

}
