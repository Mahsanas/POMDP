package pomdp;
import Enum.GeneralizedIpChoice;
import Enum.PurgeOption;
import mdp.globalMDP;
import mdp.mdpIN.*;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;
import pomdp.paramsIN.*;
import pomdp.commonIN.*;
import pomdp.cross_sumIN.*;
import pomdp.parsimoniousIN.*;
import pomdp.inc_pruneIN.*;
public class inc_prune implements inc_pruneIN{
    public int count = 0, should_destroy = 0;
    private cross_sum cs;
    private alpha ap;
    private parsimonious psm;
    private region rg;
    private common cm ;
    private globalMDP gm ;
    private global g;
    int TRUE = 1, FALSE = 0;
    /**********************************************************************/
    public inc_prune(common cm, alpha ap, parsimonious psm, region rg, globalMDP gm, global g){
       this.cm = cm;
        //cs = this.cs;
        this. ap = ap;
        this.psm = psm;
        this.rg = rg;
        this.gm = gm;
        this.g = g;
    }
/**********************************************************************/   
        @Override
    public void initIncPrune( ) 
    {
        cs = new cross_sum(ap, gm);
    }  /* initCrossSum */
/**********************************************************************/
        @Override
    public void cleanUpIncPrune(  ) 
    {

    }  /* cleanUpCrossSum */
/**********************************************************************/
    public void clearAlphaListCounter( AlphaList list ) 
    {
      /*
        There are circumstances where we want to keep a counter for the
        nodes in an AlphaList.  Since the 'length' field is only used for
        headers, we will use this field in the nodes of the list as a
        counter.  This routine zero's out the counters for the nodes in a
        list.
      */
      assert( list != null): "List is NULL." ;

      AlphaNode temp = list.head;
      while ( temp != null ) {

        //COUNT(list);
        count = 0;
        temp = temp.next;
      } /* while */

    }  /* clearAlphaListCounter */
/**********************************************************************/
    public AlphaList initializeCountersIp( AlphaList list ) 
    {
      /* 
         Increments the count for the first and second source nodes of the
         nodes in this list.
      */
      assert( list != null): "List is NULL." ;

      AlphaNode temp = list.head;
      while ( temp != null ) {

        if ( temp.first_source != null ){
            count++;
        }
        if ( temp.second_source != null ){ 
            count++;
    }
        temp = temp.next;
      } /* while */
      return list;
    }  /* initializeCountersIp */
/**********************************************************************/
    public void addSimpleSumIp( AlphaList new_list, 
                    AlphaList old_list, 
                    AlphaNode add_node,
                    AlphaNode skip_node ) 
    {
      /*
        For each vector in the 'old' list, it adds a vector to the 'new'
        list that had the vector in 'add_node' added to it.  It will ignore
        the node that is equal to 'skip_node' when doing this.
      */

      AlphaNode new_node = null;
      int i;

     assert( new_list != null 
          && old_list != null 
          && add_node != null):
          "Bad (NULL) parameter(s)." ;
      AlphaNode temp = old_list.head;
      while( temp != null ) {

        if ( temp != skip_node ) {
          new_list = ap.appendAlphaList( new_list, 
                                      ap.duplicateAlpha( temp.alpha ),
                                      temp.action );

          for ( i = 0; i < gm.getgNumStates(); i++ )
            new_node.alpha[i] += add_node.alpha[i];

        } /* if not the node to skip */

        temp = temp.next;
      } /* while */

    }  /* addSimpleSumIp */
/**********************************************************************/
    public void addExtractedNodesIp( AlphaList new_list, 
                         AlphaList old_list,
                         AlphaNode first_source, 
                         AlphaNode second_source ) 
    {
    /*
      For each node in old_list, if the first or second source fields
      match the first and second sources sent in, then adds a copy of this
      node the new_list.  If first or second source is NULL, assume it
      never matches.
    */

     assert( new_list != null && old_list != null):
          "Bad (NULL) parameter(s)." ;
     
      AlphaNode temp = old_list.head;
      while( temp != null ) {

        if ( ((first_source != null) 
              && (temp.first_source == first_source))
             || ((second_source != null) 
                 && (temp.second_source == second_source)))
         ap.appendAlphaList( new_list, 
                           ap.duplicateAlpha( temp.alpha ),
                           temp.action );

        temp = temp.next;
      } /* while */

    }  /* addExtractedNodesIp */
/**********************************************************************/
    public AlphaList getGenIpCompareList( AlphaNode node, 
                         AlphaList A, AlphaList B,
                         AlphaList cur_list,
                         GeneralizedIpChoice ip_type ) 
    {
      /*
        Based on the parameters, returns a pointer to an AlphaList
        consisting of all the alpha vectors that need to be compared to
        'node' in order to guarantee that we get a witness point if one
        exists.  This is the heart of the generalized incremental pruning
        algorithm. 
      */
      AlphaList compare_list = new AlphaList();

      assert( node != null
          && A != null && B != null
          && cur_list != null):
          "Bad (NULL) parameter(s).";
      /* What set we return depends on the parameter for the IP type to
         use. */
      switch( ip_type ) {

      case NormalIp:
        /* Normal IP just uses the current list of vectors, so just return
           this set as the one to use in the region comparison. */
        //SHOULD_DESTROY(cur_list, FALSE) ;
        return( cur_list );

      case RestrictedRegionIp:
        /* Restricted region IP always uses the same set to compare to. */ 
        compare_list = ap.newAlphaList();
        //SHOULD_DESTROY(compare_list, TRUE);

        addSimpleSumIp( compare_list, A, node.second_source,
                        node.first_source );
        addSimpleSumIp( compare_list, B, node.first_source,
                        node.second_source );
        break;

      case GeneralizedIp:
        /* This is the complicated case because the list we construct
           depends upon which candidate is smallest and because we need to
           select vectors out of the cur_list depending on the source
           fields of 'node'. Because we may or may not actually create a
           new list, we need some way to indicate whether or not the
           compare_list wil need to be destroyed.  We do this by setting
           the 'mark' field of the header node to */

        /* First determine which set will be the smallest. The simplest
           thing is for us not to have to consruct a set. This happens if
           the cur_list is smaller than the other two candidates. */
        if (( cur_list.length  <= A.length)
            && ( cur_list.length   <= B.length)) {

          SHOULD_DESTROY(cur_list, FALSE);
          return( cur_list );
        } /* If cur_list should be the comparison list. */

        /* Otherwise, we must construct a list from the remaining two
           candidates. */
        compare_list = ap.newAlphaList();
        SHOULD_DESTROY(compare_list,TRUE);

        /* Just see which variation is smaller. */
        if ( A.length < B.length) {

          addSimpleSumIp( compare_list, A, node.second_source,
                          node.first_source );
          addExtractedNodesIp( compare_list, cur_list, 
                               node.first_source, null );
        }

        else {
          addSimpleSumIp( compare_list, B, node.first_source,
                          node.second_source );
          addExtractedNodesIp( compare_list, cur_list, 
                               null, node.second_source );

        }

        break;

      default:
        System.out.println( "Unknown incremental pruning choice ." );
      } /* switch */

      /* zzz Do we need to scan this list for vectors that would be equal
         to 'node'? We have made sure the vector itself is not added, but
         the nature of the cross-sum is such that it could construct a
         buch of similar vectors. */
      return ( compare_list );

    }  /* getGenIpCompareList */
/**********************************************************************/
   public AlphaList generalizedIpCrossSum( AlphaList A, 
                           AlphaList B, 
                           PomdpSolveParams param ) 
    {
      /*
        Implements the special version of the cross-sum operation for the
        incremental pruning algorithm.  This is really a special form of the
        prune() routine which selects the list to compare each node to
        based upon parameter settings and knowledge of the fact that the
        list being pruned was from the cross-sum of two sets.  It is very,
        very similar to the prune() routine.
      */
      AlphaList new_list = ap.newAlphaList(), search_list  = ap.newAlphaList(), compare_list = ap.newAlphaList();
      AlphaNode cur_node = null, best_node = null;
      
      /* We want to keep track of how many vectors in new_list were
         derived from vectors in A and B.  This initializes the counters
         for the nodes of A and B so we can start counting. */
     // clearAlphaListCounter( A );
     // clearAlphaListCounter( B );

      /* First do the cross-sum, setting the first_source and
         second_source fields appropriately. */
     
      search_list = cs.crossSum( A, B, TRUE );
     
      //System.out.println("A is: "+ A.length +" "+B.length); 
      //System.out.println(search_list.length);
      /* See if we want to do the domination check. */
      if ( param.domination_check == 1 )
        search_list = psm.dominationCheck( search_list ); 

      /* This is both an optimization and a convenience for trying out the
         epsilonPrune() routines.  If the incremental pruning being done
         is the normal type, then all we need to do is a simple prune of
         the list.  The prune routine will decide whether to do the normal
         thing or an epsilon-prune version. */

      if ( param.ip_type == GeneralizedIpChoice.NormalIp ) {

       search_list = psm.prune( search_list, PurgeOption.purge_prune, param );
        return( search_list );

      } /* If NormalIp variation (optimization) */

      /* We will mark the best node for each simplex vertex and ranodm
         point initialization, so first clear the 'mark' field. */
      search_list = ap.clearMarkAlphaList( search_list );

      /* First we select vectors using this simple test. This will
         only mark the best vectors. */
     search_list = psm.markBestAtSimplexVertices( search_list, 
                                 param.use_witness_points,
                                 param.alpha_epsilon );

      /* Use random points to initialize the list, but this will only do
         something if param->prune_init_rand_points > 0 */
     search_list = psm.markBestAtRandomPoints( search_list, 
                              param.alg_init_rand_points,
                              param.use_witness_points,
                              param.alpha_epsilon );

      /* Now we actually initialize the parsimonious list with those
         vectors found through the simpler checks. */
      new_list = ap.extractMarkedAlphaList( search_list );

      /* Make sure the header of this list has the proper action set. */
      new_list.head.action= A.head.action;

      /* For each node in the list, increment the counts on the source
         nodes. */
    //  new_list = initializeCountersIp( new_list );
    /*   AlphaNode node = search_list.head;
      while(search_list.length > 0){
          node = ap.dequeueAlphaNode(search_list);
        /* This is where we determine which list should be used. It will
           set a field in this list to tell us whether we will need to
           destroy the list when we are done with it.  In some cases this
           routine will just return 'new_list' (which we most definitely
           do not want to destroy) and in other cases return a list that
           was temporarily constructed for comparison purpose. */
      /*  compare_list = getGenIpCompareList( cur_node, A, B, 
                                            new_list, param.ip_type );
          if(rg.findRegionPoint( node.alpha, compare_list, g.getgTempBelief(), false, param ) == 1){
              
              best_node = ap.removebestVectorNode( search_list, g.getgTempBelief(),
                                            param.alpha_epsilon );
              if(node != best_node)
                new_list = ap.appendNodeToAlphaList( new_list, best_node );
          }
          node = node.next;
      }*///end while
      while ( search_list.length > 0 ) {

        /* Remove a node from the original list. */
        cur_node = ap.dequeueAlphaNode( search_list );
        search_list = ap.getList();
        /* This is where we determine which list should be used. It will
           set a field in this list to tell us whether we will need to
           destroy the list when we are done with it.  In some cases this
           routine will just return 'new_list' (which we most definitely
           do not want to destroy) and in other cases return a list that
           was temporarily constructed for comparison purpose. */
        compare_list = getGenIpCompareList( cur_node, A, B, 
                                            new_list, param.ip_type );

        /* See if this node gives us a witness point that there must be a
           vector to be added to new list from original list. */
        if ( rg.findRegionPoint( cur_node.alpha, compare_list, 
                              g.getgTempBelief(),false, param ) == 1) {
            
          /* Note that the finding of a witness point does *not*
             necessarily mean that cur_node is the best vector at this
             point.  Since we only compare cur_node to the new list, we do
             not know whether there are vectors in the original list which
             might be even better still. */

          /* Therefore, we first put this node back into the list and then
             find the vector in the list that is maximal for this
             point. */
          search_list = ap.enqueueAlphaNode( search_list, cur_node );

          best_node = ap.removebestVectorNode( search_list, g.getgTempBelief(),
                                            param.alpha_epsilon );
          System.out.println("best alpha find by region "+best_node.alpha[0] +"  "+best_node.alpha[1]);
          /* zzz Depending on whether or not we can guarantee that the
             compare list does not have vectors equal to the node in
             question, the precision issue could mean that this is ading
             an epslion-redundant vector. */
          ap.appendNodeToAlphaList( new_list, best_node );

          /* Increment the count of number of vectors in the list for the
             source nodes. */
          
        } /* If we did find a witness point. */

        /* Otherwise, no witness point was found which mean we can simply
           get rid of this node. */
        else      
          ap.destroyAlphaNode( cur_node );

        if ( should_destroy == 1)
          ap.destroyAlphaList( compare_list );

      } /* while orig_list->length > 0 */

      return( new_list );

    }  /* generalizedIpCrossSum */
/**********************************************************************/
        
