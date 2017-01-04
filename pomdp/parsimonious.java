package pomdp;
import Enum.PurgeOption;
import mdp.globalMDP;
import mdp.mdpIN.*;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;
import pomdp.paramsIN.PomdpSolveParams;
import pomdp.statsIN.*;
import pomdp.randomIN.*;
import pomdp.parsimoniousIN.*;
import pomdp.regionIN.*;



public class parsimonious implements parsimoniousIN, globalIN{
/**********************************************************************/
/*******  Routines that are optimizations to speed up pruning   *******/
/**********************************************************************/
     alpha ap ;
     region rg ;
     random rd;
     global g ;
     globalMDP gm ;
     double max_diff = 0;
    public parsimonious(alpha ap, region rg, random rd, global g, globalMDP gm){
        this.ap = ap;
        this.rg = rg;
        this.rd = rd;
        this.g = g;
        this.gm = gm;
    }

    parsimonious() {
        
    }
/**********************************************************************/
        @Override
    public int isEmptyRegionSimpleCheck( AlphaList list, 
                              double[] alpha,
                              double epsilon,
                              int domination_check ) 
    {
      /*
        There are a number of simple checks that can be made to determine if
        the region will be non-empty.  First, if the 'alpha' vector is
        already in the list.  Second, if something in the list
        component-wise dominates the vector.  This routine does these checks
        and returns TRUE if region can easily be determined to be empty and
        FALSE if the simple check reveals nothing.
      */
      AlphaNode node;

      if (( list == null )
          || ( alpha == null))
        return ( TRUE );

      /* Check if item is already in the list. */
      node = ap.findAlphaVector( list, alpha, epsilon );

      if ( node != null )
        return ( TRUE );
      PomdpSolveParams psp = new PomdpSolveParams();
      /* See if it is component-wise dominated. */
      if ( psp.domination_check == 1
           && ap.dominatedAlphaList( alpha, list ))
        return ( TRUE );

      return ( FALSE );

    }  /* isEmptyRegionSimpleCheck */
    /**********************************************************************/
        @Override
    public AlphaList markBestAtSimplexVertices( AlphaList list, 
                               int save_witness_points, 
                               double epsilon ) 
    {
      /* 
         Sets the 'mark' field of each vector that dominates at some belief
         simplex vertex.

         A difference between this routine and initWithSimplexCornersQ()
         is that this vector does not have to construct the vectors for the
         points, it simply picks them out of the list provided.  This is
         used in the prune algorithm to initialize the list.

         Will loop through all the belief simplex vertices and find the
         best vectors at these points from the list.  
      */

      AlphaNode node = new AlphaNode();
      double best_value = 0.0;
      int i;

      assert( list != null): "List is NULL.markBestAtSimplexVertices" ;

      /* If the list is empty, we really shouldn't be calling this
         routine.  CHances are something went wrong somewhere. */
      assert ( list.length != 0): "Cannot mark an empty list.";

      /* We will actually need a belief point to generate the vector, so
         we will initialize it to all zeroes and set each component to 1.0
         as we need it. */
      for( i = 0; i < gm.getgNumStates(); i++ ) 
        g.setgTempBelief(i, 0.0);

      for( i = 0; i < gm.getgNumStates(); i++ ) {

        /* Set this so we actually have a simplex corner in 'b'. */
        g.setgTempBelief(i, 1.0);//gTempBelief[i] = 1.0;

        node = ap.BestVector( list, g.getgTempBelief(),epsilon );
        //System.out.println("best vectorjjjjjjjjjjjjjjjjjjj "+ node.alpha[0] +" "+node.alpha[1]);
        /* It is possible that this vector had already been marked, so
           only need to mark it and consider whether to save a witness
           point for it if it is not yet marked. */
        AlphaNode temp = list.head;
        while(temp != null){
            if(temp == node){
                temp.mark = TRUE;
                if ( save_witness_points == TRUE )
                    temp.witness = g.getgTempBelief();
                break;
            }
            temp = temp.next;
        }
       
        /* Clear the 'i'th component so we maintain a belief corner point
           during i+1. */
        g.setgTempBelief(i, 0.0);//gTempBelief[i] = 0.0;

       }  /* for i */
      System.out.println("Simplex marked");
      ap.displayAlphaList(list);
       return list;
    }  /* markBestAtSimplexVertices */
    /**********************************************************************/
        @Override
    public AlphaList markBestAtRandomPoints( AlphaList list, 
                            int num_points, 
                            int save_witness_points,
                            double epsilon ) 
    {
      /* 
         Will generate 'num_points' random belief points and mark the
         vectors at these points to the list.  
      */

      AlphaNode node;
      //double best_value = 0.0;
      int i;

      assert( list != null): "List is NULL.markBestAtRandomPoints" ;
      if ( num_points < 1 )
        return list;

      for( i = 0; i < num_points; i++ ) {

        /* Get a random belief point, uniformly distributed over the
           belief simplex. */
        double[] x = rd.setRandomDistribution(gm.getgNumStates() );

        node = ap.bestVector( list, x,  epsilon  );

        /* It is possible that this vector had already been marked, so
           only need to mark it and consider whether to save a witness
           point for it if it is not yet marked. */
           AlphaNode temp = list.head;
        while(temp != null){
            if(temp == node){
                temp.mark = TRUE;
                if ( save_witness_points == TRUE )
                    temp.witness = x;
                break;
            }
            temp = temp.next;
        }
        

      }  /* for i */
      
      return list;
    }  /* markBestAtRandomPoints */
    /**********************************************************************/



