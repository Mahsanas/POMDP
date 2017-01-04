
package Main;

import getRandomDate.initProb;
import java.io.FileNotFoundException;
import java.io.IOException;
import pomdp.params;
import pomdp.paramsIN.*;
import pomdp.pomdp_solve;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException{
        initProb ip = new initProb();
        ip.initGlobalMDP(14);
        
       System.out.println("Welcome to POMDP");
       String filename = "examples/testQ.POMDP";//Run the Tiger problem with discount = 0.95
       //String filename = "examples/1d.POMDP"; //1d maze by Littman
       
       
      //initialize param
      params param = new params();
      PomdpSolveParams param2 = param.newPomdpSolveParams(filename);
   
      //initialize problem
      pomdp_solve ps = new pomdp_solve();
      ps.initPomdpSolve(param2);
      
      //Solving the Pomdp 
     // ps.solvePomdp(param2);
     
    }
   

}