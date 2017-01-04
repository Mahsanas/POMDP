package pomdp;

import mdp.globalMDP;
import mdp.mdpIN.*;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;

import pomdp.projectionIN.*;
//Routines for constructing the projection sets {\bar \Gamma}^{a,z}_t.
public class projection implements projectionIN{
   alpha ap;
   globalMDP gm;
   global g;
   
   public projection(){}
   // public void dumpProjections( AlphaList[][] projection ){};
    public projection(alpha ap,globalMDP gm, global g){
        this.ap = ap;
        this.gm = gm;
        this.g = g;
    }
/**********************************************************************/
        @Override
    public AlphaList[][] allocateAllProjections(  ) 
    {
      /*
        Just creates and returns the storage to hold all the projection
        sets.  They will initially all be NULL. 
      */
      int a;
      AlphaList[][] projection;

      /* We need an arrary of projections for each action. */
      projection = new AlphaList[gm.getgNumActions()][gm.getgNumObservations()] ;

      return ( projection );

    }  /* **allocateAllProjections */
    /**********************************************************************/
    public void clearAllProjections( AlphaList[][] projection ) 
    {
      /*
        Just removes the memory for the list themselves and not for the
        2D array which is holding them.
      */
      int a, z;

      if ( projection == null )
        return;

      /* First free the individual alpha vector lists. */
      for ( a = 0; a < gm.getgNumActions(); a++ )
        for ( z = 0; z < gm.getgNumObservations(); z++ ) {

          if ( projection[a][z] == null )
            continue;

          ap.destroyAlphaList( projection[a][z] );
          projection[a][z] = null;
        } /* for z */

    }  /* clearAllProjections */
    /**********************************************************************/
        @Override
    public void freeAllProjections( AlphaList[][] projection ) 
    {
      /*
        Discards all the projection lists and memory associated with them.
      */
      int a;

      if ( projection == null)
        return;

      /* Deallocate the memory for the individual projections first. */
      clearAllProjections( projection );

      /* Deallocate the array of projections we have for each action. */
      for ( a = 0; a < gm.getgNumActions(); a++ )
         projection[a]= null;

       projection = null;

    }  /* freeAllProjections */
    public  AlphaList makeProjection(  AlphaList list, int a, int z ) {
    /*
      Compute the back projection of the list sent in for a particular 
      action and observation.  It also takes the discounting into account.
      and part of the immediate reward.  It distributes the immediate
      reward (which is independent of the observation) evenly among all
      the projection sets, so that the addition of the vectors will
      incorporate the proper immediate reward.
    */
      AlphaList projection, temp;
      double[] alpha = null;
      int j, cur_state;
      
      projection = ap.newAlphaList();
      
      /* We put the action and observation in the list header so we can
         easily identify which projection a particular list is. */
      AlphaNode aNode = new AlphaNode(alpha, a);
      aNode.obs = z;
    
      if ( list == null )
        return ( projection );

      /* It is possible that a particular observation is impossible to
         observe for this given action and the possible resulting states.
         If this happens we will get a vector of all zeroes.  This vector
         of all zeroes is a bit different than what we want.  We want to
         say that there is no value function, not that the value is zero
         everywhere.  When an observation is impossible, we represent this
         with a single vector of 1/|Z| weighted immediate rewards with
         prev_source pointer set to NULL. */
      
      if (  g.getgObservationPossible(a, z) == 0) {
          //System.out.println(g.getgObservationPossible(a,z));
        alpha = ap.newAlpha();
        for ( cur_state = 0; cur_state < gm.getgNumStates(); cur_state++)
          alpha[cur_state] =  gm.getIQ( a, cur_state )  / ((double) gm.getgNumObservations());

        /* Make sure the projections will be added in the proper order. */
        AlphaNode newNode = new AlphaNode(alpha, a);
        newNode.obs =z;
        newNode.prev_source = null;
        projection = ap.appendNodeToAlphaList( projection,newNode);
        return ( projection );
      } /* if impossible observation */
      
        double[][][] P = gm.getIP();
        double[][][] R = gm.getIR();
    
      AlphaNode node = list.head;
      while ( node != null ) {

        alpha = ap.newAlpha();
        
        /* Set projection values */
        for ( cur_state = 0; cur_state < gm.getgNumStates(); cur_state++) {
          alpha[cur_state] = 0.0;
         
          for ( j = 0; j < gm.getgNumStates(); j++ )               
           alpha[cur_state] =  alpha[cur_state] +  P[a][cur_state][j] * R[a][j][z] * node.alpha[j];

          alpha[cur_state] = alpha[cur_state] * gm.getgDiscount();

          /* Now we add a piece of the immediate rewards. This may seem a
             little odd to add only a portion of the immediate rewards
             here.  In fact, the actual values here don't make complete
             sense, but the effect will be that a vector from each
             observations' projection will sum to be the actual new alpha
             vector. Without adding this, we would need to add the extra
             step of adding the immediate rewards making the code not an
             nice. It turns out that adding this constant vector does not
             change any of the properties of the sets that we are
             interested in. IMPORTANT: Because of the way we define the
             projection set for impossible observations, we can also use
             the 1/|Z| weighting of rewards here.  If we did not define
             the impossible observation projections to exist at all then
             it would not enough to use gNumObservations in the
             denominator since some observations are not possible, meaning
             the sum will consust of less vectors than there are
             observations.  If this were the case we would need to use the
             precomputed total non-zero prob. observations for each
             action. */
          alpha[cur_state]  =  alpha[cur_state] + (gm.getIQ( a, cur_state ) / ((double) gm.getgNumObservations()));
          

        }   /* for i */
       // System.out.println();
        /* Make sure the projections will be added in the proper order. */
        AlphaNode tNode = new AlphaNode(alpha,a);
        tNode.prev_source = node;
        tNode.obs = z;
        projection = ap.appendNodeToAlphaList( projection, tNode);

        
        node = node.next;
      }  /* while list */
  
      return ( projection );
    }  /* makeProjection */
    public AlphaList[][] setAllProjections(AlphaList prev_alpha_list ) 
    {
      /*
        Makes all the projected alpha vector lists, which amounts to a
        projected list for each action-observation pair. Stores this as a
        two dimensional array of lists where the first index is the action
        and the other is the observation.

        The 'impossible_obs_epsilon' specifies the tolerance to use when
        trying to determine whether or not a particulat observation is at
        all feabile.
      */
      
      int a, z;
      
      AlphaList[][] projection = new AlphaList[gm.getgNumActions()][gm.getgNumObservations()];
      
      for ( a = 0; a < gm.getgNumActions(); a++ ) {
        for ( z = 0; z < gm.getgNumObservations(); z++ ) {          
          projection[a][z] = makeProjection( prev_alpha_list, a, z );          
        } /* for z */
      } /* for a */
    
      return projection;
    }  /* setAllProjections */
    /**********************************************************************/
        @Override
    public AlphaList[][] makeAllProjections( AlphaList prev_alpha_list ) 
    {
      /*
        Makes all the projected alpha vector lists, which amounts to a
        projected list for each action-observation pair. Stores this as a
        two dimensional array of lists where the first index is the action
        and the other is the observation.  This allocates the space for the
        projections first.

        The 'impossible_obs_epsilon' specifies the tolerance to use when
        trying to determine whether or not a particulat observation is at
        all feabile.
      */

      AlphaList[][] projection;

      /* We cannot project nothing. */
      if ( prev_alpha_list == null )
        return ( null );

      projection = setAllProjections(prev_alpha_list );

      /* Uncomment this if you want to see all the projections in files. */
      /*  dumpProjections( projection ); */
     
      return ( projection );
    }  /* *makeAllProjections */
    /**********************************************************************/
  
    /**********************************************************************/
        @Override
    public void displayProjections( AlphaList[][] projection ) 
    {
      /*
        Displays all projections to file stream.
      */
      int a, z;

      for ( a = 0; a < projection.length; a++ )
        for ( z = 0; z < projection[0].length; z++ ) {

          //fprintf( file, "Projection[a=%d][z=%d] ", a, z );
          ap.displayAlphaList( projection[a][z] );

        } /* for z */

    }  /* displayProjections */
    /**********************************************************************/
        @Override
    public void showProjections( AlphaList[][] projection ) 
    {
      /*
        Displays all projections to stdout.
      */

        displayProjections( projection );

    }  /* showProjections */

    


}
/**********************************************************************/