    /**********************************************************************/
    /*******   Main routines for finding parsimonious sets   **************/
    /**********************************************************************/

    /**********************************************************************/
    public int isEpsilonApproximation( AlphaList test_list,
                            AlphaList orig_list,
                            PomdpSolveParams param  ) 
    {
    /*
      Determines where or not test_list is an epsilon approximation of the
      original list.  Returns TRUE if there is no place where orig_list is
      more than epsilon better than test_list and FALSE if test_list is
      not an epsilon approximation.
    */
      AlphaNode vector = null;
      double diff =0.0;

      max_diff = 0;

      /* We check every vector in the original list to see if it yields a
         value that is better than the test_list because this tells us
         what we need to know. */
      for(  vector = orig_list.head;vector != null; vector = vector.next ) {

        /* Simple optimization is to see if that vector is already in the
           list, because then we know that there is no region point and
           the difference is zero. Because this is used to compare sets
           that will often be overlapping, this is a good optimization. We
           use a very small epsilon because this optimization is focused
           on when the values really should be identical. */
        if ( ap.queryAlphaList( test_list, vector.alpha, 
                             SMALLEST_PRECISION ))
          continue;

        if ( rg.findRegionPoint( vector.alpha, test_list, 
                              g.getgTempBelief(), true,param ) == 1) {
            diff =rg.getDiff();
            g.displaygTempBelief();
          /* We want to keep track of the maximal difference between the
             two sets so we know the true error. */
          max_diff = Math.max( max_diff,  diff);

          if ( rg.getDiff() > param.prune_epsilon )
            return ( FALSE );

        } /* if LP found a region point */

      } /* for vector */

      return ( TRUE );

    } /* isEpsilonApproximation */
    /**********************************************************************/
    public AlphaList epsilonPrune( AlphaList list, 
                  PomdpSolveParams param  ) 
    {
      /*
        This is the first implementation of a real epsilon pruning algorithm
        and is correct, but not that efficient.  It operates by determining
        whether removing a vector leaves a set of vectors that is still
        epilson approximates the original.  This requires a copy of the
        original set.  Note that although the input set may not be minimal,
        it still represents the "exact" value function and we can use it for
        comparison. 
      */
      int num_pruned = 0;
      AlphaList orig_list = ap.newAlphaList();
      AlphaNode test_vector = new AlphaNode();
     // double diff = 0;
      if(list.length <= 1)
          return list;
      /* Keep track of the actual computed difference between the returned
         pruned set and the original set sent in. */
      max_diff = 0.0;

      /* Because we will need to check the sets that result from removing
         vectors against the "true" set, we need to always maintain the
         true set. */
      orig_list = ap.duplicateAlphaList( list );

      /* Unmark all vector in the list so we can keep track of which ones
         we have checked.  Because the set will be dynamically changing as
         we test, it is easiest to just marke them as we use them rather
         than have to worry about which ones we remove and where we put
         them back. */
      list = ap.clearMarkAlphaList( list );
      /* Need to try each vector. */
      while(ap.sizeUnmarkedAlphaList( list ) > 0) {
        /* Extract an unchecked vector and mark it. */
        test_vector = ap.extractUnmarkedVector( list );
        test_vector.mark = TRUE;
        list = ap.getList();
        assert(list.length != 0): "null list";
       // System.out.println("sizelllllllllllllis    " + list.length);
        /* If the resulting list with the vector removed is an epsilon
           approximation then we can just get rid of it.  Otherwise we
           need to add it back into the set. This routine returns the
           actual computed maximal difference between the two sets. */
        if ( isEpsilonApproximation( list, orig_list, 
                                     param ) == 1) {
           
          ap.destroyAlphaNode( test_vector );
          //System.out.println("destroy node is  " +list.length);
          num_pruned++;
          max_diff = Math.max( max_diff, rg.getDiff() );
         // System.out.println("list sieze "+list.length);

        } /* if set is still an epsilon approximation */

        else
         list = ap.enqueueAlphaNode( list, test_vector );

      } /* while no more unmarked vectors */

      param.epsilon_diff_of_last_prune = max_diff;
 
      return(list);

    }  /* epsilonPrune */
    /**********************************************************************/

