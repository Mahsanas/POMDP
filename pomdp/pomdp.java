package pomdp;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mdp.globalMDP;
import mdp.mdpIN.*;
import mdp.mdp;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;
import Enum.Value_Type;
import getRandomDate.initProb;
public class pomdp implements pomdpIN, globalIN{
  
    /*int[][] gObservationPossible;
    int[] gNumPossibleObservations;*/
    mdp MDP ;
    globalMDP gm;
    global g;
    /* Some variations will require there to be no negative immediate
       rewards. If this is the case, then this flag should be set. */
       // int gRequireNonNegativeRewards;

    /**********************************************************************/
    public pomdp(){}
    public pomdp(mdp m, globalMDP gm1, global g1){
        MDP = m;
        gm = gm1;
        g = g1;
    }
        @Override
    public double worstPossibleValue(){
    /*
      Often we would like to do some max or min procedure and require
      initialization to the most extreme value.  This routine used to
      detect whether there were rewards or costs for the immediate
      utilities, but because the getImmediateReward() rtouine in global.h
      now enforces everything to be rewards, this is just a simple thing.  
    */
      if(gm.getgValueType() == Value_Type.REWARD_value_type)
          return( -1.0 * HUGE_VAL );
      return HUGE_VAL;

    }  /* worstPossibleValue */
    /**********************************************************************/
        @Override
    public double bestPossibleValue() {
    /*
       Often we would like to do some max or min procedure and require
       initialization to the most extreme value.  This routine used to
      detect whether there were rewards or costs for the immediate
      utilities, but because the getImmediateReward() rtouine in global.h
      now enforces everything to be rewards, this is just a simple thing.  
    */
       if(gm.getgValueType() == Value_Type.REWARD_value_type)
          return HUGE_VAL;
      return( -1.0 * HUGE_VAL );

    }  /* bestPossibleValue */
    @Override
    public int isBetterValue( double new_value, double current, double epsilon ) {
    /* 
       Often we would like to do some max or min procedure and require
       coparing a new value to a current value.  Since the test for which
       is better depends on whether rewards or costs are being used, we
       have encapsulated this in this routine.  We also want to account
       for the precision of the current run (i.e.,
       gDoubleEqualityPrecision.) 
    */

      if( gm.getgValueType() == Value_Type.REWARD_value_type ){
         return(g.LessThan( current, new_value, epsilon));
              
      }
      else 
        return( g.LessThan( new_value, current, epsilon ));

    }  /* isBetterValue */
    
    /**********************************************************************/
    public void setPossibleObservations( double epsilon ) {
    /*
      Sets the global arrays to precomputed values to determine whether or
      not each observation is possible for a given action.  Also stores
      how many observations are possible for each action.
    */
      int a, z, j, cur_state;
      boolean all_zero_prob_obs = true;
      double[][][] P = gm.getIP();
      double[][][] R = gm.getIR();
      
      for ( a = 0; a < gm.getgNumActions(); a++ ) {
        for ( z = 0; z < gm.getgNumObservations(); z++ ) {

          /* We want to check for the case where an observation is
             impossible.  */            
       
          for ( cur_state = 0; cur_state < gm.getgNumStates(); cur_state++)
            for ( j = 0;  j < P[a][cur_state].length;j++ ) 
              if ( ! Equal( gm.getIR(a, cur_state, z ),
                            0.0, epsilon )) {
                all_zero_prob_obs = false;                
              }
          
              if ( all_zero_prob_obs) /* if observation is possible */
                g.setgObservationPossible(a,z,FALSE);
              else  {
                g.setgObservationPossible(a,z,TRUE);
                g.setgNumPossibleObservations(a, 1);
              } 
        } /* for z */

      } /* for a */

      /* A little sanity check. */
      for ( a = 0; a < gm.getgNumActions(); a++ )
        assert( g.getgNumPossibleObservations(a) > 0):
            "Bad POMDP. No observations possible for some action." ;

    }  /* setPossibleObservations */
    /**********************************************************************/
   
        @Override
    public void initializePomdp( String filename, 
                          double obs_possible_epsilon ) {
    /*
      Does the necessary things to read in and set-up a POMDP file.
      Also precomputes which observations are possible and which are not.
    */
      int a;
      //char msg[MAX_MSG_LENGTH];
      if(filename != null){
      if (filename.isEmpty()) {
        System.out.println( "No parameter file specified (Use + +CMD_ARG_HELP_SHORT  +for options.)");
       // System.exit( -1 );
      }
   
      if (  MDP.readMDP( filename ) == 0) {
        System.out.println(  "Could not successfully parse file" );
        //System.exit( -1 );
      } /* if problem parsing POMDP file. */
      }else{
       initProb ip = new initProb(gm);
            try {
                ip.initGlobalMDP();
            } catch (IOException ex) {
                Logger.getLogger(pomdp.class.getName()).log(Level.SEVERE, null, ex);
            }
      }
      /* We'll use this stuff if the setPossibleObservations() routine is
         called. */ 
      g.setgObservationPossible(gm.getgNumActions(),gm.getgNumObservations());


      g.setgNumPossibleObservations(gm.getgNumActions());

      setPossibleObservations( obs_possible_epsilon );

    }  /* initializePomdp */
    /**********************************************************************/
        @Override
    public void cleanUpPomdp(  ) {
    /*
      Deallocates the POMDP read in by initializePomdp().
    */
      int a;

     /* for ( a = 0; a < gm.getgNumActions(); a++ )
            g.setgObservationPossible(a, null);*/
      
      g.cleangObservationPossible();
      g.cleangNumPossibleObservations();

      MDP.deallocateMDP();

    }  /* cleanUpPomdp */
   

   @Override
    public boolean Equal(double d, double d0, double epsilon) {
        if(Math.abs(d - d0) <= epsilon)
            return true;
        return false;
    }
    @Override
    public int LessThan(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean GreaterThan(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initGlobal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cleanUpGlobal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFile(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
/**********************************************************************/
}
