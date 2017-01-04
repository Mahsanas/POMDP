
package lpS;
import java.util.ArrayList;
import lpsolve.*;

public class Lpsolve{

public static SolverResults solveLPProblem(ArrayList<double[]> Constraints, double[] ObjF, double[] RHS,char[] sense) {                             
    //The Solution object which will be returned         
    
    SolverResults Sol = new SolverResults();   
    if(Constraints.isEmpty())
        return null;
    try {            
        int NbColumns = Constraints.get(0).length;
        int NbRows = Constraints.size();   
        int ret = 0;  
        //Create Lpsolve linear problem   
        LpSolve LpProb = LpSolve.makeLp(0, NbColumns);     
        if(LpProb.getLp() == 0) {               
            //Linear problem could not be constructed.      
            Sol.Status = SolverReturnStatus.ModelCreationFailure;   
            ret = 1;           
        }  
    if (ret == 0) {                          //Linear problem constructed OK to proceed.      
        //Set AddRowMode to true                    
        LpProb.setAddRowmode(true);            
        //Create column index Array (used to add constraints)    
        int[] ColNo = new int[NbColumns];                
        for (int i = 0; i < NbColumns;) {   
            ColNo[i++] = i;
        }                         
    //Add constraints      
        for (int j = 0; j < NbRows; j++) {   
            if(sense[j] == 'L')
                LpProb.addConstraintex(NbColumns, Constraints.get(j), ColNo, LpSolve.LE, RHS[j]); 
            else if(sense[j] == 'E')
                LpProb.addConstraintex(NbColumns, Constraints.get(j), ColNo, LpSolve.EQ, RHS[j]);
            else
                LpProb.addConstraintex(NbColumns, Constraints.get(j), ColNo, LpSolve.GE, RHS[j]);
        }          
        
        //Set Add Row Mode back to false           
        LpProb.setAddRowmode(false);             
        
        //Set Objective Function        
        LpProb.setObjFnex(NbColumns, ObjF, ColNo);   
        
        //Set Direction  Maximize            
        LpProb.setMaxim();
        //LpProb.writeLp("model.lp");
        //Solve the Linear Problem       
        ret = LpProb.solve();     
        if (ret == LpSolve.OPTIMAL) {  
            ret = 0;           
        }              
        else {            
            ret = 1;      
            Sol.Status = SolverReturnStatus.OptimalSolutionNotfound;   
        }                                                     
        //Store Optimisation values     
        if (ret == 0) {    
            double[] VariableResult = new double[NbColumns];                              
            double[] ConstraintResult = new double[NbRows];   
            double Objective = 0;        
            double[] DualResult = new double[NbColumns + NbRows + 1];  
            double[] Weights = new double[NbRows];              
            //Get Primal Solution (variables values)        
            LpProb.getVariables(VariableResult);            
            //Get Objective Value 
            Objective = LpProb.getObjective();   
            //Get Dual Solution (weights)  
            LpProb.getConstraints(ConstraintResult);    
            LpProb.getDualSolution(DualResult);    
            System.arraycopy(DualResult, NbColumns + 1, Weights, 0, NbRows);    
            Sol.ConstraintResult = ConstraintResult;         
            Sol.DualResult = DualResult;        
            Sol.Objective = Objective;    
            Sol.VariableResult = VariableResult;  
            Sol.Weights = Weights;            
            Sol.Status = SolverReturnStatus.OptimalSolutionFound;   
    }             
    }                  
    }                    
    catch (LpSolveException e) {        
        e.printStackTrace();
        Sol.Status = SolverReturnStatus.UnknownError;  
    }                         
    return Sol;         
}  

}
