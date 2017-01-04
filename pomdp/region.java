package pomdp;
import java.util.ArrayList;
import lpS.Lpsolve;
import lpS.SolverResults;
import mdp.globalMDP;
import mdp.mdpIN.*;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;
import pomdp.statsIN.*;
import pomdp.paramsIN.PomdpSolveParams;
import pomdp.regionIN.*;
import Enum.Value_Type;
import lpS.SolverReturnStatus;

public class region implements regionIN{
    private double HUGE_VAL = Double.POSITIVE_INFINITY, SMALLEST_PRECISION = 1E-12;
    private int TRUE = 1, FALSE = 0;
    private double[] witness = null;
  //  lp_interface lpIN = new lp_interface();
    globalMDP gm ;
    global g = new global();
    double diff = 0; 
    
/**********************************************************************/
    public region(globalMDP gm){
       this.gm = gm;
    }
/**********************************************************************/
        @Override
    public int findRegionPoint( double[] alpha, AlphaList list, 
                     double[] witness_point, boolean flag,
                     PomdpSolveParams param ) 
    {
      /*
        Checks to see if the alpha vector 'alpha' has a non-empty region
        (measurable area) where it is better than all the other vectors in
        the 'list'. If the region is non-empty the routine returns TRUE with
        the witness_point set to a point in that region.  If there is no
        point where alpha is better, then FALSE is returned.
      */
      lpConstants lp = null;
      int i;
      assert( alpha != null && list != null && param != null):
          "Vector or list is NULL." ;

      /* If the list is initially empty, then we know that any simplex
         point is a witness to the vector 'alpha' being bettwr than the
         list.  For this case we just return a simplex corner and forego
         the LPs.  Note that letting this empty list case pass through
         will not work, since the setUpRegionLp() doesn't not set up the
         proper LP for this case. */ 
      if ( list.length == 0 ) {

        if ( witness_point != null ) {
          witness_point[0] = 1.0;
          for( i = 1; i < gm.getgNumStates(); i++ )
            witness_point[i] = 0.0;
          setWitness(witness);
        } /* if need to return a witness point. */

        if ( flag  )
          diff = HUGE_VAL;

        return ( TRUE );
      }

      /* Set up constraints and rest of memory. */
  
      lp = setUpRegionLP( alpha, list);
      
      /* See if we get a feasible solution to the LP, but if not just
         return FALSE. */
       SolverResults ret = solve(lp);
       g.setLpResutl(ret);//save results;
      if(ret != null){
          if(ret.Status == SolverReturnStatus.ModelCreationFailure || ret.Status == SolverReturnStatus.NA 
                  || ret.Status == SolverReturnStatus.OptimalSolutionNotfound ||ret.Status == SolverReturnStatus.UnknownError)
           return ( FALSE );
        LP_freeLP( lp );             
      }
      /* If the 'diff' argument is not NULL then we return the objective
         value in it. */
      if ( flag )
        diff = ret.Objective;

      /* If the LP is feasible, then we need to make sure the objective
         value is > 0.  We want to objective value to be greater than
         zero, but use an epsilon factor for two reasons: first, numerical
         stability requires this and second, it provides the place where
         optimization can be achieved by boosting this up. Since the
         objective function consists of a single variable with a
         coefficient of '1', the objective value and the solution value of
         that variable should be identical. Alas, this does not always
         seem to be the case because of precision issues.  Thus we only
         claim the result is larger than zero if *both* the objective
         value and the variable value are larger than the epilon value we
         are interested in. */
      System.out.println("feasible "+alpha[0] +" "+alpha[1]);
       if(ret != null){
           if(ret.Objective < param.epsilon || ret.VariableResult[gm.getgNumStates()] <param.epsilon)
               return FALSE;
           /*for(int x = 0; x < gm.getgNumStates(); x++)
              if(( ret.VariableResult[x] < param.epsilon ))   {                 
                LP_freeLP( lp );
               return ( FALSE );
              */
       }
       /*for(int x = 0; x < gm.getgNumStates()+1; x++)
              if(( ret.VariableResult[x] < param.epsilon ))   {                 
                LP_freeLP( lp );
               return ( FALSE );
              }*/
             
      if ( witness_point != null ){
        for( i = 0; i < gm.getgNumStates(); i++ )
          witness_point[i] = ret.VariableResult[i];
        setWitness(witness);
      }
       LP_freeLP( lp );
      return ( TRUE );

    }  /* *findRegionPoint */
public void setWitness(double[] wit){
    witness = wit;
}
public double[] getWitness(){
    return witness;
}
/**********************************************************************/
    public lpConstants setUpRegionLP(double[] alpha, AlphaList orig_list){
        lpConstants lp = new lpConstants();
        assert(alpha != null && orig_list != null ) : "null prameters";
        lp = setUpRegionConstraints(alpha, orig_list, lp);
        lp =  setUpObj(lp);
        return lp;    
    }
/**********************************************************************/