    /**********************************************************************/
    /*******   Main routines for epsilon approximate sets    **************/
    /**********************************************************************/

    /**********************************************************************/
        @Override
    public AlphaList dominationCheck( AlphaList orig_list ) 
    {
      /*
        Removes all vectors from the list that can be determined to have a
        non-empty region using only the simple component-wise domination
        check with some other vector in the list.
      */
      AlphaNode list;

      assert( orig_list != null): "List is NULL.dominationCheck" ;

      /* There is no way there can be anything to remove unless there are
         at least two elements. */
      if ( orig_list.length < 2 )
        return ( orig_list );

      /* We will first set the 'mark' field of the list and then delete
         the 'mark'ed nodes.  So first we need to clear the 'mark' field,
         just in case they are set. */
      AlphaNode temp =orig_list.head;
      while(temp != null){
          temp.mark = 0;
          temp = temp.next;
      }
     // orig_list = ap.clearMarkAlphaList( orig_list );
      //if(orig_list != null){
      /* Now we simply go through each node in the list and mark any in
         the list that are dominated by it. Note that at some point in the
         domination check, the same vector will be comared to itself.  Since
         the dominated check does not consider a vector to dominate
         itself, we don't have to worry about it being inadvertently
         marked. */
      list = orig_list.head;
      while ( list != null ) {

        /* If we have already marked this vector as being dominated by
           something in the list, then there is no point in looking for
           vectors in the list which it dominates.  By transitivity of the
           domination check, any that this vector might dominated would
           have already been marked. */
        if ( list.mark != TRUE ){
          orig_list = ap.markDominatedAlphaList( list.alpha, orig_list );
        }
        list = list.next;
      } /* while list != NULL */
      //}
       
      AlphaList newlist = ap.newAlphaList();
      AlphaNode tp = orig_list.head;
      while(tp != null){
          if(tp.mark != TRUE)
              newlist = ap.appendNodeToAlphaList(newlist, tp);
          tp = tp.next;
      }
      
      //return( ap.removeMarkedAlphaList( orig_list ));
      return newlist;
    }  /* dominationCheck */
    /**********************************************************************/
        @Override
    public AlphaList normalPrune( AlphaList orig_list, PomdpSolveParams param ) 
    {
      /* 
         Will use linear programming to prune the list to a unique
         parsimonious represenation.  If the save_points flag is set, then
         each vector in the resulting set (with a non-empty) region will
         have the witness point used to verify its non-empty region saved in
         the node containing the vector. Returns the number of nodes pruned.

         if the save_witness_points flag is TRUE, then for every useful
         vector found, we will also save the witness point that was found
         for this vector.

         If the init_num_random_points value is > 0, then it will preceed
         the LP computation with a check for useful vectors at random
         points. 

         This uses the scheme propose by Lark and White, which is mentioned
         in White's 1991 Operations Research POMDP survey article.
      */
      AlphaList new_alpha_list;
      AlphaNode cur_node, best_node;
      int num_pruned = 0;

      assert( orig_list != null): "List is NULL.normalPrune" ;
      
      /* Want to allow variations on the epsilon pruning.
         */
      /* We will mark the best node for each simplex vertex and ranodm
         point initialization, so first clear the 'mark' field. */
     orig_list = ap.clearMarkAlphaList( orig_list );

      /* First we select vectors using this simple test. This will
         only mark the best vectors. */
      orig_list = markBestAtSimplexVertices( orig_list, 
                                 param.use_witness_points,
                                 param.alpha_epsilon );
     AlphaList prev = ap.duplicateAlphaList(orig_list);
      /* Use random points to initialize the list, but this will only do
         something if param->prune_init_rand_points > 0 */
      /*orig_list = markBestAtRandomPoints( orig_list, 
                              param.prune_init_rand_points,
                              param.use_witness_points,
                              param.alpha_epsilon );

      /* Now we actually initialize the parsimonious list with those
         vectors found through the simpler checks. */
      
    new_alpha_list = ap.extractMarkedAlphaList( orig_list );
    
    while ( prev.length > 0 ) {

    /* Remove a node from the original list. */
    cur_node = ap.dequeueAlphaNode( prev );
    prev = ap.getList();
    /* See if this node gives us a witness point that there must be a
       vector to be added to new list from original list. */
    if ( rg.findRegionPoint( cur_node.alpha, new_alpha_list, g.getgTempBelief(), false, param ) == 1) {
      /* Note that the finding of a witness point does *not*
         necessarily mean that cur_node is the best vector at this
         point.  Since we only compare cur_node to the new list, we do
         not know whether there are vectors in the original list which
         might be even better still. */
        cur_node.witness = rg.getWitness();
      /* Therefore, we first put this node back into the list and then
         find the vector in the list that is maximal for this
         point. */
        //System.out.println("Not empty region witness " + cur_node.alpha[0]+" "+cur_node.alpha[1]);
      prev = ap.enqueueAlphaNode( prev, cur_node );
     
          best_node = ap.removebestVectorNode( prev, g.getgTempBelief(),
                                            param.alpha_epsilon );
          prev = ap.getList();
          //prev = ap.extractAlphaNode(prev, best_node);
          new_alpha_list = ap.appendNodeToAlphaList( new_alpha_list, best_node );
    
    } /* If we did find a witness point. */

    /* Otherwise, no witness point was found which mean we can simply
       get rid of this node. */
    else {
     // destroyAlphaNode( cur_node );
      num_pruned++;
    } /* else no witness point was found. */

  } /* while orig_list->length > 0 */
        System.out.println("num_pruned is: "+num_pruned);
    
      return( new_alpha_list );
    }  /* normalPrune */
    /**********************************************************************/
        