    public AlphaList improveIncPrune( AlphaList[] projection, 
                     PomdpSolveParams param ) 
    {
      /* The main incremental pruning algorithm routine for finding the
         Q-function represention for value iteration with POMDPs.  
      */
      int z;
      AlphaList list = ap.newAlphaList();
      AlphaList next_list = ap.newAlphaList();

      assert ( projection != null): "Projection is NULL." ;

      /* Check for the special case of a single observation POMDP.  This
         corresponds to a completely unobservable problem, but for this
         we have no cross-sum to do. */
      if ( gm.getgNumObservations() == 1 ) 
        return ( ap.duplicateAlphaList( projection[0] ));

        /* We do one less cross-sum than the number of observations. */
       for( z = 1; z < gm.getgNumObservations(); z++) {
         /* Note that we put the burden of setting the source arrays on the
            crossSum routine. */
         if ( z == 1 ) {
           next_list = generalizedIpCrossSum( projection[0], 
                                              projection[1],
                                              param );
         //System.out.println("A is: "+ projection[0].length +" "+projection[1].length);
         }
         else {
           next_list = generalizedIpCrossSum( list, 
                                              projection[z],
                                              param);
           ap.destroyAlphaList( list );
           System.out.println("test destroy list is " +list.length);
         }

         /* Note that if we simply put the pruning operation here, then
            you get the vanilla incremental pruning algorithm!!! */

         list = next_list;

       } /* for z */
//System.out.println(" >C<C<C<<C<C<C<C<C< "+list.length);
       return ( list );

    } /* improveIncPrune */
/**********************************************************************/
        @Override
        public int COUNT(AlphaList list) {
            count = list.length;
            return list.length;
        }
        @Override
        public int SHOULD_DESTROY(AlphaList X, int T){
           X.head.mark = T;
           should_destroy = T;
            return X.head.mark;
        }
/**********************************************************************/
}