    public lpConstants setUpObj(lpConstants lp){
       //Maximize: x1 + ... + xn + b
       double[] obj = new double[gm.getgNumStates() + 1];
       for(int i = 0; i < gm.getgNumStates(); i++)
           obj[i] = 0.0;
       obj[gm.getgNumStates()] = 1.0;
       lp.obj = obj;
       return lp;
   }
/**********************************************************************/
    public lpConstants setUpRegionConstraints(double[] alpha, AlphaList orig_list,lpConstants lp){
       /* x * 1 = 1
           x * ( item - listitem )- delta >= 0
           d + x (listitem - item) <= 0*/
        double adjust_factor = -1;//for cost type
        int num_variables = gm.getgNumStates() + 1;
        int num_constraint = orig_list.length + 1;
        
        int i;
        assert(alpha != null && orig_list != null): "Null parameters";
        
        if( gm.getgValueType() == Value_Type.REWARD_value_type )
          adjust_factor = 1.0;
        
        ArrayList<double[]> constraints = new ArrayList<double[]>();
        double[] RHS = new double[num_constraint];
        char[] sense = new char[num_constraint];
        
        //First constraint
        int row = 0;
        double[] temp = new double[num_variables];
        for(i = 0; i < gm.getgNumStates(); i++)
            temp[i] = 1;
        temp[gm.getgNumStates()] = 0;//delta = 0
        constraints.add(temp);//add first constraints
        RHS[row] = 1;
        sense[row] = 'E';
        row = 1;
        //the rest of constraints
        AlphaNode node = orig_list.head;
        while(node != null){
            double[] tp = new double[num_variables];
            for(i = 0; i < gm.getgNumStates(); i++){
                tp[i] = (node.alpha[i] - alpha[i]) * adjust_factor;
                
            }
            tp[gm.getgNumStates()] = 1;//add delta
            constraints.add(tp);
            RHS[row] = 0.0;
            sense[row++] = 'L';
            node = node.next;
        }
      
       // System.out.println(constraints.size() + " "+ RHS.length);
      lp.constraints = constraints;
      lp.RHS = RHS;
      lp.sense = sense;
       return lp;
    }

/**********************************************************************/  
   /*
    * Method using to solve the linear problem
    */
   public SolverResults solve(lpConstants lp){
       SolverResults result = new SolverResults();
       //solve lp
       Lpsolve lpsolve = new Lpsolve();
       result = lpsolve.solveLPProblem(lp.constraints, lp.obj, lp.RHS,lp.sense);       
       return result;
   }
   
/**********************************************************************/
   public void LP_freeLP(lpConstants lp){
      lp.RHS = null;
      lp.constraints = null;
      lp.sense = null;
      lp.obj = null;
   }
   
/**********************************************************************/
   public double getDiff(){
       return diff;
   }
}