    public AlphaList prune( AlphaList orig_list, 
           PurgeOption purge_option,
           PomdpSolveParams param ) 
    {
      /*
        This routine just serves as a multiplex or for the particular type of
        pruning option specified. 
      */
      //int num_pruned = 0;
      AlphaList list = null;;
      switch ( purge_option ) {

      case purge_epsilon_prune:          
        list = epsilonPrune( orig_list, param );        
        break;
      case purge_prune:
          
      default:
        list = normalPrune( orig_list, param );
       break;
      } /* switch */

      return list;

    }  /* prune */
    /**********************************************************************/
        
    public AlphaList purgeAlphaList( AlphaList list, 
                    PurgeOption purge_option,
                    PomdpSolveParams param ) 
    {
      /*
        Removes vectors from the list according to the purging option sent
        it.  It can do anything from nothing, to simple domination checks,
        to full blown 'pruning'.

        It needs to param structure because it uses some of those fileds to
        decide how to do the pruning.
      */
      assert( list != null): "List is NULL.purgeAlphaList" ;
     
      switch ( purge_option ) {
      case purge_dom:
        dominationCheck( list ); 
        break;

      case purge_prune:
      case purge_epsilon_prune:
        /* We assume that pruning always does a domination check first. */
       list = dominationCheck( list );         
       list = prune( list, purge_option, param );
       break;
      case purge_none:         
      default:
        /* Do nothing. */
        break;
      } /* switch */
      return list;
     }  /* purgeAlphaList */
    /**********************************************************************/
        
    public AlphaList[][] purgeProjections( AlphaList[][] projection, 
                      PomdpSolveParams param ) 
    {
      /*
        Runs the purgeAlphaList() routine on all the projection sets using
        the purging option for projections as set on the command line (or
        with default.)
      */
      int a, z;

      if ( projection == null )
        return null;
     // System.out.println("Size >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+projection[0][0].length);
      for ( a = 0; a < projection.length; a++ ) 
        for ( z = 0; z < projection[0].length; z++ )

          /* If an observation is not possible, then we will have an empty
             projection list. Also, there is no need to purge a list of
             length 1. */
          if ( projection[a][z].length > 1 ){
              //System.out.println(">+++++++++++++++++++++++++++++++++++++++++++");
            projection[a][z] = purgeAlphaList( projection[a][z], 
                            param.proj_purge,
                            param );
          }
      return projection;
    }  /* purgeProjections */

    @Override
    public boolean Equal(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
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
